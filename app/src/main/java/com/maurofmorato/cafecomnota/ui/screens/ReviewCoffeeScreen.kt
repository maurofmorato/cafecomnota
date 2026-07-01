package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun ReviewCoffeeScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    coffeeId: String,
    coffeeName: String,
    authSession: AuthSession?,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { SupabaseReviewRepository() }

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
        Spacer(modifier = Modifier.height(22.dp))
        SectionTitle(title = "Dar nota")
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = coffeeName, color = CoffeeBrownDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (authSession == null) {
            LoginRequiredCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        ReviewCard(
            rating = rating,
            wouldBuyAgain = wouldBuyAgain,
            onRatingChange = { rating = it },
            onWouldBuyAgainChange = { wouldBuyAgain = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PriceCard(
            priceText = priceText,
            weightText = weightText,
            pricePerKg = pricePerKg,
            onPriceChange = { priceText = it },
            onWeightChange = { weightText = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CommentCard(
            brewMethod = brewMethod,
            comment = comment,
            onBrewMethodChange = { brewMethod = it },
            onCommentChange = { comment = it }
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val session = authSession
                if (session == null) {
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
                        "rating" to rating.toDouble()
                    )
                )

                coroutineScope.launch {
                    try {
                        repository.saveReviewAndOptionalPrice(
                            ReviewSaveRequest(
                                cafeId = coffeeId,
                                userId = session.userId,
                                accessToken = session.accessToken,
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
                                "rating" to rating.toDouble()
                            )
                        )
                        message = "Avaliação salva com sucesso. Ao reabrir o app, a nota atualizada será carregada do Supabase."
                    } catch (throwable: Throwable) {
                        CafeAnalytics.recordNonFatal(
                            throwable = throwable,
                            params = mapOf("screen" to "review", "action" to "save_review", "coffee_id" to coffeeId)
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
            Text(text = if (isSaving) "Salvando..." else "Salvar avaliação", modifier = Modifier.padding(start = 8.dp))
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, color = CoffeeBrown, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun ReviewCard(
    rating: Float,
    wouldBuyAgain: Boolean,
    onRatingChange: (Float) -> Unit,
    onWouldBuyAgainChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            Text("Nota geral: ${formatRating(rating.toDouble())}", color = CoffeeBrownDark, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Slider(value = rating, onValueChange = onRatingChange, valueRange = 1f..5f, steps = 7)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Compraria novamente?", color = CoffeeBrownDark, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = wouldBuyAgain, onClick = { onWouldBuyAgainChange(true) }, label = { Text("Sim") })
                FilterChip(selected = !wouldBuyAgain, onClick = { onWouldBuyAgainChange(false) }, label = { Text("Não") })
            }
        }
    }
}

@Composable
private fun PriceCard(
    priceText: String,
    weightText: String,
    pricePerKg: Double?,
    onPriceChange: (String) -> Unit,
    onWeightChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = CoffeeBrown)
                Text("Preço pago", color = CoffeeBrownDark, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Informe o preço da embalagem e o peso. O app calcula o preço por kg e o equivalente por 250g.", color = CoffeeMuted, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = priceText, onValueChange = onPriceChange, modifier = Modifier.fillMaxWidth(), label = { Text("Preço pago") }, placeholder = { Text("Ex.: 18,90") }, singleLine = true, colors = fieldColors())
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = weightText, onValueChange = onWeightChange, modifier = Modifier.fillMaxWidth(), label = { Text("Peso em gramas") }, placeholder = { Text("Ex.: 250, 500 ou 1000") }, singleLine = true, colors = fieldColors())
            Spacer(modifier = Modifier.height(12.dp))
            PricePreview(pricePerKg = pricePerKg)
        }
    }
}

@Composable
private fun CommentCard(
    brewMethod: String,
    comment: String,
    onBrewMethodChange: (String) -> Unit,
    onCommentChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            Text("Preparo e comentário", color = CoffeeBrownDark, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PreparationChip(selected = brewMethod == "nao_informado", text = "Não informar", onClick = { onBrewMethodChange("nao_informado") })
                PreparationChip(selected = brewMethod == "coado", text = "Coado", onClick = { onBrewMethodChange("coado") })
                PreparationChip(selected = brewMethod == "espresso", text = "Espresso", onClick = { onBrewMethodChange("espresso") })
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = comment, onValueChange = onCommentChange, modifier = Modifier.fillMaxWidth(), label = { Text("Comentário") }, placeholder = { Text("Ex.: café aromático, corpo bom, acidez equilibrada...") }, minLines = 3, colors = fieldColors())
        }
    }
}

@Composable
private fun LoginRequiredCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Security, contentDescription = null, tint = CoffeeBrown)
            androidx.compose.foundation.layout.Column {
                Text("Login necessário", color = CoffeeBrownDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Para salvar avaliação e preço, entre na conta pela tela Perfil.", color = CoffeeMuted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun PreparationChip(selected: Boolean, text: String, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(text) })
}

@Composable
private fun PricePreview(pricePerKg: Double?) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    if (pricePerKg == null) {
        Text("Preço por kg será calculado automaticamente.", color = CoffeeMuted, fontSize = 14.sp)
        return
    }
    val price250g = pricePerKg / 4.0
    Text("Preço por kg: ${formatter.format(pricePerKg)}", color = CoffeeBrownDark, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    Text("Equivalente por 250g: ${formatter.format(price250g)}", color = CoffeeText, fontSize = 15.sp)
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = CoffeeGold,
    unfocusedIndicatorColor = CoffeeLine,
    cursorColor = CoffeeBrown
)

private fun parseDecimal(value: String): Double? {
    val cleanedValue = value.trim().replace("R$", "").replace(" ", "").replace(".", "").replace(",", ".")
    return cleanedValue.toDoubleOrNull()
}

private fun calculatePricePerKg(pricePaid: Double?, weightGrams: Double?): Double? {
    if (pricePaid == null || weightGrams == null || pricePaid <= 0.0 || weightGrams <= 0.0) return null
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
    return null
}
