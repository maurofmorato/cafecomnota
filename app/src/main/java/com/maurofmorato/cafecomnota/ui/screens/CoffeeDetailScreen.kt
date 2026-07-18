package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.data.admin.SupabaseAdminRepository
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.components.SubScreenHero
import com.maurofmorato.cafecomnota.ui.components.formatPrice250g
import com.maurofmorato.cafecomnota.ui.components.formatPriceKg
import com.maurofmorato.cafecomnota.ui.components.formatRating
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import com.maurofmorato.cafecomnota.ui.theme.CoffeeText
import kotlinx.coroutines.launch

@Composable
fun CoffeeDetailScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    coffee: CoffeeUiModel,
    authSession: AuthSession?,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onReview: () -> Unit,
    onCoffeeModerated: () -> Unit
) {
    var showShareDialog by remember { mutableStateOf(false) }

    CafeResponsiveContent(
        innerPadding = innerPadding
    ) {
        SubScreenHero(
            strings = strings,
            title = "Detalhes do café",
            subtitle = "Informações, avaliações e perfil percebido pela comunidade.",
            onBack = onBack,
            trailingAction = {
                IconButton(onClick = { showShareDialog = true }) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "Compartilhar café por QR Code",
                    tint = CoffeeBrown
                )
            }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CoffeeMainCard(
            strings = strings,
            coffee = coffee
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onReview,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null
            )

            Text(
                text = strings.detailGiveMyRating,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (isAdmin && authSession != null) {
            Spacer(modifier = Modifier.height(14.dp))

            AdminModerationCard(
                coffee = coffee,
                authSession = authSession,
                onCoffeeModerated = onCoffeeModerated
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = strings.detailSummary)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = strings.detailPricePerKg,
                value = if (coffee.hasPrice) {
                    formatPriceKg(coffee.priceKg)
                } else {
                    "Ainda não informado"
                },
                supportingText = if (coffee.hasPrice && coffee.price250g > 0.0) {
                    "Equivale a ${formatPrice250g(coffee.price250g)}"
                } else {
                    "Informe um preço na avaliação"
                },
                iconKind = SummaryIcon.Price
            )

            SummaryCard(
                modifier = Modifier.weight(1f),
                title = strings.detailWouldBuyAgain,
                value = if (coffee.hasRating) {
                    "${coffee.wouldBuyAgainPercent}%"
                } else {
                    "—"
                },
                supportingText = if (coffee.hasRating) {
                    "Baseado nas avaliações"
                } else {
                    "Aguardando avaliações"
                },
                iconKind = SummaryIcon.BuyAgain
            )
        }

        if (coffee.hasPrice) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = buildPriceFreshnessText(coffee),
                color = CoffeeMuted,
                fontSize = 13.sp
            )
        }

        if (coffee.hasTechnicalSheet) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionTitle(title = "Ficha técnica")

            Spacer(modifier = Modifier.height(12.dp))

            TechnicalSheetCard(coffee = coffee)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(title = strings.detailPerceivedProfile)

        Spacer(modifier = Modifier.height(12.dp))

        CoffeeTags(tags = coffee.tags)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = coffee.description,
            color = CoffeeText,
            fontSize = 16.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = strings.detailDetailedScores)

        Spacer(modifier = Modifier.height(12.dp))

        if (coffee.hasRating) {
            RatingMetric(title = "Aroma", value = coffee.aroma)
            RatingMetric(title = "Sabor", value = coffee.flavor)
            RatingMetric(title = "Corpo", value = coffee.body)
            RatingMetric(title = "Acidez", value = coffee.acidity)
            RatingMetric(title = "Amargor", value = coffee.bitterness)
            RatingMetric(title = "Doçura", value = coffee.sweetness)
            RatingMetric(title = strings.shortcutBestValue, value = coffee.valueRating)
        } else {
            Text(
                text = "As notas detalhadas aparecerão depois das primeiras avaliações.",
                color = CoffeeMuted,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showShareDialog) {
        CoffeeShareDialog(
            coffeeId = coffee.id,
            coffeeName = coffee.name,
            onDismiss = { showShareDialog = false }
        )
    }
}

@Composable
private fun AdminModerationCard(
    coffee: CoffeeUiModel,
    authSession: AuthSession,
    onCoffeeModerated: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val adminRepository = remember {
        SupabaseAdminRepository()
    }

    var isWorking by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    fun updateStatus(
        newStatus: String,
        reason: String
    ) {
        if (isWorking) {
            return
        }

        isWorking = true
        message = "Aplicando moderação..."

        coroutineScope.launch {
            try {
                adminRepository.updateCoffeeStatus(
                    coffeeId = coffee.id,
                    accessToken = authSession.accessToken,
                    newStatus = newStatus,
                    reason = reason
                )

                message = "Status atualizado para $newStatus."
                onCoffeeModerated()
            } catch (throwable: Throwable) {
                message = throwable.message ?: "Não foi possível moderar este café."
            } finally {
                isWorking = false
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Administração",
                color = CoffeeBrownDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ações rápidas para moderar este café pelo celular.",
                color = CoffeeMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    updateStatus(
                        newStatus = "oculto",
                        reason = "Ocultado pelo administrador no aplicativo."
                    )
                },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ocultar café",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    updateStatus(
                        newStatus = "pendente",
                        reason = "Marcado para revisão pelo administrador no aplicativo."
                    )
                },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Marcar como pendente",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    updateStatus(
                        newStatus = "ativo",
                        reason = "Reativado pelo administrador no aplicativo."
                    )
                },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Reativar café",
                    fontSize = 14.sp
                )
            }

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    color = CoffeeBrown,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun CoffeeMainCard(
    strings: AppStrings,
    coffee: CoffeeUiModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = CoffeeBrown,
                            shape = CircleShape
                        )
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = null,
                        tint = CoffeeGold
                    )
                }

                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = coffee.name,
                        color = CoffeeBrownDark,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )

                    Text(
                        text = "${coffee.brand} • ${coffee.type} • ${coffee.roast}",
                        color = CoffeeMuted,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                androidx.compose.foundation.layout.Column {
                    Text(
                        text = strings.detailAverageRating,
                        color = CoffeeMuted,
                        fontSize = 14.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (coffee.hasRating) {
                                formatRating(coffee.rating)
                            } else {
                                "—"
                            },
                            color = CoffeeBrownDark,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (coffee.hasRating) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = CoffeeGold,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }

                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${coffee.totalReviews}",
                        color = CoffeeBrownDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = strings.detailReviews,
                        color = CoffeeMuted,
                        fontSize = 14.sp
                    )
                }
            }

            if (!coffee.hasRating) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Aguardando avaliações",
                    color = CoffeeMuted,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private enum class SummaryIcon {
    Price,
    BuyAgain
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    supportingText: String,
    iconKind: SummaryIcon
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = when (iconKind) {
                    SummaryIcon.Price -> Icons.Default.Sell
                    SummaryIcon.BuyAgain -> Icons.Default.ThumbUp
                },
                contentDescription = null,
                tint = CoffeeBrown
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = CoffeeMuted,
                fontSize = 13.sp
            )

            Text(
                text = value,
                color = CoffeeBrownDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = supportingText,
                color = CoffeeMuted,
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun TechnicalSheetCard(
    coffee: CoffeeUiModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = CoffeeBrown
            )

            TechnicalLine("Produto", coffee.productLabel)
            TechnicalLine("Marca", coffee.brand)
            TechnicalLine("Produtor", coffee.producer)
            TechnicalLine("Origem", coffee.originRegion)
            TechnicalLine("Altitude", coffee.altitudeMeters?.let { "$it m" })
            TechnicalLine("Variedade", coffee.variety)
            TechnicalLine("Tipo de Café", coffee.type)
            TechnicalLine("Processo", coffee.process)
            TechnicalLine("Pontuação SCA", coffee.scaScoreText)
            TechnicalLine("Corpo", coffee.bodyDescription)
            TechnicalLine("Aroma e Sabor", coffee.aromaFlavor)
            TechnicalLine("Acidez", coffee.acidityDescription)
            TechnicalLine("Certificação/Fonte", coffee.certification)
            TechnicalLine("Fonte do cadastro", coffee.dataSourceLabel)
        }
    }
}

@Composable
private fun TechnicalLine(
    label: String,
    value: String?
) {
    if (value.isNullOrBlank()) {
        return
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "$label: $value",
        color = CoffeeText,
        fontSize = 14.sp,
        lineHeight = 19.sp
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoffeeTags(
    tags: List<String>
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            ElevatedAssistChip(
                onClick = { },
                label = {
                    Text(tag)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Composable
private fun RatingMetric(
    title: String,
    value: Double
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = CoffeeBrownDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = formatRating(value),
                color = CoffeeBrown,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        LinearProgressIndicator(
            progress = {
                (value / 5.0).toFloat().coerceIn(0f, 1f)
            },
            modifier = Modifier.fillMaxWidth(),
            color = CoffeeGold,
            trackColor = CoffeeLine
        )
    }
}

private fun buildPriceFreshnessText(
    coffee: CoffeeUiModel
): String {
    val total = coffee.totalPriceRecords
    val date = coffee.lastPriceDate

    return when {
        total > 0 && !date.isNullOrBlank() -> {
            "Preço baseado em $total registro(s). Último preço informado em $date."
        }

        total > 0 -> {
            "Preço baseado em $total registro(s)."
        }

        else -> {
            "Preço ainda sem histórico."
        }
    }
}
