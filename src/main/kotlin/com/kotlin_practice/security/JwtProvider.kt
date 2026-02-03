package com.kotlin_practice.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date

@Component
class JwtProvider(
    @Value("\${security.jwt.secret}") private val secret: String,
    @Value("\${security.jwt.expiration-seconds}") private val expirationSeconds: Long,
) {
    private fun signingKey() = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun generateToken(subject: String): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(expirationSeconds)
        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiry))
            .signWith(signingKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            parseClaims(token)
            true
        } catch (ex: Exception) {
            false
        }
    }

    fun getSubject(token: String): String {
        return parseClaims(token).subject
    }

    private fun parseClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey())
            .build()
            .parseClaimsJws(token)
            .body
    }
}
