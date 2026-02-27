package com.homeops.grocery.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.homeops.grocery.settings.SettingsManager

/**
 * Simple settings screen for overriding the backend BASE_URL at runtime.
 *
 * Changes are saved to SharedPreferences immediately when the user taps Save.
 * The next API call from any ViewModel will pick up the new URL automatically.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var urlInput by remember { mutableStateOf(SettingsManager.getBaseUrl()) }
    var saved by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current

    fun save() {
        if (urlInput.isNotBlank()) {
            val normalized = if (urlInput.endsWith("/")) urlInput else "$urlInput/"
            SettingsManager.saveBaseUrl(normalized)
            urlInput = normalized
            saved = true
            keyboard?.hide()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server Settings") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Backend BASE_URL",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Enter the full URL to your HomeOps backend, including the trailing slash.\n\n" +
                    "• Emulator default: http://10.0.2.2:8000/\n" +
                    "• Phone on LAN:     http://<miniPC-IP>:8000/",
                style = MaterialTheme.typography.bodySmall,
            )

            OutlinedTextField(
                value = urlInput,
                onValueChange = {
                    urlInput = it
                    saved = false
                },
                label = { Text("BASE_URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { save() }),
                isError = urlInput.isBlank(),
            )

            Button(
                onClick = { save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = urlInput.isNotBlank(),
            ) {
                Text("Save")
            }

            if (saved) {
                Text(
                    text = "Saved. The next API call will use the new URL.",
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Text("Validation rules:", style = MaterialTheme.typography.labelSmall)
            Text(
                text = "• Must start with http:// or https://\n• Must end with a trailing slash /\n• Cleartext HTTP is allowed for local dev",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
