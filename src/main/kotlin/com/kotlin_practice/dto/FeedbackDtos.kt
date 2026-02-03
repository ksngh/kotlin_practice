package com.kotlin_practice.dto

import com.kotlin_practice.domain.FeedbackStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class FeedbackCreateRequest(
    @field:NotBlank
    @field:Schema(example = "10", description = "대화 ID")
    val chatId: String,

    @field:NotNull
    @field:Schema(example = "true", description = "긍정/부정")
    val isPositive: Boolean,
)

data class FeedbackStatusUpdateRequest(
    @field:NotNull
    val status: FeedbackStatus,
)

data class FeedbackResponse(
    val feedbackId: Long,
    val userId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: FeedbackStatus,
    val createdAt: OffsetDateTime,
)

data class FeedbackPageResponse(
    val feedbacks: List<FeedbackResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
