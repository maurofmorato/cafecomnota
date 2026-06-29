package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted

@Composable
fun AddCoffeeScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit
) {
    val coffeeName = remember { mutableStateOf("") }
    val brand = remember { mutableStateOf("") }
    val type = remember { mutableStateOf("") }
    val roast = remember { mutableStateOf("") }
    val defaultWeight = remember { mutableStateOf("250") }
    val barcode = remember { mutableStateOf("") }

    CafeResponsiveContent(
        innerPadding = innerPadding
    ) {
        IconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = CoffeeBrown
            )
        }

        CafeHeader(compact = true)

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Cadastrar café novo")

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = CoffeeCard
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Dados básicos do café",
                    color = CoffeeBrown,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Cadastro ainda local. Depois vamos salvar no Supabase.",
                    color = CoffeeMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                CafeTextField(
                    value = coffeeName.value,
                    onValueChange = {
                        coffeeName.value = it
                    },
                    label = "Nome do café",
                    placeholder = "Ex.: Gourmet Cerrado Mineiro"
                )

                Spacer(modifier = Modifier.height(12.dp))

                CafeTextField(
                    value = brand.value,
                    onValueChange = {
                        brand.value = it
                    },
                    label = "Marca ou torrefação",
                    placeholder = "Ex.: 3 Corações"
                )

                Spacer(modifier = Modifier.height(12.dp))

                CafeTextField(
                    value = type.value,
                    onValueChange = {
                        type.value = it
                    },
                    label = "Tipo",
                    placeholder = "Ex.: moído, grão, cápsula"
                )

                Spacer(modifier = Modifier.height(12.dp))

                CafeTextField(
                    value = roast.value,
                    onValueChange = {
                        roast.value = it
                    },
                    label = "Torra",
                    placeholder = "Ex.: clara, média, escura"
                )

                Spacer(modifier = Modifier.height(12.dp))

                CafeTextField(
                    value = defaultWeight.value,
                    onValueChange = {
                        defaultWeight.value = it
                    },
                    label = "Peso padrão em gramas",
                    placeholder = "Ex.: 250",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(12.dp))

                CafeTextField(
                    value = barcode.value,
                    onValueChange = {
                        barcode.value = it
                    },
                    label = "Código de barras",
                    placeholder = "Opcional",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        CafeAnalytics.logEvent(
                            eventName = AnalyticsEvents.SAVE_NEW_COFFEE_TAP,
                            params = mapOf(
                                "has_name" to coffeeName.value.isNotBlank(),
                                "has_brand" to brand.value.isNotBlank(),
                                "has_barcode" to barcode.value.isNotBlank()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = null
                    )

                    Text(
                        text = "Cadastrar café em breve",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CafeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(label)
        },
        placeholder = {
            Text(placeholder)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = androidx.compose.ui.graphics.Color.White,
            unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
            focusedIndicatorColor = CoffeeGold,
            unfocusedIndicatorColor = CoffeeLine,
            cursorColor = CoffeeBrown
        )
    )
}
