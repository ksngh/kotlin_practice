package com.kotlin_practice.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_feedback_user_chat", columnNames = ["user_id", "chat_id"]),
    ],
)
class FeedbackEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: ChatEntity,

    @Column(nullable = false)
    val isPositive: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FeedbackStatus = FeedbackStatus.PENDING,

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
) {
    // JPA requires a no-arg constructor
    protected constructor() : this(
        id = null,
        user = UserEntity(
            email = "",
            password = "",
            name = "",
            role = UserRole.MEMBER,
        ),
        chat = ChatEntity(
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
        ),
        isPositive = true,
        status = FeedbackStatus.PENDING,
        createdAt = null,
    )
}
