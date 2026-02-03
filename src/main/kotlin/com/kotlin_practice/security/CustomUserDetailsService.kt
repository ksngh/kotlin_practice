package com.kotlin_practice.security

import com.kotlin_practice.user.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): User {
        val user = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found")

        val authority = SimpleGrantedAuthority("ROLE_${user.role.name}")
        return User(user.email, user.password, listOf(authority))
    }
}

