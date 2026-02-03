package com.kotlin_practice.service

import com.kotlin_practice.domain.ChatEntity
import com.kotlin_practice.domain.ThreadEntity
import com.kotlin_practice.domain.UserRole
import com.kotlin_practice.dto.ChatCreateRequest
import com.kotlin_practice.dto.ChatItemResponse
import com.kotlin_practice.dto.ChatResponse
import com.kotlin_practice.dto.ThreadPageResponse
import com.kotlin_practice.dto.ThreadResponse
import com.kotlin_practice.openai.OpenAiClient
import com.kotlin_practice.openai.OpenAiMessage
import com.kotlin_practice.openai.OpenAiResponseRequest
import com.kotlin_practice.repository.ChatRepository
import com.kotlin_practice.repository.ThreadRepository
import com.kotlin_practice.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Service
class ChatService(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val openAiClient: OpenAiClient,
    @Value("\${openai.api.model}") private val defaultModel: String,
) {
    @Transactional
    fun createChat(email: String, request: ChatCreateRequest): ChatResponse {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        val now = OffsetDateTime.now()
        val thread = findOrCreateThread(user = user, now = now)
        val messages = buildMessages(thread.id, request.question)

        val model = request.model?.ifBlank { null } ?: defaultModel
        val answer = openAiClient.createResponse(
            OpenAiResponseRequest(
                model = model,
                input = messages,
                stream = false,
            ),
        )

        val chat = chatRepository.save(
            ChatEntity(
                thread = thread,
                user = user,
                question = request.question,
                answer = answer,
            ),
        )

        thread.lastMessageAt = now
        threadRepository.save(thread)

        return ChatResponse(
            threadId = thread.id!!,
            chatId = chat.id!!,
            question = chat.question,
            answer = chat.answer,
            createdAt = chat.createdAt ?: now,
        )
    }

    @Transactional
    fun streamChat(email: String, request: ChatCreateRequest): SseEmitter {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        val now = OffsetDateTime.now()
        val thread = findOrCreateThread(user = user, now = now)
        val messages = buildMessages(thread.id, request.question)
        val model = request.model?.ifBlank { null } ?: defaultModel

        val emitter = SseEmitter(0)
        emitter.send(
            SseEmitter.event()
                .name("meta")
                .data(mapOf("threadId" to thread.id)),
        )

        CompletableFuture.runAsync {
            val answerBuilder = StringBuilder()
            try {
                openAiClient.streamResponse(
                    OpenAiResponseRequest(
                        model = model,
                        input = messages,
                        stream = true,
                    ),
                ).doOnNext { delta ->
                    answerBuilder.append(delta)
                    emitter.send(SseEmitter.event().name("delta").data(delta))
                }.doOnComplete {
                    val answer = answerBuilder.toString()
                    val chat = chatRepository.save(
                        ChatEntity(
                            thread = thread,
                            user = user,
                            question = request.question,
                            answer = answer,
                        ),
                    )
                    thread.lastMessageAt = now
                    threadRepository.save(thread)
                    emitter.send(
                        SseEmitter.event()
                            .name("done")
                            .data(mapOf("chatId" to chat.id, "threadId" to thread.id)),
                    )
                    emitter.complete()
                }.doOnError { ex ->
                    emitter.send(SseEmitter.event().name("error").data(ex.message ?: "error"))
                    emitter.completeWithError(ex)
                }.subscribe()
            } catch (ex: Exception) {
                emitter.send(SseEmitter.event().name("error").data(ex.message ?: "error"))
                emitter.completeWithError(ex)
            }
        }

        return emitter
    }

    @Transactional
    fun listThreads(
        email: String,
        page: Int,
        size: Int,
        sortDir: String,
    ): ThreadPageResponse {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        val direction = if (sortDir.equals("asc", true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"))

        val threadPage = if (user.role == UserRole.ADMIN) {
            threadRepository.findAll(pageable)
        } else {
            threadRepository.findAllByUser(user, pageable)
        }

        val threadIds = threadPage.content.mapNotNull { it.id }
        val chats = if (threadIds.isEmpty()) {
            emptyList()
        } else {
            chatRepository.findAllByThreadIdInOrderByCreatedAtAsc(threadIds)
        }

        val chatsByThread = chats.groupBy { it.thread.id }
        val threadResponses = threadPage.content.map { thread ->
            val threadChats = chatsByThread[thread.id].orEmpty().map { chat ->
                ChatItemResponse(
                    chatId = chat.id!!,
                    question = chat.question,
                    answer = chat.answer,
                    createdAt = chat.createdAt!!,
                )
            }
            ThreadResponse(
                threadId = thread.id!!,
                createdAt = thread.createdAt!!,
                lastMessageAt = thread.lastMessageAt,
                chats = threadChats,
            )
        }

        return ThreadPageResponse(
            threads = threadResponses,
            page = threadPage.number,
            size = threadPage.size,
            totalElements = threadPage.totalElements,
            totalPages = threadPage.totalPages,
        )
    }

    @Transactional
    fun deleteThread(email: String, threadId: Long) {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        val thread = threadRepository.findById(threadId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found") }

        if (user.role != UserRole.ADMIN && thread.user.id != user.id) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden")
        }

        threadRepository.delete(thread)
    }

    private fun findOrCreateThread(user: com.kotlin_practice.domain.UserEntity, now: OffsetDateTime): ThreadEntity {
        val latest = threadRepository.findFirstByUserOrderByLastMessageAtDesc(user)
        if (latest == null) {
            return threadRepository.save(
                ThreadEntity(
                    user = user,
                    lastMessageAt = now,
                ),
            )
        }
        val minutes = ChronoUnit.MINUTES.between(latest.lastMessageAt, now)
        return if (minutes >= 30) {
            threadRepository.save(
                ThreadEntity(
                    user = user,
                    lastMessageAt = now,
                ),
            )
        } else {
            latest
        }
    }

    private fun buildMessages(threadId: Long?, question: String): List<OpenAiMessage> {
        if (threadId == null) return listOf(OpenAiMessage(role = "user", content = question))
        val history = chatRepository.findAllByThreadIdOrderByCreatedAtAsc(threadId)
        val messages = mutableListOf<OpenAiMessage>()
        history.forEach { chat ->
            messages.add(OpenAiMessage(role = "user", content = chat.question))
            messages.add(OpenAiMessage(role = "assistant", content = chat.answer))
        }
        messages.add(OpenAiMessage(role = "user", content = question))
        return messages
    }
}
