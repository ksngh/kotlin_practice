package com.kotlin_practice.service

import com.kotlin_practice.domain.UserRole
import com.kotlin_practice.dto.ActivityStatsResponse
import com.kotlin_practice.repository.ChatRepository
import com.kotlin_practice.repository.LoginEventRepository
import com.kotlin_practice.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class AnalyticsService(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val loginEventRepository: LoginEventRepository,
) {
    fun getActivityStats(email: String, now: OffsetDateTime = OffsetDateTime.now()): ActivityStatsResponse {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        if (user.role != UserRole.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only")
        }

        val from = now.minusDays(1)
        val to = now
        val signUps = userRepository.countByCreatedAtBetween(from, to)
        val logins = loginEventRepository.countByCreatedAtBetween(from, to)
        val chats = chatRepository.countByCreatedAtBetween(from, to)

        return ActivityStatsResponse(
            from = from,
            to = to,
            signUpCount = signUps,
            loginCount = logins,
            chatCount = chats,
        )
    }
}
