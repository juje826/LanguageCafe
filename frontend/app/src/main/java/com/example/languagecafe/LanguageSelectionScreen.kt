package com.example.languagecafe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Language(val code: String, val label: String)

@Composable
fun LanguageSelectionPage(
    onLanguageSelected: (targetLanguage: String) -> Unit
) {

    val languages = listOf(
        Language("es", "Spanish"),
        Language("nl", "Dutch"),
        Language("de", "German"),
        Language("fr", "French")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding()
            )
    ) {

        AppHeader()

        Text(
            "Choose a Language",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {

            items(languages) { language ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {

                            onLanguageSelected(language.code)
                        },

                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Text(
                        language.label,

                        modifier = Modifier.padding(16.dp),

                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}