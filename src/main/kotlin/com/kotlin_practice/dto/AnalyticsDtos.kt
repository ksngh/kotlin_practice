package com.kotlin_practice.dto

import java.time.OffsetDateTime

data class ActivityStatsResponse(
    val from: OffsetDateTime,
    val to: OffsetDateTime,
    val signUpCount: Long,
    val loginCount: Long,
    val chatCount: Long,
)
