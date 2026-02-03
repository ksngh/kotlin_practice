package com.kotlin_practice.openai

import reactor.core.publisher.Flux

interface AiClient {
    fun createResponse(request: OpenAiResponseRequest): String
    fun streamResponse(request: OpenAiResponseRequest): Flux<String>
}
