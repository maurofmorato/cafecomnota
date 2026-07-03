package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.data.review.ReviewSaveRequest
import com.maurofmorato.cafecomnota.data.review.SupabaseReviewRepository
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.components.formatRating
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import com.maurofmorato.cafecomnota.ui.theme.CoffeeText
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private val TitleTextStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
private val BodyTextStyle = TextStyle(fontSize = 13.sp)
private val FieldTextStyle = TextStyle(fontSize = 16.sp)
private val FieldLabelTextStyle = TextStyle(fontSize = 12.sp)
private val ChipTextStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)

@Composable
fun ReviewCoffeeScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    coffeeId: String,
    coffeeName: String,
    authSession: AuthSession?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val reviewRepository = remember { SupabaseReviewRepository() }

    var rating by remember { mutableFloatStateOf(4.0f) }
    var wouldBuyAgain by remember { mutableStateOf(true) }
    var priceText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("250") }
    var brewMethod by remember { mutableStateOf("nao_informado") }
    var comment by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val pricePaid = parseDecimal(priceText)
    val weightGrams = parseDecimal(weightText)
    val pricePerKg = calculatePricePerKg(pricePaid, weightGrams)

    CafeResponsiveContent(innerPadding = innerPadding) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = strings.commonBack, tint = CoffeeBrown)
        }

        CafeHeader(strings = strings, compact = true)

        Spacer(modifier = Modifier.height(18.dp))
        SectionTitle(title = "Dar nota")
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = coffeeName,
            color = CoffeeBrownDark,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 25.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (authSession == null) {
            LoginRequiredCard()
            Spacer(modifier = Modifier.height(14.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CoffeeCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Nota geral: ${formatRating(rating.toDouble())}",
                    color = CoffeeBrownDark,
                    style = TitleTextStyle
                )

                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 1f..5f,
                    steps = 7
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Compraria novamente?",
                    color = CoffeeBrownDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactFilterChip(selected = wouldBuyAgain, text = "Sim", onClick = { wouldBuyAgain = true })
                    CompactFilterChip(selected = !wouldBuyAgain, text = "Não", onClick = { wouldBuyAgain = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CoffeeCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = CoffeeBrown)
                    Text(text = "Preço pago", color = CoffeeBrownDark, style = TitleTextStyle)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Moeda atual: BRL. Informe preço e peso da embalagem.",
                    color = CoffeeMuted,
                    style = BodyTextStyle,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                CompactOutlinedTextField(
                    value = priceText,
                    onValueChange = { typed -> priceText = sanitizePriceInput(typed) },
                    label = "Preço pago (BRL)",
                    placeholder = "Ex.: 20,00",
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                priceText = formatDecimalForInput(priceText)
                            }
                        }
                )

                Spacer(modifier = Modifier.height(8.dp))

                CompactOutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = "Peso em gramas",
                    placeholder = "Ex.: 250",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                PricePreview(pricePerKg = pricePerKg)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CoffeeCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Preparo e comentário", color = CoffeeBrownDark, style = TitleTextStyle)
                Spacer(modifier = Modifier.height(10.dp))

                PreparationChips(
                    brewMethod = brewMethod,
                    onChange = { brewMethod = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Comentário", style = FieldLabelTextStyle) },
                    placeholder = {
                        Text(
                            text = "Ex.: aromático, corpo bom, acidez equilibrada...",
                            style = BodyTextStyle
                        )
                    },
                    minLines = 3,
                    textStyle = TextStyle(fontSize = 15.sp, lineHeight = 19.sp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = CoffeeGold,
                        unfocusedIndicatorColor = CoffeeLine,
                        cursorColor = CoffeeBrown
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (authSession == null) {
                    message = "Entre na conta pelo Perfil antes de salvar."
                    return@Button
                }

                if (isSaving) return@Button

                val validationMessage = validateReview(
                    rating = rating.toDouble(),
                    pricePaid = pricePaid,
                    weightGrams = weightGrams,
                    hasTypedPrice = priceText.isNotBlank(),
                    hasTypedWeight = weightText.isNotBlank()
                )

                if (validationMessage != null) {
                    message = validationMessage
                    return@Button
                }

                isSaving = true
                message = "Salvando..."

                CafeAnalytics.logEvent(
                    eventName = AnalyticsEvents.SAVE_REVIEW_TAP,
                    params = mapOf(
                        "coffee_id" to coffeeId,
                        "has_price" to (pricePaid != null && weightGrams != null),
                        "rating" to rating.toDouble(),
                        "price_paid" to pricePaid,
                        "weight_grams" to weightGrams,
                        "currency" to "BRL"
                    )
                )

                coroutineScope.launch {
                    try {
                        reviewRepository.saveReviewAndOptionalPrice(
                            request = ReviewSaveRequest(
                                cafeId = coffeeId,
                                userId = authSession.userId,
                                accessToken = authSession.accessToken,
                                rating = rating.toDouble(),
                                wouldBuyAgain = wouldBuyAgain,
                                pricePaid = pricePaid,
                                weightGrams = weightGrams,
                                brewMethod = brewMethod,
                                comment = comment.trim()
                            )
                        )

                        CafeAnalytics.logEvent(
                            eventName = "save_review_success",
                            params = mapOf(
                                "coffee_id" to coffeeId,
                                "has_price" to (pricePaid != null && weightGrams != null),
                                "rating" to rating.toDouble(),
                                "price_paid" to pricePaid,
                                "weight_grams" to weightGrams,
                                "currency" to "BRL"
                            )
                        )

                        message = "Avaliação salva com sucesso."
                        onSaved()
                    } catch (throwable: Throwable) {
                        CafeAnalytics.recordNonFatal(
                            throwable = throwable,
                            params = mapOf(
                                "screen" to "review",
                                "action" to "save_review",
                                "coffee_id" to coffeeId
                            )
                        )

                        CafeAnalytics.logEvent(
                            eventName = "save_review_error",
                            params = mapOf(
                                "coffee_id" to coffeeId,
                                "message" to (throwable.message ?: "erro")
                            )
                        )

                        message = throwable.message ?: "Não foi possível salvar a avaliação."
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
                text = if (isSaving) "Salvando..." else "Salvar avaliação",
                modifier = Modifier.padding(start = 8.dp),
                fontSize = 14.sp
            )
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = message, color = CoffeeBrown, fontSize = 13.sp, lineHeight = 17.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun LoginRequiredCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = CoffeeBrown)

            androidx.compose.foundation.layout.Column {
                Text(
                    text = "Login necessário",
                    color = CoffeeBrownDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Para salvar avaliação e preço, entre na conta pela tela Perfil.",
                    color = CoffeeMuted,
                    style = BodyTextStyle,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun CompactOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = 56.dp),
        label = {
            Text(
                text = label,
                style = FieldLabelTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                style = BodyTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        singleLine = true,
        textStyle = FieldTextStyle,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = CoffeeGold,
            unfocusedIndicatorColor = CoffeeLine,
            cursorColor = CoffeeBrown
        )
    )
}

@Composable
private fun CompactFilterChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = ChipTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@Composable
private fun PreparationChips(
    brewMethod: String,
    onChange: (String) -> Unit
) {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CompactFilterChip(
            modifier = Modifier.fillMaxWidth(),
            selected = brewMethod == "nao_informado",
            text = "Não informar",
            onClick = { onChange("nao_informado") }
        )

        CompactFilterChip(
            modifier = Modifier.fillMaxWidth(),
            selected = brewMethod == "coado",
            text = "Coado",
            onClick = { onChange("coado") }
        )

        CompactFilterChip(
            modifier = Modifier.fillMaxWidth(),
            selected = brewMethod == "espresso",
            text = "Espresso",
            onClick = { onChange("espresso") }
        )
    }
}

@Composable
private fun PricePreview(pricePerKg: Double?) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    if (pricePerKg == null) {
        Text(
            text = "Preço por kg será calculado automaticamente.",
            color = CoffeeMuted,
            fontSize = 13.sp,
            lineHeight = 17.sp
        )
        return
    }

    val price250g = pricePerKg / 4.0

    Text(
        text = "Preço por kg: ${formatter.format(pricePerKg)}",
        color = CoffeeBrownDark,
        fontSize = 15.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        text = "Equivalente por 250g: ${formatter.format(price250g)}",
        color = CoffeeText,
        fontSize = 13.sp,
        lineHeight = 17.sp
    )
}

private fun sanitizePriceInput(value: String): String {
    return value.filter { char -> char.isDigit() || char == ',' || char == '.' }
}

private fun parseDecimal(value: String): Double? {
    val cleanedValue = value
        .trim()
        .replace("R$", "", ignoreCase = true)
        .replace(" ", "")

    if (cleanedValue.isBlank()) return null

    val hasComma = cleanedValue.contains(",")
    val hasDot = cleanedValue.contains(".")

    val normalizedValue = when {
        hasComma && hasDot -> {
            val lastComma = cleanedValue.lastIndexOf(",")
            val lastDot = cleanedValue.lastIndexOf(".")

            if (lastComma > lastDot) {
                cleanedValue.replace(".", "").replace(",", ".")
            } else {
                cleanedValue.replace(",", "")
            }
        }

        hasComma -> cleanedValue.replace(",", ".")
        hasDot -> cleanedValue
        else -> cleanedValue
    }

    return normalizedValue.toDoubleOrNull()
}

private fun formatDecimalForInput(value: String): String {
    val parsedValue = parseDecimal(value) ?: return value.trim()

    return String.format(Locale("pt", "BR"), "%.2f", parsedValue)
}

private fun calculatePricePerKg(pricePaid: Double?, weightGrams: Double?): Double? {
    if (pricePaid == null || weightGrams == null || pricePaid <= 0.0 || weightGrams <= 0.0) {
        return null
    }

    return pricePaid / weightGrams * 1000.0
}

private fun validateReview(
    rating: Double,
    pricePaid: Double?,
    weightGrams: Double?,
    hasTypedPrice: Boolean,
    hasTypedWeight: Boolean
): String? {
    if (rating < 1.0 || rating > 5.0) return "A nota deve estar entre 1 e 5."
    if (hasTypedPrice && (pricePaid == null || pricePaid <= 0.0)) return "Informe um preço válido."
    if (hasTypedWeight && (weightGrams == null || weightGrams <= 0.0)) return "Informe um peso válido em gramas."
    if (hasTypedPrice != hasTypedWeight) return "Para registrar preço, informe preço e peso."
    if (pricePaid != null && pricePaid > 1000.0) return "Preço muito alto. Confira se você digitou corretamente."

    return null
}
