package com.maurofmorato.cafecomnota.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import com.maurofmorato.cafecomnota.ui.theme.CoffeeText
import java.text.NumberFormat
import java.util.Locale

enum class ActionIcon {
    Ranking,
    Review,
    AddCoffee
}

enum class ShortcutType {
    Value,
    Star
}

@Composable
fun CafeBottomBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = androidx.compose.ui.graphics.Color.White,
        tonalElevation = 4.dp
    ) {
        BottomItem(
            destination = AppDestination.Home,
            selected = currentDestination == AppDestination.Home,
            icon = Icons.Default.Home,
            onNavigate = onNavigate
        )

        BottomItem(
            destination = AppDestination.Search,
            selected = currentDestination == AppDestination.Search,
            icon = Icons.Default.Search,
            onNavigate = onNavigate
        )

        BottomItem(
            destination = AppDestination.Ranking,
            selected = currentDestination == AppDestination.Ranking,
            icon = Icons.Default.BarChart,
            onNavigate = onNavigate
        )

        BottomItem(
            destination = AppDestination.Profile,
            selected = currentDestination == AppDestination.Profile,
            icon = Icons.Default.Person,
            onNavigate = onNavigate
        )
    }
}

@Composable
private fun BottomItem(
    destination: AppDestination,
    selected: Boolean,
    icon: ImageVector,
    onNavigate: (AppDestination) -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = {
            onNavigate(destination)
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = destination.label
            )
        },
        label = {
            Text(destination.label)
        }
    )
}

@Composable
fun CafeHeader(
    compact: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalCafe,
                contentDescription = null,
                tint = CoffeeBrown,
                modifier = Modifier.size(if (compact) 34.dp else 44.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Café com nota",
                color = CoffeeBrownDark,
                fontSize = if (compact) 28.sp else 34.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        }

        if (!compact) {
            Spacer(modifier = Modifier.size(8.dp))

            HorizontalDivider(
                modifier = Modifier.width(190.dp),
                color = CoffeeGold
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = "Descubra se esse café é bom antes de comprar.",
                color = CoffeeText.copy(alpha = 0.82f),
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MainActionCard(
    iconType: ActionIcon,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(CoffeeBrown),
                contentAlignment = Alignment.Center
            ) {
                when (iconType) {
                    ActionIcon.Ranking -> Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = CoffeeGold,
                        modifier = Modifier.size(30.dp)
                    )

                    ActionIcon.Review -> Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = CoffeeGold,
                        modifier = Modifier.size(30.dp)
                    )

                    ActionIcon.AddCoffee -> Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = CoffeeGold,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = CoffeeBrownDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = CoffeeMuted,
                        fontSize = 14.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = CoffeeGold,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocalCafe,
            contentDescription = null,
            tint = CoffeeBrown,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            color = CoffeeBrownDark,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.width(12.dp))

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = CoffeeGold
        )
    }
}

@Composable
fun CoffeeRankingItem(
    position: Int,
    coffee: CoffeeUiModel,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CoffeeBrown)
                    .border(
                        width = 1.dp,
                        color = CoffeeGold,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$position",
                    color = CoffeeGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = coffee.name,
                    color = CoffeeBrownDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${coffee.brand} • ${coffee.type} • ${coffee.roast}",
                    color = CoffeeMuted,
                    fontSize = 13.sp
                )

                Text(
                    text = "${formatRating(coffee.rating)} ★ • ${coffee.totalReviews} avaliações",
                    color = CoffeeGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = formatPriceKg(coffee.priceKg),
                color = CoffeeBrownDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ShortcutChip(
    modifier: Modifier = Modifier,
    title: String,
    type: ShortcutType,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable {
            onClick()
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val icon = when (type) {
                ShortcutType.Value -> Icons.Default.Sell
                ShortcutType.Star -> Icons.Default.Star
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CoffeeBrown,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = title,
                color = CoffeeText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun formatRating(value: Double): String {
    return String.format(Locale("pt", "BR"), "%.1f", value)
}

fun formatPriceKg(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return "${formatter.format(value)}/kg"
}
