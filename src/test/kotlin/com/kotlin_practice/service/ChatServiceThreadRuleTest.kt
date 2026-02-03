package com.kotlin_practice.service

import com.kotlin_practice.domain.ChatEntity
import com.kotlin_practice.domain.ThreadEntity
import com.kotlin_practice.domain.UserEntity
import com.kotlin_practice.domain.UserRole
import com.kotlin_practice.dto.ChatCreateRequest
import com.kotlin_practice.openai.AiClient
import com.kotlin_practice.repository.ChatRepository
import com.kotlin_practice.repository.ThreadRepository
import com.kotlin_practice.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ChatServiceThreadRuleTest {
    private val userRepository = mock(UserRepository::class.java)
    private val threadRepository = mock(ThreadRepository::class.java)
    private val chatRepository = mock(ChatRepository::class.java)
    private val openAiClient = mock(AiClient::class.java)

    private val fixedInstant = Instant.parse("2026-02-03T06:00:00Z")
    private val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    private val now = OffsetDateTime.ofInstant(fixedInstant, ZoneOffset.UTC)

    private val service = ChatService(
        userRepository = userRepository,
        threadRepository = threadRepository,
        chatRepository = chatRepository,
        openAiClient = openAiClient,
        defaultModel = "gpt-4o-mini",
        clock = fixedClock,
    )

    private val user = UserEntity(
        id = 1L,
        email = "user@example.com",
        password = "pw",
        name = "User",
        role = UserRole.MEMBER,
    )

    private fun stubCommon() {
        `when`(userRepository.findByEmail(user.email)).thenReturn(user)
        `when`(openAiClient.createResponse(any())).thenReturn("answer")
        `when`(threadRepository.save(any(ThreadEntity::class.java))).thenAnswer { invocation ->
            val saved = invocation.arguments[0] as ThreadEntity
            ThreadEntity(
                id = saved.id ?: 100L,
                user = saved.user,
                createdAt = saved.createdAt,
                lastMessageAt = saved.lastMessageAt,
            )
        }
        `when`(chatRepository.save(any(ChatEntity::class.java))).thenAnswer { invocation ->
            val c = invocation.arguments[0] as ChatEntity
            ChatEntity(
                id = 10L,
                thread = c.thread,
                user = c.user,
                question = c.question,
                answer = c.answer,
                createdAt = now,
            )
        }
    }

    @Test
    fun `first question creates a new thread`() {
        val request = ChatCreateRequest(question = "q")
        `when`(threadRepository.findFirstByUserOrderByLastMessageAtDesc(user)).thenReturn(null)
        stubCommon()

        service.createChat(user.email, request)

        verify(threadRepository, times(2)).save(any(ThreadEntity::class.java))
    }

    @Test
    fun `within 30 minutes keeps existing thread`() {
        val request = ChatCreateRequest(question = "q")
        val existing = ThreadEntity(id = 200L, user = user, lastMessageAt = now.minusMinutes(10))
        `when`(threadRepository.findFirstByUserOrderByLastMessageAtDesc(user)).thenReturn(existing)
        stubCommon()

        service.createChat(user.email, request)

        verify(threadRepository, times(1)).save(any(ThreadEntity::class.java))
    }

    @Test
    fun `over 30 minutes creates a new thread`() {
        val request = ChatCreateRequest(question = "q")
        val existing = ThreadEntity(id = 200L, user = user, lastMessageAt = now.minusMinutes(31))
        `when`(threadRepository.findFirstByUserOrderByLastMessageAtDesc(user)).thenReturn(existing)
        stubCommon()

        service.createChat(user.email, request)

        verify(threadRepository, times(2)).save(any(ThreadEntity::class.java))
    }

    @Test
    fun `exactly 30 minutes keeps existing thread`() {
        val request = ChatCreateRequest(question = "q")
        val existing = ThreadEntity(id = 200L, user = user, lastMessageAt = now.minusMinutes(30))
        `when`(threadRepository.findFirstByUserOrderByLastMessageAtDesc(user)).thenReturn(existing)
        stubCommon()

        service.createChat(user.email, request)

        verify(threadRepository, times(1)).save(any(ThreadEntity::class.java))
    }
}
