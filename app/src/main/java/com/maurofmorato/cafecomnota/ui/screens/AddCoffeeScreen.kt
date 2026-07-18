package com.maurofmorato.cafecomnota.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.data.auth.AuthenticationExpiredException
import com.maurofmorato.cafecomnota.data.coffee.CoffeeCreateRequest
import com.maurofmorato.cafecomnota.data.coffee.SupabaseCoffeeWriteRepository
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.components.SubScreenHero
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
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

@Composable
fun AddCoffeeScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    authSession: AuthSession?,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onRequireLogin: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val coffeeWriteRepository = remember {
        SupabaseCoffeeWriteRepository()
    }

    var coffeeName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("moido") }
    var roast by remember { mutableStateOf("media") }
    var standardWeightText by remember { mutableStateOf("250") }
    var isSaving by remember { mutableStateOf(false) }
    var isReadingLabel by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var lastSuggestion by remember { mutableStateOf<CoffeeLabelSuggestion?>(null) }

    val labelCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val photoUri = pendingPhotoUri

        if (!success || photoUri == null) {
            message = "Não foi possível obter a foto. Tente novamente."
            return@rememberLauncherForActivityResult
        }

        isReadingLabel = true
        message = "Lendo o rótulo..."

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(InputImage.fromFilePath(context, photoUri))
            .addOnSuccessListener { result ->
                val suggestion = coffeeLabelSuggestion(result.text)
                lastSuggestion = suggestion

                if (suggestion.name.isNotBlank()) coffeeName = suggestion.name
                if (suggestion.brand.isNotBlank()) brand = suggestion.brand
                suggestion.weightGrams?.let { standardWeightText = it.toString() }
                suggestion.type?.let { type = it }
                suggestion.roast?.let { roast = it }

                message = if (result.text.isBlank()) {
                    "Não identifiquei texto suficiente. Preencha os campos manualmente."
                } else {
                    "Sugestões preenchidas. Confira cada campo antes de salvar."
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
                            text = "A foto é analisada apenas no aparelho para sugerir nome, marca e peso.",
                            color = CoffeeMuted,
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val photoUri = createLabelPhotoUri(context)
                        pendingPhotoUri = photoUri
                        labelCameraLauncher.launch(photoUri)
                    },
                    enabled = !isReadingLabel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Text(
                        text = if (isReadingLabel) "Lendo rótulo..." else "Fotografar rótulo",
                        modifier = Modifier.padding(start = 8.dp)
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
                val standardWeight = standardWeightText.toIntOrNull() ?: 250

                val validationMessage = validateCoffeeForm(
                    name = name,
                    brand = brandName,
                    standardWeight = standardWeight
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
                        coffeeWriteRepository.createCoffee(
                            request = CoffeeCreateRequest(
                                name = name,
                                brand = brandName,
                                type = type,
                                roast = roast,
                                standardWeightGrams = standardWeight,
                                userId = session.userId,
                                accessToken = session.accessToken,
                                status = statusToSave
                            )
                        )

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

private fun validateCoffeeForm(
    name: String,
    brand: String,
    standardWeight: Int
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

private data class CoffeeLabelSuggestion(
    val name: String,
    val brand: String,
    val weightGrams: Int?,
    val type: String?,
    val roast: String?
) {
    fun summary(): String {
        val detected = buildList {
            if (name.isNotBlank()) add("nome: $name")
            if (brand.isNotBlank()) add("marca: $brand")
            weightGrams?.let { add("peso: ${it}g") }
            type?.let { add("tipo: ${if (it == "grao") "grãos" else if (it == "moido") "moído" else "cápsula"}") }
            roast?.let { add("torra: ${if (it == "media") "média" else "escura"}") }
        }

        return if (detected.isEmpty()) "Nenhum campo reconhecido com segurança." else "Detectado — ${detected.joinToString(" • ")}"
    }
}

private fun coffeeLabelSuggestion(text: String): CoffeeLabelSuggestion {
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
        Regex("\\b(torra )?(escura|forte|extraforte|extra forte)\\b").containsMatchIn(normalizedText) -> "escura"
        Regex("\\b(torra )?(media|medio)\\b").containsMatchIn(normalizedText) -> "media"
        else -> null
    }

    val ignoredTerms = listOf(
        "informacao nutricional", "validade", "lote", "sac", "industria", "fabricado",
        "cafe torrado", "torrado em grao", "torrado e moido", "100 arabica", "peso liquido",
        "especial de verdade", "notas de", "torra media", "torra escura"
    )

    val titleCandidates = cleanLines.filter { line ->
        val normalized = normalizeForSearch(line)
        line.length in 3..42 &&
            ignoredTerms.none(normalized::contains) &&
            !normalized.matches(Regex(".*\\b\\d{2,4}\\s*(g|gr|gramas|kg)\\b.*"))
    }

    val descriptorWords = listOf(
        "bourbon", "intenso", "extra forte", "tradicional", "gourmet", "especial", "espresso", "premium"
    )
    val coffeeLineIndex = titleCandidates.indexOfFirst { line ->
        normalizeForSearch(line).contains("cafe")
    }
    val coffeeLine = titleCandidates.getOrNull(coffeeLineIndex).orEmpty()
    val lineAfterCoffee = titleCandidates.getOrNull(coffeeLineIndex + 1).orEmpty()
    val brand = when {
        normalizeForSearch(coffeeLine) == "cafe" &&
            lineAfterCoffee.isNotBlank() &&
            descriptorWords.none(normalizeForSearch(lineAfterCoffee)::contains) -> "$coffeeLine $lineAfterCoffee"
        coffeeLine.isNotBlank() -> coffeeLine
        else -> titleCandidates.firstOrNull().orEmpty()
    }

    val descriptor = titleCandidates.firstOrNull { line ->
        val normalized = normalizeForSearch(line)
        line != brand && descriptorWords.any(normalized::contains)
    }.orEmpty()

    val name = when {
        brand.isBlank() -> descriptor
        descriptor.isBlank() -> brand
        normalizeForSearch(brand).contains(normalizeForSearch(descriptor)) -> brand
        else -> "$brand $descriptor"
    }

    return CoffeeLabelSuggestion(
        name = name,
        brand = brand,
        weightGrams = weightInGrams ?: weightInKg,
        type = type,
        roast = roast
    )
}

private fun cleanOcrLine(value: String): String = value
    .replace(Regex("[^\\p{L}\\p{N}%+.,'& -]"), " ")
    .replace(Regex("\\s+"), " ")
    .trim(' ', '-', '.', ',')

private fun createLabelPhotoUri(context: Context): Uri {
    val photoDirectory = File(context.cacheDir, "label_photos").apply { mkdirs() }
    val photoFile = File.createTempFile("coffee_label_", ".jpg", photoDirectory)

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}
