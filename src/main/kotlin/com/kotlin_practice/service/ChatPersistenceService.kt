package com.kotlin_practice.service

import com.kotlin_practice.domain.ChatEntity
import com.kotlin_practice.repository.ChatRepository
import com.kotlin_practice.repository.ThreadRepository
import com.kotlin_practice.repository.UserRepository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class ChatPersistenceService(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveChatAndUpdateThread(
        userId: Long,
        threadId: Long,
        question: String,
        answer: String,
        now: OffsetDateTime,
    ): ChatEntity {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }
        val thread = threadRepository.findById(threadId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found") }

        val chat = chatRepository.save(
            ChatEntity(
                thread = thread,
                user = user,
                question = question,
                answer = answer,
            ),
        )

        thread.lastMessageAt = now
        threadRepository.save(thread)
        return chat
    }
}
