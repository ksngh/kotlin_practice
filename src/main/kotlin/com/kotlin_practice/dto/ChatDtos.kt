package com.kotlin_practice.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

data class ChatCreateRequest(
    @field:NotBlank
    @field:Size(max = 2000)
    @field:Schema(example = "질문을 입력하세요.")
    val question: String,

    @field:Schema(description = "true면 스트리밍 응답", example = "false")
    val isStreaming: Boolean = false,

    @field:Schema(description = "OpenAI model", example = "gpt-4o-mini")
    val model: String? = null,
)

data class ChatResponse(
    val threadId: Long,
    val chatId: Long,
    val question: String,
    val answer: String,
    val createdAt: OffsetDateTime,
)

data class ThreadResponse(
    val threadId: Long,
    val createdAt: OffsetDateTime,
    val lastMessageAt: OffsetDateTime,
    val chats: List<ChatItemResponse>,
)

data class ChatItemResponse(
    val chatId: Long,
    val question: String,
    val answer: String,
    val createdAt: OffsetDateTime,
)

data class ThreadPageResponse(
    val threads: List<ThreadResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
