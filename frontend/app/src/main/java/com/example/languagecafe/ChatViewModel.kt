package com.example.languagecafe

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ChatViewModel : ViewModel() {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    var conversation = mutableStateListOf<ChatMessage>()

    fun sendMessage(message : String) {
        // Add user message to conversation
        conversation.add(ChatMessage("user", message))

        // Send message to backend
        viewModelScope.launch {
            try {
                Log.i("ChatViewModel", "Sending request to backend: $message")
                val response: LLMResponse = client.post("https://languagecafe.onrender.com/chat") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("message" to message))
                }.body()

                // Add response
                conversation.add(ChatMessage("assistant", response.response))
                Log.i("ChatViewModel", "Assistant: ${response.response}")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }
}

@Serializable
data class ChatMessage(val role: String, val text: String)

@Serializable
data class LLMResponse(val response: String)