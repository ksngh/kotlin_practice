package com.kotlin_practice.controller

import com.kotlin_practice.dto.ChatCreateRequest
import com.kotlin_practice.dto.ChatResponse
import com.kotlin_practice.dto.ThreadPageResponse
import com.kotlin_practice.service.ChatService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.http.HttpStatus

@RestController
class ChatController(
    private val chatService: ChatService,
) {
    @PostMapping("/chats", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE])
    @Operation(summary = "대화 생성", description = "질문을 입력받아 답변을 생성합니다. isStreaming=true면 SSE로 스트리밍합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "성공",
                content = [Content(schema = Schema(implementation = ChatResponse::class))]),
            ApiResponse(responseCode = "400", description = "검증 실패"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
        ],
    )
    fun createChat(@Valid @RequestBody request: ChatCreateRequest): Any {
        val email = currentEmail()
        return if (request.isStreaming) {
            chatService.streamChat(email, request)
        } else {
            chatService.createChat(email, request)
        }
    }

    @GetMapping("/threads")
    @Operation(summary = "스레드/대화 목록", description = "스레드 단위로 대화를 조회합니다.")
    fun listThreads(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
    ): ThreadPageResponse {
        val email = currentEmail()
        return chatService.listThreads(email, page, size, sort)
    }

    @DeleteMapping("/threads/{threadId}")
    @Operation(summary = "스레드 삭제", description = "스레드와 하위 대화를 삭제합니다.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteThread(@PathVariable threadId: Long) {
        val email = currentEmail()
        chatService.deleteThread(email, threadId)
    }

    private fun currentEmail(): String {
        val auth = SecurityContextHolder.getContext().authentication
        return auth?.name ?: throw IllegalStateException("Unauthenticated")
    }
}
