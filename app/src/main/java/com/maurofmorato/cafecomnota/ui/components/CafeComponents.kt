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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
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
    strings: AppStrings,
    onNavigate: (AppDestination) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomItem(
            modifier = Modifier.weight(1f),
            destination = AppDestination.Home,
            label = strings.navHome,
            selected = currentDestination == AppDestination.Home,
            icon = Icons.Default.Home,
            onNavigate = onNavigate
        )

        BottomItem(
            modifier = Modifier.weight(1f),
            destination = AppDestination.Search,
            label = strings.navSearch,
            selected = currentDestination == AppDestination.Search,
            icon = Icons.Default.Search,
            onNavigate = onNavigate
        )

        BottomItem(
            modifier = Modifier.weight(1f),
            destination = AppDestination.Ranking,
            label = strings.navRanking,
            selected = currentDestination == AppDestination.Ranking,
            icon = Icons.Default.BarChart,
            onNavigate = onNavigate
        )

        BottomItem(
            modifier = Modifier.weight(1f),
            destination = AppDestination.Profile,
            label = strings.navProfile,
            selected = currentDestination == AppDestination.Profile,
            icon = Icons.Default.Person,
            onNavigate = onNavigate
        )
    }
}

@Composable
private fun BottomItem(
    modifier: Modifier = Modifier,
    destination: AppDestination,
    label: String,
    selected: Boolean,
    icon: ImageVector,
    onNavigate: (AppDestination) -> Unit
) {
    val itemColor = if (selected) CoffeeBrown else CoffeeMuted
    val backgroundColor = if (selected) CoffeeCard else Color.Transparent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable {
                onNavigate(destination)
            }
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = itemColor,
            modifier = Modifier.size(22.dp)
        )

        Text(
            text = label,
            color = itemColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CafeHeader(
    strings: AppStrings,
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
                modifier = Modifier.size(if (compact) 26.dp else 36.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = strings.appName,
                color = CoffeeBrownDark,
                fontSize = if (compact) 23.sp else 29.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!compact) {
            Spacer(modifier = Modifier.size(6.dp))

            HorizontalDivider(
                modifier = Modifier.width(170.dp),
                color = CoffeeGold
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = strings.tagline,
                color = CoffeeText.copy(alpha = 0.82f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
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
        shape = RoundedCornerShape(18.dp),
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
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(CoffeeBrown),
                contentAlignment = Alignment.Center
            ) {
                when (iconType) {
                    ActionIcon.Ranking -> Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = CoffeeGold,
                        modifier = Modifier.size(24.dp)
                    )

                    ActionIcon.Review -> Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = CoffeeGold,
                        modifier = Modifier.size(24.dp)
                    )

                    ActionIcon.AddCoffee -> Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = CoffeeGold,
                        modifier = Modifier.size(27.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = CoffeeBrownDark,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = CoffeeMuted,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = CoffeeGold,
                modifier = Modifier.size(25.dp)
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
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            modifier = Modifier.weight(1f, fill = false),
            text = title,
            color = CoffeeBrownDark,
            fontSize = 20.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(10.dp))

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
    reviewLabel: String,
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
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(CoffeeBrown)
                    .border(
                        width = 1.dp,
                        color = CoffeeGold,
                        shape = RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$position",
                    color = CoffeeGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = coffee.name,
                    color = CoffeeBrownDark,
                    fontSize = 17.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = coffee.brand,
                    color = CoffeeMuted,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${coffee.type} • ${coffee.roast}",
                    color = CoffeeMuted,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text = if (coffee.hasRating) {
                        "${formatRating(coffee.rating)} ★ • ${coffee.totalReviews} $reviewLabel"
                    } else {
                        "Aguardando avaliações"
                    },
                    color = CoffeeGold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatPriceKgSmart(coffee.priceKg),
                    color = if (coffee.hasPrice) CoffeeBrownDark else CoffeeMuted,
                    fontSize = if (coffee.hasPrice) 15.sp else 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (coffee.hasPrice) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
        shape = RoundedCornerShape(14.dp),
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
                .padding(horizontal = 9.dp, vertical = 10.dp),
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
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = title,
                color = CoffeeText,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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

fun formatPriceKgSmart(value: Double): String {
    return if (value > 0.0) {
        formatPriceKg(value)
    } else {
        "Preço não informado"
    }
}

fun formatPrice250g(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return "${formatter.format(value)}/250g"
}
