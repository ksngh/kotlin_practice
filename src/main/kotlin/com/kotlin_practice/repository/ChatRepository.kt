package com.kotlin_practice.repository

import com.kotlin_practice.domain.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<ChatEntity, Long> {
    fun findAllByThreadIdInOrderByCreatedAtAsc(threadIds: List<Long>): List<ChatEntity>
    fun findAllByThreadIdOrderByCreatedAtAsc(threadId: Long): List<ChatEntity>
    fun findByIdAndUserId(id: Long, userId: Long): ChatEntity?
}
