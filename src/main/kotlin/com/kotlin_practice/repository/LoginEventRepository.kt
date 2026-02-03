package com.kotlin_practice.repository

import com.kotlin_practice.domain.LoginEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime

interface LoginEventRepository : JpaRepository<LoginEventEntity, Long> {
    fun countByCreatedAtBetween(start: OffsetDateTime, end: OffsetDateTime): Long
}
