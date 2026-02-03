package com.kotlin_practice.repository

import com.kotlin_practice.domain.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun countByCreatedAtBetween(start: java.time.OffsetDateTime, end: java.time.OffsetDateTime): Long
}
