package com.kotlin_practice.service

import com.kotlin_practice.domain.UserRole
import com.kotlin_practice.repository.ChatRepository
import com.kotlin_practice.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class ReportService(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
) {
    fun generateChatsCsv(email: String, now: OffsetDateTime = OffsetDateTime.now()): String {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        if (user.role != UserRole.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only")
        }

        val from = now.minusDays(1)
        val to = now
        val chats = chatRepository.findAllByCreatedAtBetweenOrderByCreatedAtAsc(from, to)

        val sb = StringBuilder()
        sb.appendLine("chat_id,user_id,user_email,question,answer,created_at")
        chats.forEach { chat ->
            val question = escapeCsv(chat.question)
            val answer = escapeCsv(chat.answer)
            val createdAt = chat.createdAt ?: now
            sb.append(chat.id).append(',')
                .append(chat.user.id).append(',')
                .append(escapeCsv(chat.user.email)).append(',')
                .append(question).append(',')
                .append(answer).append(',')
                .append(createdAt)
                .appendLine()
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String?): String {
        if (value == null) return ""
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }
}
