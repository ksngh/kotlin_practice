package com.kotlin_practice.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Kotlin Practice API",
        version = "v1",
        description = "Auth and core APIs",
    ),
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER,
    scheme = "bearer",
    bearerFormat = "JWT",
)
class OpenApiConfig

@Configuration
class OpenAiKeyLogConfig(
    @Value("\${openai.api.key:}") private val apiKey: String,
) {
    private val log = LoggerFactory.getLogger(OpenAiKeyLogConfig::class.java)

    @Bean
    fun openAiKeyLogger(): CommandLineRunner {
        return CommandLineRunner {
            val masked = maskKey(apiKey)
            log.info("OpenAI key loaded: {}", masked)
        }
    }

    private fun maskKey(key: String): String {
        if (key.isBlank()) return "EMPTY"
        val visible = key.takeLast(4)
        return "***$visible"
    }
}
