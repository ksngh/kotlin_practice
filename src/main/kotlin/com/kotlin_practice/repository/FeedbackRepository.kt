package com.kotlin_practice.repository

import com.kotlin_practice.domain.FeedbackEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FeedbackRepository : JpaRepository<FeedbackEntity, Long> {
    fun existsByUserIdAndChatId(userId: Long, chatId: Long): Boolean
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<FeedbackEntity>
    fun findAllByUserIdAndIsPositive(userId: Long, isPositive: Boolean, pageable: Pageable): Page<FeedbackEntity>
    fun findAllByIsPositive(isPositive: Boolean, pageable: Pageable): Page<FeedbackEntity>
}
