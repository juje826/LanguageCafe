package com.example.languagecafe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.util.UUID

data class Scenario(val id: String, val title: String, val icon: ImageVector)

@Composable
fun ScenarioSelectionPage(
    onScenarioSelected: (scenarioId: String, sessionId: String) -> Unit
) {
    // available scenarios
    val scenarios = listOf(
        Scenario("coffee_ordering", "Ordering Coffee", Icons.Default.Coffee),
        Scenario("restaurant", "At the Restaurant", Icons.Default.Restaurant),
        Scenario("directions", "Asking for Directions", Icons.Default.Directions),
        Scenario("hotel", "Checking into a Hotel", Icons.Default.Hotel),
        Scenario("airport", "At the Airport", Icons.Default.LocalAirport),
        Scenario("party", "Socializing at a Party", Icons.Default.Celebration)
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {

        AppHeader()

        Text(
            "Choose a Scenario",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(scenarios) { scenario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val sessionId = UUID.randomUUID().toString()
                            onScenarioSelected(scenario.id, sessionId)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                scenario.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = scenario.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
