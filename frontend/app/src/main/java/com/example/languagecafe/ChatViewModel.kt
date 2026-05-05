package com.example.languagecafe

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.util.UUID

class ChatViewModel : ViewModel() {

    var nativeLanguage: String? = null
    var targetLanguage: String? = null
    var sessionId: String = ""
    var scenarioId: String = ""

    var streak by mutableStateOf(0)
    var dailyGoal by mutableStateOf(5)
    var dailyProgress by mutableStateOf(0)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                coerceInputValues = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("Ktor", message)
                }
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 100_000 // 100 seconds
            connectTimeoutMillis = 100_000
            socketTimeoutMillis = 100_000
        }
    }

    var conversation = mutableStateListOf<ChatMessage>()

    var isLoading by mutableStateOf(false)
        private set

    var serverReady by mutableStateOf(false)
        private set

    fun sendMessage(message : String) {
        // Generate a temporary ID to track this specific message
        val tempId = UUID.randomUUID().toString()
        conversation.add(ChatMessage(id = tempId, role = "user", text = message))

        viewModelScope.launch {
            try {
                isLoading = true

                Log.i("ChatViewModel", "Sending request to backend: $message")
                val response: LLMResponse = client.post("https://languagecafe.onrender.com/chat") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "message" to message,
                            "session_id" to sessionId,
                            "scenario_id" to scenarioId,
                            "native_language" to nativeLanguage,
                            "target_language" to targetLanguage
                        )
                    )
                }.body()

                // FIND AND UPDATE the user message using the stable ID
                val index = conversation.indexOfFirst { it.id == tempId }
                if (index != -1) {
                    conversation[index] = ChatMessage(
                        id = response.user_message.id, // Update to the real ID from backend
                        role = "user",
                        text = response.user_message.text,
                        corrections = response.user_message.corrections
                    )
                }

                // Add assistant response
                conversation.add(ChatMessage(
                    id = response.assistant_message.id,
                    role = "assistant",
                    text = response.assistant_message.text,
                    translation = response.assistant_message.translation
                ))

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                conversation.add(ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = "assistant",
                    text = "Sorry, something went wrong. Please check your connection or try again."
                ))
            } finally {
                isLoading =  false
            }
        }
    }

    fun checkServer() {
        viewModelScope.launch {
            while (!serverReady) {
                try {
                    val response: ServerStatus = client.get(
                        "https://languagecafe.onrender.com/"
                    ).body()

                    if (response.status == "LanguageCafe backend running") {
                        serverReady = true
                    }
                } catch (e: Exception) {
                    Log.i("ChatViewModel", "Server still waking up...")
                }

                if (!serverReady) {
                    delay(5000)
                }
            }
        }
    }
}

@Serializable
data class ServerStatus(val status: String)

@Serializable
data class Correction(
    val original: String,
    val corrected: String,
    val explanation: String
)

@Serializable
data class ChatMessage(
    val id: String,
    val role: String,
    val text: String,
    val corrections: List<Correction>? = null,
    val translation: String? = null
)

@Serializable
data class LLMResponse(
    val user_message: UserMessageDetail,
    val assistant_message: AssistantMessageDetail
)

@Serializable
data class UserMessageDetail(
    val id: String,
    val text: String,
    val corrections: List<Correction>? = null
)

@Serializable
data class AssistantMessageDetail(
    val id: String,
    val text: String,
    val translation: String? = null
)
