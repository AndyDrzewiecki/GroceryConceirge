package com.homeops.grocery.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.homeops.grocery.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    // Pending capture metadata – set before launching the camera intent.
    var pendingFile by remember { mutableStateOf<File?>(null) }
    var pendingLocation by remember { mutableStateOf<String?>(null) }
    var pendingIsReceipt by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val file = pendingFile ?: return@rememberLauncherForActivityResult
            isUploading = true
            statusMessage = "Uploading…"
            scope.launch(Dispatchers.IO) {
                val result = uploadFile(file, pendingLocation, pendingIsReceipt)
                withContext(Dispatchers.Main) {
                    isUploading = false
                    statusMessage = result
                }
            }
        } else {
            statusMessage = "Capture cancelled."
        }
    }

    fun launchCamera(location: String?, isReceipt: Boolean) {
        val (file, uri) = createTempImageFile(context)
        pendingFile = file
        pendingLocation = location
        pendingIsReceipt = isReceipt
        cameraLauncher.launch(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capture Photo") },
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
            Text("Select what you are photographing:", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = { launchCamera("FRIDGE", false) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
            ) { Text("Fridge") }

            Button(
                onClick = { launchCamera("FREEZER", false) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
            ) { Text("Freezer") }

            Button(
                onClick = { launchCamera("PANTRY", false) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
            ) { Text("Pantry") }

            OutlinedButton(
                onClick = { launchCamera(null, true) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
            ) { Text("Receipt") }

            if (isUploading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            if (statusMessage.isNotEmpty()) {
                Text(statusMessage)
            }
        }
    }
}

private fun createTempImageFile(context: Context): Pair<File, Uri> {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File.createTempFile("IMG_${timestamp}_", ".jpg", dir)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    return file to uri
}

private suspend fun uploadFile(file: File, location: String?, isReceipt: Boolean): String {
    return try {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val api = RetrofitClient.instance
        val response = if (isReceipt) {
            api.uploadReceiptPhoto(part)
        } else {
            api.uploadInventoryPhoto(location ?: "FRIDGE", part)
        }
        if (response.isSuccessful) "Upload successful (id=${response.body()?.id})" else "Upload failed: HTTP ${response.code()}"
    } catch (e: Exception) {
        "Upload error: ${e.message}"
    }
}
