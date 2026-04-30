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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

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
            json(Json { ignoreUnknownKeys = true })
        }
    }

    var conversation = mutableStateListOf<ChatMessage>()

    var isLoading by mutableStateOf(false)
        private set

    var serverReady by mutableStateOf(false)
        private set

    fun sendMessage(message : String) {
        // Add temporary user message to conversation
        val userMsgIndex = conversation.size
        conversation.add(ChatMessage(role = "user", text = message))

        // Send message to backend
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

                // Update the user message with backend data (like corrections)
                conversation[userMsgIndex] = ChatMessage(
                    id = response.user_message.id,
                    role = "user",
                    text = response.user_message.text,
                    corrections = response.user_message.corrections
                )

                // Add assistant response
                conversation.add(ChatMessage(
                    id = response.assistant_message.id,
                    role = "assistant",
                    text = response.assistant_message.text,
                    translation = response.assistant_message.translation
                ))

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
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
                    delay(5000) // wait 5 seconds before trying again
                }
            }
        }
    }
}

@Serializable
data class ServerStatus(val status: String)

@Serializable
data class ChatMessage(
    val id: String? = null,
    val role: String,
    val text: String,
    val corrections: List<String>? = null,
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
    val corrections: List<String>? = null
)

@Serializable
data class AssistantMessageDetail(
    val id: String,
    val text: String,
    val translation: String? = null
)
