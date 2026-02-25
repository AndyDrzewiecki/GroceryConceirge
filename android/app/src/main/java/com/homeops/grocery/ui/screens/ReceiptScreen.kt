package com.homeops.grocery.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeops.grocery.network.models.Receipt
import com.homeops.grocery.viewmodel.ReceiptUiState
import com.homeops.grocery.viewmodel.ReceiptViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    onBack: () -> Unit,
    viewModel: ReceiptViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingReceipt by remember { mutableStateOf<Receipt?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is ReceiptUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is ReceiptUiState.Error -> Text(
                    text = "Error: ${state.message}",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error,
                )

                is ReceiptUiState.Success -> {
                    if (state.receipts.isEmpty()) {
                        Text(
                            text = "No receipts yet. Capture a receipt photo first.",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.receipts, key = { it.id }) { receipt ->
                                ReceiptRow(receipt = receipt, onEdit = { editingReceipt = receipt })
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit dialog.
    editingReceipt?.let { receipt ->
        ReceiptEditDialog(
            receipt = receipt,
            onDismiss = { editingReceipt = null },
            onSave = { store, total, date ->
                viewModel.updateMetadata(receipt.id, store, total, date)
                editingReceipt = null
            },
        )
    }
}

@Composable
private fun ReceiptRow(receipt: Receipt, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(receipt.store ?: "Unknown store", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = buildString {
                        receipt.totalAmount?.let { append("€%.2f".format(it)) }
                        receipt.purchaseDate?.let { if (isNotEmpty()) append("  •  "); append(it) }
                        if (isEmpty()) append("No metadata – tap edit to add")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit receipt")
            }
        }
    }
}

@Composable
private fun ReceiptEditDialog(
    receipt: Receipt,
    onDismiss: () -> Unit,
    onSave: (store: String?, total: Double?, date: String?) -> Unit,
) {
    var store by remember { mutableStateOf(receipt.store ?: "") }
    var total by remember { mutableStateOf(receipt.totalAmount?.toString() ?: "") }
    var date by remember { mutableStateOf(receipt.purchaseDate ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Receipt #${receipt.id}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = store,
                    onValueChange = { store = it },
                    label = { Text("Store") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = total,
                    onValueChange = { total = it },
                    label = { Text("Total (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Purchase Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    store.ifBlank { null },
                    total.toDoubleOrNull(),
                    date.ifBlank { null },
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
