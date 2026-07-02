package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
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
import java.text.Normalizer

@Composable
fun AddCoffeeScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    onBack: () -> Unit
) {
    val coffeeName = remember { mutableStateOf("") }
    val brand = remember { mutableStateOf("") }
    val type = remember { mutableStateOf("moido") }
    val roast = remember { mutableStateOf("media") }
    val message = remember { mutableStateOf("") }

    CafeResponsiveContent(innerPadding = innerPadding) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = strings.commonBack, tint = CoffeeBrown)
        }

        CafeHeader(strings = strings, compact = true)

        Spacer(modifier = Modifier.height(22.dp))
        SectionTitle(title = "Cadastrar café")
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CoffeeCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.LocalCafe, contentDescription = null, tint = CoffeeBrown)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Dados principais",
                    color = CoffeeBrownDark,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "O nome exibido preserva acentos. A versão sem acento fica apenas para busca interna.",
                    color = CoffeeMuted,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = coffeeName.value,
                    onValueChange = { coffeeName.value = it },
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
                    value = brand.value,
                    onValueChange = { brand.value = it },
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

                Spacer(modifier = Modifier.height(16.dp))

                Text("Tipo", color = CoffeeBrownDark, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.foundation.layout.Row {
                    FilterChip(
                        selected = type.value == "moido",
                        onClick = { type.value = "moido" },
                        label = { Text("Moído") }
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    FilterChip(
                        selected = type.value == "grao",
                        onClick = { type.value = "grao" },
                        label = { Text("Grãos") }
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    FilterChip(
                        selected = type.value == "capsula",
                        onClick = { type.value = "capsula" },
                        label = { Text("Cápsula") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Torra", color = CoffeeBrownDark, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.foundation.layout.Row {
                    FilterChip(
                        selected = roast.value == "media",
                        onClick = { roast.value = "media" },
                        label = { Text("Média") }
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    FilterChip(
                        selected = roast.value == "escura",
                        onClick = { roast.value = "escura" },
                        label = { Text("Escura") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CoffeeCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
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
                    text = "Nome exibido: ${coffeeName.value.ifBlank { "—" }}",
                    color = CoffeeText,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Busca interna: ${normalizeForSearch(coffeeName.value).ifBlank { "—" }}",
                    color = CoffeeMuted,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val name = coffeeName.value.trim()
                val brandName = brand.value.trim()

                if (name.isBlank()) {
                    message.value = "Informe o nome do café."
                    return@Button
                }

                if (brandName.isBlank()) {
                    message.value = "Informe a marca ou torrefação."
                    return@Button
                }

                CafeAnalytics.logEvent(
                    eventName = AnalyticsEvents.SAVE_NEW_COFFEE_TAP,
                    params = mapOf(
                        "name_length" to name.length,
                        "brand_length" to brandName.length,
                        "type" to type.value,
                        "roast" to roast.value,
                        "preserves_accents" to true
                    )
                )

                message.value = "Cadastro preservado na tela. Na próxima etapa vamos salvar no Supabase mantendo os acentos."
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Text(text = "Salvar café", modifier = Modifier.padding(start = 8.dp))
        }

        if (message.value.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message.value, color = CoffeeBrown, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
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
