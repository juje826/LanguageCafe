package com.example.languagecafe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

data class Scenario(val id: String, val title: String)

@Composable
fun ScenarioSelectionPage(
    onScenarioSelected: (scenarioId: String, sessionId: String) -> Unit
) {
    // available scenarios
    val scenarios = listOf(
        Scenario("coffee_ordering", "Ordering Coffee"),
        //Scenario("introducing_yourself", "Introducing Yourself"),
        //Scenario("family_conversation", "Talk About Family")
        // add more when needed
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {

        AppHeader()

        Text(
            "Choose a Scenario",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(scenarios) { scenario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            val sessionId = UUID.randomUUID().toString()
                            onScenarioSelected(scenario.id, sessionId)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        scenario.title,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}