package com.kotlin_practice.openai

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.util.retry.Retry
import java.time.Duration

@Component
class OpenAiClient(
    @Value("\${openai.api.key}") private val apiKey: String,
    @Value("\${openai.api.base-url}") private val baseUrl: String,
    @Value("\${openai.retry.max-attempts}") private val maxAttempts: Long,
    @Value("\${openai.retry.backoff-ms}") private val backoffMs: Long,
    private val objectMapper: ObjectMapper,
) {
    private val webClient: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer $apiKey")
        .build()

    fun createResponse(request: OpenAiResponseRequest): String {
        val json = try {
            webClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String::class.java)
                .retryWhen(retry429())
                .block()
                ?: error("Empty OpenAI response")
        } catch (ex: WebClientResponseException.TooManyRequests) {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "OpenAI rate limit", ex)
        }

        return extractText(json)
    }

    fun streamResponse(request: OpenAiResponseRequest): Flux<String> {
        return webClient.post()
            .uri("/responses")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(request.copy(stream = true))
            .retrieve()
            .bodyToFlux(String::class.java)
            .retryWhen(retry429())
            .flatMapIterable { chunk -> parseSseChunk(chunk) }
            .filter { it.isNotEmpty() }
    }

    private fun parseSseChunk(chunk: String): List<String> {
        val lines = chunk.split("\n")
        val deltas = mutableListOf<String>()
        for (raw in lines) {
            val line = raw.trim()
            if (!line.startsWith("data:")) continue
            val data = line.removePrefix("data:").trim()
            if (data.isEmpty() || data == "[DONE]") continue
            val node = runCatching { objectMapper.readTree(data) }.getOrNull() ?: continue
            val type = node.path("type").asText()
            if (type == "response.output_text.delta") {
                val delta = node.path("delta").asText("")
                if (delta.isNotEmpty()) deltas.add(delta)
            } else if (type == "response.output_text.done") {
                val text = node.path("text").asText("")
                if (text.isNotEmpty()) deltas.add(text)
            }
        }
        return deltas
    }

    private fun extractText(json: String): String {
        val root = objectMapper.readTree(json)
        val output = root.path("output")
        if (!output.isArray) return ""
        val sb = StringBuilder()
        output.forEach { item ->
            val content = item.path("content")
            if (content.isArray) {
                content.forEach { c ->
                    val text = c.path("text").asText()
                    if (text.isNotEmpty()) sb.append(text)
                }
            }
        }
        return sb.toString()
    }

    private fun retry429(): Retry {
        return Retry.backoff(maxAttempts, Duration.ofMillis(backoffMs))
            .filter { ex -> ex is WebClientResponseException.TooManyRequests }
    }
}

data class OpenAiResponseRequest(
    @field:Schema(example = "gpt-4o-mini")
    val model: String,
    val input: List<OpenAiMessage>,
    val stream: Boolean = false,
)

data class OpenAiMessage(
    val role: String,
    val content: String,
)
