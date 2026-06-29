package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.components.formatPriceKg
import com.maurofmorato.cafecomnota.ui.components.formatRating
import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import com.maurofmorato.cafecomnota.ui.theme.CoffeeText

@Composable
fun CoffeeDetailScreen(
    innerPadding: PaddingValues,
    coffee: CoffeeUiModel,
    onBack: () -> Unit,
    onReview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 18.dp)
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

        CoffeeMainCard(coffee = coffee)

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
                text = "Dar minha nota",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Resumo")

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Preço por kg",
                value = formatPriceKg(coffee.priceKg),
                iconKind = SummaryIcon.Price
            )

            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Comprariam",
                value = "${coffee.wouldBuyAgainPercent}%",
                iconKind = SummaryIcon.BuyAgain
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(title = "Perfil percebido")

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

        SectionTitle(title = "Notas detalhadas")

        Spacer(modifier = Modifier.height(12.dp))

        RatingMetric(title = "Aroma", value = coffee.aroma)
        RatingMetric(title = "Sabor", value = coffee.flavor)
        RatingMetric(title = "Corpo", value = coffee.body)
        RatingMetric(title = "Acidez", value = coffee.acidity)
        RatingMetric(title = "Amargor", value = coffee.bitterness)
        RatingMetric(title = "Doçura", value = coffee.sweetness)
        RatingMetric(title = "Custo-benefício", value = coffee.valueRating)

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun CoffeeMainCard(
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
        Column(
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

                Column(
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
                        text = "${coffee.brand} • ${coffee.type} • Torra ${coffee.roast}",
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
                Column {
                    Text(
                        text = "Nota média",
                        color = CoffeeMuted,
                        fontSize = 14.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatRating(coffee.rating),
                            color = CoffeeBrownDark,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CoffeeGold,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${coffee.totalReviews}",
                        color = CoffeeBrownDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "avaliações",
                        color = CoffeeMuted,
                        fontSize = 14.sp
                    )
                }
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
        Column(
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
        }
    }
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
    Column(
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
