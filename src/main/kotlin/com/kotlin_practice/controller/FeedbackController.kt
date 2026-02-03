package com.kotlin_practice.controller

import com.kotlin_practice.dto.FeedbackCreateRequest
import com.kotlin_practice.dto.FeedbackPageResponse
import com.kotlin_practice.dto.FeedbackResponse
import com.kotlin_practice.dto.FeedbackStatusUpdateRequest
import com.kotlin_practice.service.FeedbackService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class FeedbackController(
    private val feedbackService: FeedbackService,
) {
    @PostMapping("/feedbacks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "피드백 생성", description = "특정 대화에 대한 피드백을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "생성 성공"),
            ApiResponse(responseCode = "400", description = "검증 실패"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "403", description = "권한 없음"),
            ApiResponse(responseCode = "409", description = "이미 피드백 존재"),
        ],
    )
    fun createFeedback(@Valid @RequestBody request: FeedbackCreateRequest): FeedbackResponse {
        return feedbackService.createFeedback(currentEmail(), request)
    }

    @GetMapping("/feedbacks")
    @Operation(summary = "피드백 목록 조회", description = "사용자/관리자 권한에 따라 피드백 목록을 조회합니다.")
    fun listFeedbacks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
        @RequestParam(required = false) isPositive: Boolean?,
    ): FeedbackPageResponse {
        return feedbackService.listFeedbacks(currentEmail(), page, size, sort, isPositive)
    }

    @PatchMapping("/feedbacks/{feedbackId}/status")
    @Operation(summary = "피드백 상태 변경", description = "관리자가 피드백 상태를 변경합니다.")
    fun updateStatus(
        @PathVariable feedbackId: Long,
        @Valid @RequestBody request: FeedbackStatusUpdateRequest,
    ): FeedbackResponse {
        return feedbackService.updateStatus(currentEmail(), feedbackId, request)
    }

    private fun currentEmail(): String {
        val auth = SecurityContextHolder.getContext().authentication
        return auth?.name ?: throw IllegalStateException("Unauthenticated")
    }
}
