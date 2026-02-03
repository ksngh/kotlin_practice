package com.kotlin_practice.controller

import com.kotlin_practice.dto.ActivityStatsResponse
import com.kotlin_practice.service.AnalyticsService
import com.kotlin_practice.service.ReportService
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
@RequestMapping("/admin")
class AdminAnalyticsController(
    private val analyticsService: AnalyticsService,
    private val reportService: ReportService,
) {
    @GetMapping("/activity")
    @Operation(summary = "사용자 활동 기록", description = "지난 24시간 회원가입/로그인/대화 생성 수를 반환합니다.")
    fun activity(): ActivityStatsResponse {
        val email = currentEmail()
        return analyticsService.getActivityStats(email)
    }

    @GetMapping("/reports/chats.csv", produces = [MediaType.TEXT_PLAIN_VALUE])
    @Operation(summary = "대화 CSV 보고서", description = "지난 24시간 대화 내역 CSV를 생성합니다.")
    fun chatsReport(response: HttpServletResponse) {
        val email = currentEmail()
        val csv = reportService.generateChatsCsv(email)
        val filename = "chats-${OffsetDateTime.now().toLocalDate()}.csv"
        response.contentType = "text/csv; charset=UTF-8"
        response.characterEncoding = "UTF-8"
        response.setHeader("Content-Disposition", "attachment; filename=\"$filename\"")
        // Write UTF-8 BOM so Excel opens correctly
        response.writer.write("\uFEFF")
        response.writer.write(csv)
    }

    private fun currentEmail(): String {
        val auth = SecurityContextHolder.getContext().authentication
        return auth?.name ?: throw IllegalStateException("Unauthenticated")
    }
}
