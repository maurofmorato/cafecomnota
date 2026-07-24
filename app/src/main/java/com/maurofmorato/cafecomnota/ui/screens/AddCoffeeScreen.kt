package com.maurofmorato.cafecomnota.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.data.auth.AuthenticationExpiredException
import com.maurofmorato.cafecomnota.data.coffee.CoffeeCreateRequest
import com.maurofmorato.cafecomnota.data.coffee.CoffeePhotoUpload
import com.maurofmorato.cafecomnota.data.coffee.SupabaseCoffeePhotoRepository
import com.maurofmorato.cafecomnota.data.coffee.SupabaseCoffeeWriteRepository
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.components.SubScreenHero
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import com.maurofmorato.cafecomnota.ui.theme.CoffeeText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.io.File
import java.text.Normalizer
import java.util.UUID

@Composable
fun AddCoffeeScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    authSession: AuthSession?,
    isAdmin: Boolean,
    existingCoffees: List<CoffeeUiModel>,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onOpenExistingCoffee: (String) -> Unit,
    onRequireLogin: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val coffeeWriteRepository = remember {
        SupabaseCoffeeWriteRepository()
    }
    val coffeePhotoRepository = remember {
        SupabaseCoffeePhotoRepository()
    }

    var coffeeName by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("") }
    var roast by rememberSaveable { mutableStateOf("") }
    var standardWeightText by rememberSaveable { mutableStateOf("250") }
    var producer by rememberSaveable { mutableStateOf("") }
    var originRegion by rememberSaveable { mutableStateOf("") }
    var altitudeText by rememberSaveable { mutableStateOf("") }
    var variety by rememberSaveable { mutableStateOf("") }
    var process by rememberSaveable { mutableStateOf("") }
    var aromaFlavor by rememberSaveable { mutableStateOf("") }
    var certification by rememberSaveable { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isReadingLabel by remember { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf("") }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingPhotoLabel by remember { mutableStateOf(LabelPhotoLabel.Front) }
    var pendingPhotoId by remember { mutableStateOf<String?>(null) }
    var selectedPhotoLabel by rememberSaveable { mutableStateOf(LabelPhotoLabel.Front) }
    var labelPhotos by remember { mutableStateOf<List<LabelPhotoDraft>>(emptyList()) }
    var lastSuggestion by remember { mutableStateOf<CoffeeLabelSuggestion?>(null) }

    fun applyCombinedSuggestion(photos: List<LabelPhotoDraft>) {
        val combinedText = photos
            .map { it.ocrText }
            .filter { it.isNotBlank() }
            .joinToString("\n")
        val suggestion = coffeeLabelSuggestion(
            text = combinedText,
            existingCoffees = existingCoffees
        )
        lastSuggestion = suggestion

        coffeeName = suggestion.name
        brand = suggestion.brand
        standardWeightText = suggestion.weightGrams?.toString().orEmpty()
        type = suggestion.type.orEmpty()
        roast = suggestion.roast.orEmpty()
        producer = suggestion.producer.orEmpty()
        originRegion = suggestion.originRegion.orEmpty()
        altitudeText = suggestion.altitudeMeters?.toString().orEmpty()
        variety = suggestion.variety.orEmpty()
        process = suggestion.process.orEmpty()
        aromaFlavor = suggestion.aromaFlavor.orEmpty()
        certification = suggestion.certification.orEmpty()
    }

    val labelCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val photoUri = pendingPhotoUri
        val photoLabel = pendingPhotoLabel
        val photoId = pendingPhotoId

        if (!success || photoUri == null) {
            message = "Não foi possível obter a foto. Tente novamente."
            return@rememberLauncherForActivityResult
        }

        isReadingLabel = true
        message = "Lendo o rótulo..."

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(InputImage.fromFilePath(context, photoUri))
            .addOnSuccessListener { result ->
                val capturedPhoto = LabelPhotoDraft(
                    id = photoId ?: UUID.randomUUID().toString(),
                    label = photoLabel,
                    uri = photoUri,
                    ocrText = result.text
                )
                val updatedPhotos = if (photoId == null) {
                    labelPhotos + capturedPhoto
                } else {
                    labelPhotos.map { photo ->
                        if (photo.id == photoId) capturedPhoto else photo
                    }
                }
                labelPhotos = updatedPhotos
                applyCombinedSuggestion(updatedPhotos)

                message = if (result.text.isBlank()) {
                    "Não identifiquei texto suficiente nesta foto. Você pode refazê-la ou preencher manualmente."
                } else {
                    "Leitura ${photoLabel.label.lowercase()} concluída. Confira as sugestões antes de salvar."
                }
            }
            .addOnFailureListener {
                message = "Não consegui ler este rótulo. Tente uma foto mais nítida."
            }
            .addOnCompleteListener {
                recognizer.close()
                isReadingLabel = false
            }
    }

    val normalizedName = normalizeForSearch(coffeeName)
    val statusToSave = if (isAdmin) "ativo" else "pendente"
    val possibleDuplicate = remember(coffeeName, brand, existingCoffees) {
        findPotentialDuplicate(
            coffeeName = coffeeName,
            brand = brand,
            existingCoffees = existingCoffees
        )
    }

    CafeResponsiveContent(innerPadding = innerPadding) {
        SubScreenHero(
            strings = strings,
            title = "Cadastrar café",
            subtitle = "Fotografe o rótulo ou preencha os dados; você sempre confirma antes de enviar.",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (authSession == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CoffeeCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Login necessário",
                        color = CoffeeBrownDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Entre pela tela Perfil para cadastrar novos cafés.",
                        color = CoffeeMuted,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(onClick = { onRequireLogin("Entre para cadastrar um novo café.") }) {
                        Text("Ir para Perfil")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CoffeeCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CoffeeBrown)
                    Column {
                        Text(
                            text = "Ler rótulo pela câmera",
                            color = CoffeeBrownDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Adicione até cinco fotos: frente, verso, laterais ou detalhes do pacote. O app combina as leituras para sugerir a ficha técnica.",
                            color = CoffeeMuted,
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tipo da próxima foto",
                    color = CoffeeBrownDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabelPhotoLabel.entries.forEach { label ->
                        FilterChip(
                            selected = selectedPhotoLabel == label,
                            onClick = { selectedPhotoLabel = label },
                            label = { Text(label.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    labelPhotos.forEach { photo ->
                        LabelPhotoCard(
                            modifier = Modifier.width(146.dp),
                            title = photo.label.label,
                            photoUri = photo.uri,
                            context = context,
                            enabled = !isReadingLabel,
                            onClick = {
                                val photoUri = createLabelPhotoUri(context, photo.label)
                                pendingPhotoLabel = photo.label
                                pendingPhotoId = photo.id
                                pendingPhotoUri = photoUri
                                labelCameraLauncher.launch(photoUri)
                            },
                            onRemove = {
                                labelPhotos = labelPhotos.filterNot { it.id == photo.id }
                                applyCombinedSuggestion(labelPhotos)
                            }
                        )
                    }

                    if (labelPhotos.size < MAX_LABEL_PHOTOS) {
                        LabelPhotoCard(
                            modifier = Modifier.width(146.dp),
                            title = "Adicionar ${selectedPhotoLabel.label}",
                            photoUri = null,
                            context = context,
                            enabled = !isReadingLabel,
                            onClick = {
                                val photoUri = createLabelPhotoUri(context, selectedPhotoLabel)
                                pendingPhotoLabel = selectedPhotoLabel
                                pendingPhotoId = null
                                pendingPhotoUri = photoUri
                                labelCameraLauncher.launch(photoUri)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${labelPhotos.size}/$MAX_LABEL_PHOTOS fotos. A primeira será a capa quando o café for aprovado.",
                    color = CoffeeMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                if (isReadingLabel) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lendo ${pendingPhotoLabel.label.lowercase()} do pacote...",
                        color = CoffeeBrown,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                lastSuggestion?.let { suggestion ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = suggestion.summary(),
                        color = CoffeeBrown,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "A câmera sugere; você confirma. Corrija qualquer campo que não corresponda ao pacote.",
                        color = CoffeeMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CoffeeCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ficha técnica do rótulo",
                    color = CoffeeBrownDark,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Campos opcionais. Aproveite somente o que estiver legível na embalagem e corrija as sugestões quando necessário.",
                    color = CoffeeMuted,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LabelDetailField(
                    value = aromaFlavor,
                    onValueChange = { aromaFlavor = it },
                    label = "Aromas e sabores declarados",
                    placeholder = "Ex.: chocolate, caramelo e frutas amarelas"
                )

                LabelDetailField(
                    value = originRegion,
                    onValueChange = { originRegion = it },
                    label = "Origem / região",
                    placeholder = "Ex.: Cerrado Mineiro, MG"
                )

                LabelDetailField(
                    value = producer,
                    onValueChange = { producer = it },
                    label = "Produtor / fazenda",
                    placeholder = "Quando informado no pacote"
                )

                LabelDetailField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = "Variedade",
                    placeholder = "Ex.: Bourbon Amarelo, 100% Arábica"
                )

                LabelDetailField(
                    value = process,
                    onValueChange = { process = it },
                    label = "Processo",
                    placeholder = "Ex.: natural, honey ou lavado"
                )

                LabelDetailField(
                    value = altitudeText,
                    onValueChange = { typed -> altitudeText = typed.filter(Char::isDigit).take(4) },
                    label = "Altitude em metros",
                    placeholder = "Ex.: 1100"
                )

                LabelDetailField(
                    value = certification,
                    onValueChange = { certification = it },
                    label = "Certificação",
                    placeholder = "Ex.: ABIC, orgânico, Rainforest Alliance",
                    addBottomSpacing = false
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CoffeeCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.LocalCafe, contentDescription = null, tint = CoffeeBrown)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Dados principais",
                    color = CoffeeBrownDark,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "O nome exibido preserva acentos. A versão sem acento fica apenas para busca interna.",
                    color = CoffeeMuted,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = coffeeName,
                    onValueChange = { coffeeName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nome do café") },
                    placeholder = { Text("Ex.: Café moído especial") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = CoffeeGold,
                        unfocusedIndicatorColor = CoffeeLine,
                        cursorColor = CoffeeBrown
                    )
                )

                if (possibleDuplicate != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CoffeeGold.copy(alpha = 0.13f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Encontramos um café parecido",
                                color = CoffeeBrownDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${possibleDuplicate.name} • ${possibleDuplicate.brand}",
                                color = CoffeeMuted,
                                fontSize = 13.sp,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { onOpenExistingCoffee(possibleDuplicate.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver café existente")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Marca / torrefação") },
                    placeholder = { Text("Ex.: Café do Mauro") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = CoffeeGold,
                        unfocusedIndicatorColor = CoffeeLine,
                        cursorColor = CoffeeBrown
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = standardWeightText,
                    onValueChange = { typed ->
                        standardWeightText = typed.filter { it.isDigit() }.take(4)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Peso padrão em gramas") },
                    placeholder = { Text("Ex.: 250") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = CoffeeGold,
                        unfocusedIndicatorColor = CoffeeLine,
                        cursorColor = CoffeeBrown
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Tipo", color = CoffeeBrownDark, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = type == "moido",
                            onClick = { type = "moido" },
                            label = { Text("Moído") }
                        )

                        FilterChip(
                            selected = type == "grao",
                            onClick = { type = "grao" },
                            label = { Text("Grãos") }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = type == "capsula",
                            onClick = { type = "capsula" },
                            label = { Text("Cápsula") }
                        )
                    }
                }

                if (type.isBlank()) {
                    Text(
                        text = "Não identificado. Selecione o tipo indicado no pacote.",
                        color = CoffeeMuted,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Torra", color = CoffeeBrownDark, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = roast == "media",
                        onClick = { roast = "media" },
                        label = { Text("Média") }
                    )

                    FilterChip(
                        selected = roast == "escura",
                        onClick = { roast = "escura" },
                        label = { Text("Escura") }
                    )
                }

                if (roast.isBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Não identificada. Selecione a torra antes de salvar.",
                        color = CoffeeMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CoffeeCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = CoffeeBrown)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Prévia",
                    color = CoffeeBrownDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Nome exibido: ${coffeeName.ifBlank { "—" }}",
                    color = CoffeeText,
                    fontSize = 15.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Busca interna: ${normalizedName.ifBlank { "—" }}",
                    color = CoffeeMuted,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isAdmin) {
                        "Status após salvar: ativo, por ser administrador."
                    } else {
                        "Status após salvar: pendente, aguardando moderação."
                    },
                    color = CoffeeMuted,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val session = authSession

                if (session == null) {
                    onRequireLogin("Entre para cadastrar um novo café.")
                    return@Button
                }

                if (isSaving) {
                    return@Button
                }

                val name = coffeeName.trim()
                val brandName = brand.trim()
                val standardWeight = standardWeightText.toIntOrNull() ?: 0

                val validationMessage = validateCoffeeForm(
                    name = name,
                    brand = brandName,
                    standardWeight = standardWeight,
                    type = type,
                    roast = roast
                )

                if (validationMessage != null) {
                    message = validationMessage
                    return@Button
                }

                isSaving = true
                message = "Salvando café..."

                CafeAnalytics.logEvent(
                    eventName = AnalyticsEvents.SAVE_NEW_COFFEE_TAP,
                    params = mapOf(
                        "name_length" to name.length,
                        "brand_length" to brandName.length,
                        "type" to type,
                        "roast" to roast,
                        "weight" to standardWeight,
                        "status" to statusToSave,
                        "is_admin" to isAdmin,
                        "preserves_accents" to true
                    )
                )

                coroutineScope.launch {
                    try {
                        val photosToUpload = labelPhotos.map { photo ->
                            CoffeePhotoUpload(
                                uri = photo.uri,
                                label = photo.label.storageValue
                            )
                        }

                        val coffeeId = coffeeWriteRepository.createCoffee(
                            request = CoffeeCreateRequest(
                                name = name,
                                brand = brandName,
                                type = type,
                                roast = roast,
                                standardWeightGrams = standardWeight,
                                producer = producer.trim().ifBlank { null },
                                originRegion = originRegion.trim().ifBlank { null },
                                altitudeMeters = altitudeText.toIntOrNull(),
                                variety = variety.trim().ifBlank { null },
                                process = process.trim().ifBlank { null },
                                aromaFlavor = aromaFlavor.trim().ifBlank { null },
                                certification = certification.trim().ifBlank { null },
                                userId = session.userId,
                                accessToken = session.accessToken,
                                status = statusToSave,
                                photos = photosToUpload
                            )
                        )

                        if (photosToUpload.isNotEmpty()) {
                            if (coffeeId.isBlank()) {
                                throw IllegalStateException(
                                    "O café foi salvo, mas não foi possível identificar o cadastro para enviar as fotos."
                                )
                            }

                            coffeePhotoRepository.uploadPhotos(
                                context = context,
                                coffeeId = coffeeId,
                                userId = session.userId,
                                accessToken = session.accessToken,
                                photos = photosToUpload
                            )
                        }

                        CafeAnalytics.logEvent(
                            eventName = "save_new_coffee_success",
                            params = mapOf(
                                "type" to type,
                                "roast" to roast,
                                "status" to statusToSave,
                                "is_admin" to isAdmin
                            )
                        )

                        message = if (isAdmin) {
                            "Café salvo e publicado."
                        } else {
                            "Café enviado para moderação."
                        }

                        onSaved()
                    } catch (throwable: AuthenticationExpiredException) {
                        onRequireLogin(throwable.message ?: "Sua sessão expirou. Entre novamente para continuar.")
                    } catch (throwable: Throwable) {
                        CafeAnalytics.recordNonFatal(
                            throwable = throwable,
                            params = mapOf(
                                "screen" to "add_coffee",
                                "action" to "save_new_coffee"
                            )
                        )

                        CafeAnalytics.logEvent(
                            eventName = "save_new_coffee_error",
                            params = mapOf(
                                "message" to (throwable.message ?: "erro")
                            )
                        )

                        message = throwable.message ?: "Não foi possível salvar o café."
                    } finally {
                        isSaving = false
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Text(
                text = if (isSaving) "Salvando..." else "Salvar café",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, color = CoffeeBrown, fontSize = 14.sp, lineHeight = 18.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

private const val MAX_LABEL_PHOTOS = 5

private enum class LabelPhotoLabel(
    val label: String,
    val storageValue: String
) {
    Front("Frente", "frente"),
    Back("Verso", "verso"),
    Side("Lateral", "lateral"),
    Information("Informações", "informacoes"),
    Barcode("Código de barras", "codigo_barras"),
    Other("Outra", "outra")
}

private data class LabelPhotoDraft(
    val id: String,
    val label: LabelPhotoLabel,
    val uri: Uri,
    val ocrText: String
)

@Composable
private fun LabelPhotoCard(
    modifier: Modifier,
    title: String,
    photoUri: Uri?,
    context: Context,
    enabled: Boolean,
    onClick: () -> Unit,
    onRemove: (() -> Unit)? = null
) {
    val preview = remember(photoUri) {
        photoUri?.let { loadPhotoPreview(context, it) }
    }

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeGold.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CoffeeLine.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                if (preview != null) {
                    Image(
                        bitmap = preview.asImageBitmap(),
                        contentDescription = "Foto da ${title.lowercase()} do pacote",
                        modifier = Modifier.fillMaxWidth().height(94.dp),
                        contentScale = ContentScale.Crop
                    )

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CoffeeGold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(25.dp)
                    )

                    onRemove?.let { remove ->
                        IconButton(
                            onClick = remove,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(1.dp)
                                .size(32.dp)
                                .background(
                                    color = CoffeeCard.copy(alpha = 0.92f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remover foto",
                                tint = CoffeeBrownDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = CoffeeBrown,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (photoUri == null) title else "Refazer $title",
                color = CoffeeBrownDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LabelDetailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    addBottomSpacing: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = CoffeeGold,
            unfocusedIndicatorColor = CoffeeLine,
            cursorColor = CoffeeBrown
        )
    )

    if (addBottomSpacing) {
        Spacer(modifier = Modifier.height(10.dp))
    }
}

private fun loadPhotoPreview(context: Context, uri: Uri) = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    }

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > 900 || bounds.outHeight / sampleSize > 900) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
}.getOrNull()

private fun validateCoffeeForm(
    name: String,
    brand: String,
    standardWeight: Int,
    type: String,
    roast: String
): String? {
    if (name.isBlank()) {
        return "Informe o nome do café."
    }

    if (brand.isBlank()) {
        return "Informe a marca ou torrefação."
    }

    if (name.length < 3) {
        return "O nome do café está muito curto."
    }

    if (brand.length < 2) {
        return "A marca está muito curta."
    }

    if (standardWeight <= 0) {
        return "Informe um peso padrão válido."
    }

    if (standardWeight > 5000) {
        return "Peso muito alto. Confira se está em gramas."
    }

    if (type !in setOf("moido", "grao", "capsula")) {
        return "Selecione o tipo do café."
    }

    if (roast !in setOf("media", "escura")) {
        return "Selecione a torra do café."
    }

    return null
}

private fun normalizeForSearch(value: String): String {
    val withoutAccents = Normalizer
        .normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

    return withoutAccents
        .lowercase()
        .replace("ç", "c")
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")
}

private fun findPotentialDuplicate(
    coffeeName: String,
    brand: String,
    existingCoffees: List<CoffeeUiModel>
): CoffeeUiModel? {
    val normalizedName = normalizeForSearch(coffeeName)
    val normalizedBrand = normalizeForSearch(brand)

    if (normalizedName.length < 4 && normalizedBrand.length < 3) {
        return null
    }

    return existingCoffees.firstOrNull { coffee ->
        val existingName = normalizeForSearch(coffee.name)
        val existingBrand = normalizeForSearch(coffee.brand)
        val sameName = normalizedName.length >= 4 &&
            (existingName == normalizedName || existingName.contains(normalizedName) || normalizedName.contains(existingName))
        val sameBrandAndCloseName = normalizedBrand.length >= 3 &&
            existingBrand == normalizedBrand &&
            normalizedName.split(' ').filter { it.length >= 4 }.any { word ->
                existingName.contains(word)
            }

        sameName || sameBrandAndCloseName
    }
}

internal data class CoffeeLabelSuggestion(
    val name: String,
    val brand: String,
    val weightGrams: Int?,
    val type: String?,
    val roast: String?,
    val producer: String? = null,
    val originRegion: String? = null,
    val altitudeMeters: Int? = null,
    val variety: String? = null,
    val process: String? = null,
    val aromaFlavor: String? = null,
    val certification: String? = null,
    val matchedCatalog: Boolean = false
) {
    fun summary(): String {
        val detected = buildList {
            if (name.isNotBlank()) add("nome: $name")
            if (brand.isNotBlank()) add("marca: $brand")
            weightGrams?.let { add("peso: ${it}g") }
            type?.let { add("tipo: ${if (it == "grao") "grãos" else if (it == "moido") "moído" else "cápsula"}") }
            roast?.let { add("torra: ${if (it == "media") "média" else "escura"}") }
            aromaFlavor?.let { add("perfil: $it") }
            originRegion?.let { add("origem: $it") }
            variety?.let { add("variedade: $it") }
            process?.let { add("processo: $it") }
            certification?.let { add("certificação: $it") }
        }

        return when {
            detected.isEmpty() -> "Nenhum campo reconhecido com segurança."
            matchedCatalog -> "Encontrado no catálogo — ${detected.joinToString(" • ")}"
            else -> "Detectado — ${detected.joinToString(" • ")}"
        }
    }
}

internal fun coffeeLabelSuggestion(
    text: String,
    existingCoffees: List<CoffeeUiModel>
): CoffeeLabelSuggestion {
    val cleanLines = text.lines()
        .map(::cleanOcrLine)
        .filter { it.length >= 3 }
        .distinctBy(::normalizeForSearch)

    val normalizedText = normalizeForSearch(text)

    val weightInGrams = Regex("\\b(\\d{2,4})\\s?(g|gr|gramas)\\b", RegexOption.IGNORE_CASE)
        .find(text)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.takeIf { it in 20..5000 }

    val weightInKg = Regex("\\b(\\d(?:[.,]\\d{1,3})?)\\s?kg\\b", RegexOption.IGNORE_CASE)
        .find(text)
        ?.groupValues
        ?.getOrNull(1)
        ?.replace(',', '.')
        ?.toDoubleOrNull()
        ?.times(1000)
        ?.toInt()
        ?.takeIf { it in 20..5000 }

    val type = when {
        Regex("\\b(capsula|capsulas)\\b").containsMatchIn(normalizedText) -> "capsula"
        Regex("\\b(grao|graos)\\b").containsMatchIn(normalizedText) -> "grao"
        Regex("\\b(moido|moida)\\b").containsMatchIn(normalizedText) -> "moido"
        else -> null
    }

    val roast = when {
        Regex("\\btorra (escura|forte|extraforte|extra forte)\\b").containsMatchIn(normalizedText) -> "escura"
        Regex("\\b(extra forte|extraforte)\\b").containsMatchIn(normalizedText) -> "escura"
        Regex("\\btorra (media|medio)\\b").containsMatchIn(normalizedText) -> "media"
        else -> null
    }

    val technicalDetails = technicalLabelDetails(cleanLines, normalizedText)

    findCatalogCoffee(normalizedText, existingCoffees)?.let { coffee ->
        return CoffeeLabelSuggestion(
            name = coffee.name,
            brand = coffee.brand,
            weightGrams = weightInGrams ?: weightInKg,
            type = technicalCoffeeType(coffee.type) ?: type,
            roast = technicalCoffeeRoast(coffee.roast) ?: roast,
            producer = coffee.producer ?: technicalDetails.producer,
            originRegion = coffee.originRegion ?: technicalDetails.originRegion,
            altitudeMeters = coffee.altitudeMeters ?: technicalDetails.altitudeMeters,
            variety = coffee.variety ?: technicalDetails.variety,
            process = coffee.process ?: technicalDetails.process,
            aromaFlavor = coffee.aromaFlavor ?: technicalDetails.aromaFlavor,
            certification = coffee.certification ?: technicalDetails.certification,
            matchedCatalog = true
        )
    }

    val ignoredTerms = listOf(
        "informacao nutricional", "validade", "lote", "sac", "industria", "fabricado",
        "cafe torrado", "torrado em grao", "torrado e moido", "100 arabica", "peso liquido",
        "especial de verdade", "notas de", "torra media", "torra escura",
        "cafe com", "compre pelo app", "pague por", "baixar app", "desde 18",
        "imagem meramente ilustrativa", "informacoes do produto", "avaliacoes"
    )

    val titleCandidates = cleanLines.filter { line ->
        val normalized = normalizeForSearch(line)
        line.length in 3..42 &&
            ignoredTerms.none(normalized::contains) &&
            !normalized.matches(Regex(".*\\b\\d{2,4}\\s*(g|gr|gramas|kg)\\b.*"))
    }

    val descriptorWords = listOf(
        "bourbon", "intenso", "extra forte", "tradicional", "gourmet", "especial",
        "espresso", "premium", "chocolate", "trufado", "avela", "caramelo",
        "frutado", "classico", "organico", "blend", "cerrado", "mogiana"
    )
    val descriptorLines = titleCandidates.filter { line ->
        val normalized = normalizeForSearch(line)
        descriptorWords.any(normalized::contains)
    }
    val descriptor = descriptorLines
        .take(2)
        .joinToString(" ")
        .replace(Regex("(?i)^(aromas?|sabor|notas? de)\\s+"), "")
        .trim()

    val brand = titleCandidates
        .filter { line ->
            val normalized = normalizeForSearch(line)
            line !in descriptorLines &&
                normalized !in setOf("cafe", "coffee") &&
                descriptorWords.none(normalized::contains)
        }
        .maxByOrNull(::brandCandidateScore)
        ?.let(::smartDisplayText)
        .orEmpty()

    val name = when {
        brand.isBlank() -> descriptor
        descriptor.isBlank() -> brand
        normalizeForSearch(brand).contains(normalizeForSearch(descriptor)) -> brand
        else -> "$brand ${smartDisplayText(descriptor)}"
    }

    return CoffeeLabelSuggestion(
        name = name,
        brand = brand,
        weightGrams = weightInGrams ?: weightInKg,
        type = type,
        roast = roast,
        producer = technicalDetails.producer,
        originRegion = technicalDetails.originRegion,
        altitudeMeters = technicalDetails.altitudeMeters,
        variety = technicalDetails.variety,
        process = technicalDetails.process,
        aromaFlavor = technicalDetails.aromaFlavor,
        certification = technicalDetails.certification
    )
}

private data class TechnicalLabelDetails(
    val producer: String? = null,
    val originRegion: String? = null,
    val altitudeMeters: Int? = null,
    val variety: String? = null,
    val process: String? = null,
    val aromaFlavor: String? = null,
    val certification: String? = null
)

private fun technicalLabelDetails(
    lines: List<String>,
    normalizedText: String
): TechnicalLabelDetails {
    val altitude = Regex("\\b(\\d{3,4})\\s?(m|metros)\\b", RegexOption.IGNORE_CASE)
        .find(normalizedText)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.takeIf { it in 300..3000 }

    val varietyTerms = listOf(
        "bourbon amarelo", "bourbon vermelho", "bourbon", "catucai", "catuai",
        "mundo novo", "geisha", "gesha", "icatu", "arara", "100 arabica", "arabica"
    )
    val processTerms = listOf(
        "cereja descascado", "despolpado", "fermentacao anaerobica", "anaerobico",
        "honey", "natural", "lavado", "washed"
    )
    val sensoryTerms = listOf(
        "chocolate trufado", "chocolate", "caramelo", "avela", "castanhas", "amendoas",
        "frutas amarelas", "frutas vermelhas", "frutado", "floral", "citricos", "mel",
        "acucar mascavo", "baunilha", "cacau", "rapadura"
    )
    val certificationTerms = listOf(
        "abic gourmet", "abic superior", "abic tradicional", "rainforest alliance",
        "fairtrade", "certifica minas", "denominacao de origem", "organico"
    )

    val variety = findTermsInText(normalizedText, varietyTerms)
    val process = findTermsInText(normalizedText, processTerms)
    val aromaFlavor = findTermsInText(normalizedText, sensoryTerms)
    val certification = findTermsInText(normalizedText, certificationTerms)

    val producer = findLabeledValue(lines, listOf("produtor", "fazenda", "sitio", "torrefacao"))
    val origin = findLabeledValue(lines, listOf("origem", "regiao", "procedencia"))

    return TechnicalLabelDetails(
        producer = producer,
        originRegion = origin,
        altitudeMeters = altitude,
        variety = variety,
        process = process,
        aromaFlavor = aromaFlavor,
        certification = certification
    )
}

private fun findTermsInText(text: String, terms: List<String>): String? {
    val matches = terms
        .filter { term -> Regex("(^| )${Regex.escape(term)}( |$)").containsMatchIn(text) }
        .distinct()

    return matches.takeIf { it.isNotEmpty() }
        ?.joinToString(", ") { smartDisplayText(it) }
}

private fun findLabeledValue(lines: List<String>, labels: List<String>): String? {
    lines.forEachIndexed { index, line ->
        val normalized = normalizeForSearch(line)
        val label = labels.firstOrNull { normalized == it || normalized.startsWith("$it ") }
            ?: return@forEachIndexed
        val inlineValue = normalized.removePrefix(label).trim(' ', ':', '-')
        if (inlineValue.length >= 3) {
            return smartDisplayText(inlineValue)
        }

        val nextLine = lines.getOrNull(index + 1).orEmpty()
        if (nextLine.length in 3..80) {
            return smartDisplayText(nextLine)
        }
    }

    return null
}

private val catalogTokenStopWords = setOf(
    "cafe", "cafes", "coffee", "torrado", "torrada", "moido", "moida", "grao", "graos",
    "aroma", "aromas", "sabor", "desde", "especial", "premium", "gourmet"
)

private fun findCatalogCoffee(
    normalizedOcrText: String,
    existingCoffees: List<CoffeeUiModel>
): CoffeeUiModel? {
    val ocrTokens = normalizedOcrText.split(' ').filter { it.isNotBlank() }.toSet()

    return existingCoffees.mapNotNull { coffee ->
        val brandTokens = meaningfulCatalogTokens(coffee.brand)
        val nameTokens = meaningfulCatalogTokens(coffee.name)
        val productTokens = nameTokens - brandTokens

        if (brandTokens.isEmpty() || brandTokens.any { it !in ocrTokens }) {
            return@mapNotNull null
        }

        val productHits = productTokens.count { it in ocrTokens }
        if (productTokens.isNotEmpty() && productHits == 0) {
            return@mapNotNull null
        }

        val coverage = if (productTokens.isEmpty()) 0 else (productHits * 100) / productTokens.size
        coffee to (brandTokens.size * 100 + productHits * 30 + coverage)
    }.maxByOrNull { (_, score) -> score }
        ?.takeIf { (_, score) -> score >= 130 }
        ?.first
}

private fun meaningfulCatalogTokens(value: String): Set<String> = normalizeForSearch(value)
    .split(' ')
    .filter { token -> token.length >= 3 && token !in catalogTokenStopWords }
    .toSet()

private fun technicalCoffeeType(displayValue: String): String? = when {
    normalizeForSearch(displayValue).contains("capsula") -> "capsula"
    normalizeForSearch(displayValue).contains("grao") -> "grao"
    normalizeForSearch(displayValue).contains("moido") -> "moido"
    else -> null
}

private fun technicalCoffeeRoast(displayValue: String): String? = when {
    normalizeForSearch(displayValue).contains("escura") -> "escura"
    normalizeForSearch(displayValue).contains("media") -> "media"
    else -> null
}

private fun brandCandidateScore(value: String): Int {
    val letters = value.filter { it.isLetter() }
    val uppercaseRatio = if (letters.isEmpty()) 0 else letters.count { it.isUpperCase() } * 100 / letters.length
    val wordCount = value.split(' ').count { it.isNotBlank() }
    val conciseBonus = when (wordCount) {
        1 -> 45
        2, 3 -> 30
        else -> 0
    }

    return uppercaseRatio + conciseBonus - value.length.coerceAtMost(40)
}

private fun smartDisplayText(value: String): String {
    val letters = value.filter { it.isLetter() }
    if (letters.isEmpty() || letters.any { it.isLowerCase() }) return value.trim()

    return value.lowercase().split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { first -> first.titlecase() }
    }.trim()
}

private fun cleanOcrLine(value: String): String = value
    .replace(Regex("[^\\p{L}\\p{N}%+.,'& -]"), " ")
    .replace(Regex("\\s+"), " ")
    .trim(' ', '-', '.', ',')

private fun createLabelPhotoUri(context: Context, label: LabelPhotoLabel): Uri {
    val photoDirectory = File(context.cacheDir, "label_photos").apply { mkdirs() }
    val photoFile = File.createTempFile(
        "coffee_label_${label.storageValue}_",
        ".jpg",
        photoDirectory
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}
