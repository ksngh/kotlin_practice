package com.kotlin_practice.repository

import com.kotlin_practice.domain.ThreadEntity
import com.kotlin_practice.domain.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ThreadRepository : JpaRepository<ThreadEntity, Long> {
    fun findFirstByUserOrderByLastMessageAtDesc(user: UserEntity): ThreadEntity?
    fun findAllByUser(user: UserEntity, pageable: Pageable): Page<ThreadEntity>
}
