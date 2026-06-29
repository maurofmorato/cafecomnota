package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.components.formatPriceKg
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted

@Composable
fun ReviewCoffeeScreen(
    innerPadding: PaddingValues,
    coffeeId: String,
    coffeeName: String,
    onBack: () -> Unit
) {
    val rating = remember { mutableIntStateOf(4) }
    val price = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("250") }
    val wouldBuyAgain = remember { mutableStateOf(true) }
    val comment = remember { mutableStateOf("") }

    val priceKg = remember {
        derivedStateOf {
            val priceValue = price.value
                .replace(".", "")
                .replace(",", ".")
                .toDoubleOrNull()

            val weightValue = weight.value
                .replace(",", ".")
                .toDoubleOrNull()

            if (priceValue != null && weightValue != null && weightValue > 0.0) {
                priceValue * 1000.0 / weightValue
            } else {
                null
            }
        }
    }

    LaunchedEffect(priceKg.value) {
        val calculatedPriceKg = priceKg.value

        if (calculatedPriceKg != null) {
            CafeAnalytics.logEvent(
                eventName = AnalyticsEvents.CALCULATE_PRICE_KG,
                params = mapOf(
                    "coffee_id" to coffeeId,
                    "weight_g" to weight.value,
                    "price_kg_range" to priceKgRange(calculatedPriceKg)
                )
            )
        }
    }

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

        SectionTitle(title = "Dar nota")

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
                    text = coffeeName,
                    color = CoffeeBrown,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Avaliação rápida. A ligação com o Supabase entra na próxima etapa.",
                    color = CoffeeMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Nota geral",
                    color = CoffeeBrown
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    for (star in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Nota $star",
                            tint = if (star <= rating.intValue) CoffeeGold else CoffeeLine,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable {
                                    rating.intValue = star
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = price.value,
                    onValueChange = {
                        price.value = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Preço pago")
                    },
                    placeholder = {
                        Text("Ex.: 18,90")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                        focusedIndicatorColor = CoffeeGold,
                        unfocusedIndicatorColor = CoffeeLine,
                        cursorColor = CoffeeBrown
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = weight.value,
                    onValueChange = {
                        weight.value = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Peso da embalagem em gramas")
                    },
                    placeholder = {
                        Text("Ex.: 250")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                        focusedIndicatorColor = CoffeeGold,
                        unfocusedIndicatorColor = CoffeeLine,
                        cursorColor = CoffeeBrown
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (priceKg.value != null) {
                        "Preço estimado por kg: ${formatPriceKg(priceKg.value!!)}"
                    } else {
                        "Preço estimado por kg: informe preço e peso"
                    },
                    color = CoffeeBrown
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Compraria novamente?",
                        modifier = Modifier.weight(1f),
                        color = CoffeeBrown
                    )

                    Switch(
                        checked = wouldBuyAgain.value,
                        onCheckedChange = {
                            wouldBuyAgain.value = it
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = comment.value,
                    onValueChange = {
                        comment.value = it.take(500)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Comentário")
                    },
                    placeholder = {
                        Text("Ex.: Bom no coado, pouco amargo.")
                    },
                    minLines = 3,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                        focusedIndicatorColor = CoffeeGold,
                        unfocusedIndicatorColor = CoffeeLine,
                        cursorColor = CoffeeBrown
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        CafeAnalytics.logEvent(
                            eventName = AnalyticsEvents.SAVE_REVIEW_TAP,
                            params = mapOf(
                                "coffee_id" to coffeeId,
                                "rating" to rating.intValue,
                                "has_price_kg" to (priceKg.value != null),
                                "would_buy_again" to wouldBuyAgain.value
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )

                    Text(
                        text = "Salvar avaliação em breve",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

private fun priceKgRange(value: Double): String {
    return when {
        value < 40.0 -> "below_40"
        value < 70.0 -> "40_70"
        value < 100.0 -> "70_100"
        else -> "above_100"
    }
}
