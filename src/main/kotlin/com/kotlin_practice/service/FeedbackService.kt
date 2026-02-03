package com.kotlin_practice.service

import com.kotlin_practice.domain.FeedbackEntity
import com.kotlin_practice.domain.FeedbackStatus
import com.kotlin_practice.domain.UserRole
import com.kotlin_practice.dto.FeedbackCreateRequest
import com.kotlin_practice.dto.FeedbackPageResponse
import com.kotlin_practice.dto.FeedbackResponse
import com.kotlin_practice.dto.FeedbackStatusUpdateRequest
import com.kotlin_practice.repository.ChatRepository
import com.kotlin_practice.repository.FeedbackRepository
import com.kotlin_practice.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class FeedbackService(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val feedbackRepository: FeedbackRepository,
) {
    @Transactional
    fun createFeedback(email: String, request: FeedbackCreateRequest): FeedbackResponse {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        val chatId = request.chatId.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid chatId")

        val chat = if (user.role == UserRole.ADMIN) {
            chatRepository.findById(chatId).orElse(null)
        } else {
            chatRepository.findByIdAndUserId(chatId, user.id!!)
        } ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed")

        if (feedbackRepository.existsByUserIdAndChatId(user.id!!, chatId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Feedback already exists")
        }

        val feedback = feedbackRepository.save(
            FeedbackEntity(
                user = user,
                chat = chat,
                isPositive = request.isPositive,
                status = FeedbackStatus.PENDING,
            ),
        )

        return feedback.toResponse()
    }

    @Transactional
    fun listFeedbacks(
        email: String,
        page: Int,
        size: Int,
        sortDir: String,
        isPositive: Boolean?,
    ): FeedbackPageResponse {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        val direction = if (sortDir.equals("asc", true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"))

        val pageResult = if (user.role == UserRole.ADMIN) {
            if (isPositive == null) {
                feedbackRepository.findAll(pageable)
            } else {
                feedbackRepository.findAllByIsPositive(isPositive, pageable)
            }
        } else {
            if (isPositive == null) {
                feedbackRepository.findAllByUserId(user.id!!, pageable)
            } else {
                feedbackRepository.findAllByUserIdAndIsPositive(user.id!!, isPositive, pageable)
            }
        }

        val responses = pageResult.content.map { it.toResponse() }
        return FeedbackPageResponse(
            feedbacks = responses,
            page = pageResult.number,
            size = pageResult.size,
            totalElements = pageResult.totalElements,
            totalPages = pageResult.totalPages,
        )
    }

    @Transactional
    fun updateStatus(email: String, feedbackId: Long, request: FeedbackStatusUpdateRequest): FeedbackResponse {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")

        if (user.role != UserRole.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only")
        }

        val feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found") }

        feedback.status = request.status
        return feedback.toResponse()
    }

    private fun FeedbackEntity.toResponse(): FeedbackResponse {
        return FeedbackResponse(
            feedbackId = id!!,
            userId = user.id!!,
            chatId = chat.id!!,
            isPositive = isPositive,
            status = status,
            createdAt = createdAt!!,
        )
    }
}
