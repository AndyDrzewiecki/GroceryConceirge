package com.homeops.grocery.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeops.grocery.viewmodel.ConnectionState
import com.homeops.grocery.viewmodel.HomeUiState
import com.homeops.grocery.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCapture: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToReceipts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HomeOps Grocery") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Server settings",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Current server URL chip.
            SuggestionChip(
                onClick = onNavigateToSettings,
                label = {
                    Text(
                        text = "Server: ${viewModel.currentBaseUrl}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
            )

            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is HomeUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                    Button(onClick = { viewModel.loadDashboard() }) {
                        Text("Retry")
                    }
                }

                is HomeUiState.Success -> {
                    val dashboard = state.dashboard

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("This Month's Grocery Spend", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "€%.2f".format(dashboard.monthGrocerySpend),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Today's Meal", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = dashboard.todaysMeal ?: "No meal planned – generate a meal plan first.",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = onNavigateToCapture,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Capture Photo")
                    }

                    Button(
                        onClick = onNavigateToShoppingList,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = dashboard.shoppingNeeded,
                    ) {
                        Text(if (dashboard.shoppingNeeded) "View Shopping List" else "Shopping List (nothing needed)")
                    }

                    OutlinedButton(
                        onClick = onNavigateToReceipts,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("View Receipts")
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            HorizontalDivider()
            Text("Developer Tools", style = MaterialTheme.typography.labelSmall)
            OutlinedButton(
                onClick = { viewModel.testConnection() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Test Connection")
            }

            when (val conn = connectionState) {
                is ConnectionState.Testing -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is ConnectionState.Success -> Text(conn.message, color = MaterialTheme.colorScheme.primary)
                is ConnectionState.Failure -> Text("Failed: ${conn.message}", color = MaterialTheme.colorScheme.error)
                is ConnectionState.Idle -> Unit
            }
        }
    }
}
