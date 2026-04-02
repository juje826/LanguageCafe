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
            LanguageCafeTheme {

                var selectedScenario by remember { mutableStateOf<String?>(null) }
                var sessionId by remember { mutableStateOf<String?>(null) }

                if (!chatViewModel.serverReady) {
                    LoadingScreen()
                } else {
                    // back button handling
                    BackHandler(enabled = selectedScenario != null) {
                        // go back to scenario selection
                        selectedScenario = null
                        sessionId = null
                        chatViewModel.conversation.clear() // optionally clear previous chat
                    }

                    if (selectedScenario == null || sessionId == null) {
                        // Show scenario selection
                        ScenarioSelectionPage { scenario, session ->
                            selectedScenario = scenario
                            sessionId = session
                            // Update the ViewModel
                            chatViewModel.scenarioId = scenario
                            chatViewModel.sessionId = session
                        }
                    } else {
                        // Show chat page
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
