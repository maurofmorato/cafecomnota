package com.maurofmorato.cafecomnota.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.data.coffee.CoffeeCreateRequest
import com.maurofmorato.cafecomnota.data.coffee.SupabaseCoffeeWriteRepository
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import com.maurofmorato.cafecomnota.ui.theme.CoffeeText
import kotlinx.coroutines.launch
import java.text.Normalizer

@Composable
fun AddCoffeeScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    authSession: AuthSession?,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
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
    var message by remember { mutableStateOf("") }

    val normalizedName = normalizeForSearch(coffeeName)
    val statusToSave = if (isAdmin) "ativo" else "pendente"

    CafeResponsiveContent(innerPadding = innerPadding) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = strings.commonBack, tint = CoffeeBrown)
        }

        CafeHeader(strings = strings, compact = true)

        Spacer(modifier = Modifier.height(18.dp))
        SectionTitle(title = "Cadastrar café")
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
                    message = "Entre na conta pelo Perfil antes de cadastrar café."
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
