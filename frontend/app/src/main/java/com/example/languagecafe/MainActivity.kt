package com.example.languagecafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.example.languagecafe.ui.theme.LanguageCafeTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        chatViewModel.checkServer()

        setContent {
            LanguageCafeTheme(darkTheme = false) {
                var nativeLanguage by remember { mutableStateOf<String?>(null) }
                var targetLanguage by remember { mutableStateOf<String?>(null) }

                var selectedScenario by remember { mutableStateOf<String?>(null) }
                var sessionId by remember { mutableStateOf<String?>(null) }

                if (!chatViewModel.serverReady) {
                    LoadingScreen()
                } else {
                    // back navigation logic
                    BackHandler(
                        enabled =
                            selectedScenario != null ||
                                    nativeLanguage != null
                    ) {

                        when {

                            // from chat -> go back to scenario selection
                            selectedScenario != null -> {

                                selectedScenario = null
                                sessionId = null

                                chatViewModel.conversation.clear()
                            }

                            // from scenario selection -> go back to language selection
                            nativeLanguage != null -> {

                                nativeLanguage = null
                                targetLanguage = null
                            }
                        }
                    }

                    when {

                        // language selection screen
                        nativeLanguage == null || targetLanguage == null -> {

                            LanguageSelectionPage { targetLang ->

                                nativeLanguage = "en"
                                targetLanguage = targetLang

                                // store in ViewModel so backend can access later
                                chatViewModel.nativeLanguage = targetLanguage
                                chatViewModel.targetLanguage = targetLang
                            }
                        }

                        // scenario selection screen
                        selectedScenario == null || sessionId == null -> {

                            ScenarioSelectionPage { scenario, session ->

                                selectedScenario = scenario
                                sessionId = session

                                chatViewModel.scenarioId = scenario
                                chatViewModel.sessionId = session
                            }
                        }

                        // chat screen
                        else -> {

                            Scaffold(
                                modifier = Modifier.fillMaxSize()
                            ) { innerPadding ->

                                ChatPage(
                                    modifier = Modifier.padding(innerPadding),
                                    viewModel = chatViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}