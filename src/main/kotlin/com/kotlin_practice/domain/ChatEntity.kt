package com.kotlin_practice.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity
@Table(
    name = "chats",
    indexes = [
        jakarta.persistence.Index(name = "idx_chats_thread_id", columnList = "thread_id"),
        jakarta.persistence.Index(name = "idx_chats_user_id", columnList = "user_id"),
        jakarta.persistence.Index(name = "idx_chats_created_at", columnList = "created_at"),
    ],
)
class ChatEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    val thread: ThreadEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(nullable = false)
    val question: String,

    @Column(nullable = false)
    val answer: String,

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
) {
    // JPA requires a no-arg constructor
    protected constructor() : this(
        id = null,
        thread = ThreadEntity(
            user = UserEntity(
                email = "",
                password = "",
                name = "",
                role = UserRole.MEMBER,
            ),
            lastMessageAt = OffsetDateTime.now(),
        ),
        user = UserEntity(
            email = "",
            password = "",
            name = "",
            role = UserRole.MEMBER,
        ),
        question = "",
        answer = "",
        createdAt = null,
    )
}
