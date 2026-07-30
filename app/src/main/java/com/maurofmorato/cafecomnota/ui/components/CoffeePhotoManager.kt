package com.maurofmorato.cafecomnota.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.data.coffee.CoffeePhotoRules
import com.maurofmorato.cafecomnota.data.coffee.CoffeePhotoUpload
import com.maurofmorato.cafecomnota.data.coffee.CoffeeStoredPhoto
import com.maurofmorato.cafecomnota.data.coffee.SupabaseCoffeePhotoRepository
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CoffeePhotoManager(
    coffeeId: String,
    authSession: AuthSession,
    onChanged: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { SupabaseCoffeePhotoRepository() }
    var photos by remember(coffeeId) { mutableStateOf<List<CoffeeStoredPhoto>>(emptyList()) }
    var loading by remember(coffeeId) { mutableStateOf(true) }
    var working by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var pendingOrder by remember { mutableStateOf<Int?>(null) }
    var pendingLabel by remember { mutableStateOf("outra") }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var deleteCandidate by remember { mutableStateOf<CoffeeStoredPhoto?>(null) }

    suspend fun reload() {
        loading = true
        photos = runCatching {
            repository.loadPhotos(coffeeId, authSession.accessToken)
        }.onFailure {
            message = "Não foi possível carregar as fotos agora."
        }.getOrDefault(emptyList())
        loading = false
    }

    fun save(uri: Uri?) {
        val order = pendingOrder
        if (uri == null || order == null) {
            message = "Nenhuma foto foi escolhida."
            return
        }
        working = true
        scope.launch {
            try {
                repository.savePhoto(
                    context = context,
                    coffeeId = coffeeId,
                    userId = authSession.userId,
                    accessToken = authSession.accessToken,
                    photo = CoffeePhotoUpload(uri = uri, label = pendingLabel),
                    order = order
                )
                message = "Foto salva com sucesso."
                reload()
                onChanged()
            } catch (_: Throwable) {
                message = "Não foi possível salvar a foto. Confira a conexão e tente novamente."
            } finally {
                working = false
                pendingOrder = null
                pendingCameraUri = null
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) save(pendingCameraUri) else message = "A foto não foi concluída."
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> save(uri) }

    fun openCamera(order: Int, label: String) {
        pendingOrder = order
        pendingLabel = label
        createManagementPhotoUri(context).also { uri ->
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun openGallery(order: Int, label: String) {
        pendingOrder = order
        pendingLabel = label
        galleryLauncher.launch("image/*")
    }

    LaunchedEffect(coffeeId, authSession.accessToken) {
        reload()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Fotos da embalagem",
                color = CoffeeBrownDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "A foto frontal aparece primeiro nas listas e no ranking.",
                color = CoffeeMuted,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(12.dp))

            if (loading) {
                Text("Carregando fotos…", color = CoffeeMuted)
            } else {
                photos.forEach { photo ->
                    StoredPhotoRow(
                        photo = photo,
                        accessToken = authSession.accessToken,
                        enabled = !working,
                        onCamera = { openCamera(photo.order, photo.label) },
                        onGallery = { openGallery(photo.order, photo.label) },
                        onMakeFront = {
                            working = true
                            scope.launch {
                                try {
                                    repository.makeFrontPhoto(
                                        coffeeId,
                                        photo.id,
                                        authSession.accessToken
                                    )
                                    message = "Foto frontal atualizada."
                                    reload()
                                    onChanged()
                                } catch (_: Throwable) {
                                    message = "Não foi possível definir a foto frontal."
                                } finally {
                                    working = false
                                }
                            }
                        },
                        onDelete = { deleteCandidate = photo }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                val usedOrders = photos.mapTo(mutableSetOf()) { it.order }
                val nextOrder = (0 until CoffeePhotoRules.MAX_PHOTOS)
                    .firstOrNull { it !in usedOrders }
                if (nextOrder != null) {
                    val newLabel = if (photos.isEmpty()) "frente" else "outra"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { openCamera(nextOrder, newLabel) },
                            enabled = !working,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Câmera")
                        }
                        OutlinedButton(
                            onClick = { openGallery(nextOrder, newLabel) },
                            enabled = !working,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Galeria")
                        }
                    }
                } else {
                    Text(
                        "Limite de cinco fotos atingido.",
                        color = CoffeeMuted,
                        fontSize = 13.sp
                    )
                }
            }

            if (message.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                CafeMessageCard(message)
                TextButton(onClick = { message = "" }) {
                    Text("Fechar aviso")
                }
            }
        }
    }

    deleteCandidate?.let { photo ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Remover esta foto?") },
            text = { Text("A imagem deixará de aparecer no aplicativo.") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteCandidate = null
                        working = true
                        scope.launch {
                            try {
                                repository.deletePhoto(
                                    coffeeId,
                                    photo,
                                    authSession.accessToken
                                )
                                message = "Foto removida."
                                reload()
                                onChanged()
                            } catch (_: Throwable) {
                                message = "Não foi possível remover a foto."
                            } finally {
                                working = false
                            }
                        }
                    }
                ) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun StoredPhotoRow(
    photo: CoffeeStoredPhoto,
    accessToken: String,
    enabled: Boolean,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onMakeFront: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoffeePackageImage(
            imagePath = photo.storagePath,
            label = photoLabel(photo.label),
            accessToken = accessToken,
            modifier = Modifier.size(width = 74.dp, height = 96.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                photoLabel(photo.label),
                color = CoffeeBrownDark,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                TextButton(onClick = onCamera, enabled = enabled) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                }
                TextButton(onClick = onGallery, enabled = enabled) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                }
                if (!photo.label.equals("frente", ignoreCase = true)) {
                    TextButton(onClick = onMakeFront, enabled = enabled) {
                        Icon(Icons.Default.Star, contentDescription = "Usar como frontal")
                    }
                }
                TextButton(onClick = onDelete, enabled = enabled) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover foto")
                }
            }
        }
    }
}

private fun photoLabel(value: String): String = when (value.lowercase()) {
    "frente" -> "Foto frontal"
    "verso" -> "Verso"
    "lateral" -> "Lateral"
    "informacoes" -> "Informações"
    "codigo_barras" -> "Código de barras"
    else -> "Outra foto"
}

private fun createManagementPhotoUri(context: Context): Uri {
    val directory = File(context.cacheDir, "label_photos").apply { mkdirs() }
    val file = File.createTempFile("coffee_manage_", ".jpg", directory)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}
