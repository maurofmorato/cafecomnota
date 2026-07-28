package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.ui.components.ActionIcon
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.CoffeeRankingItem
import com.maurofmorato.cafecomnota.ui.components.MainActionCard
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.components.ShortcutChip
import com.maurofmorato.cafecomnota.ui.components.ShortcutType
import com.maurofmorato.cafecomnota.ui.i18n.AppLanguage
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
import com.maurofmorato.cafecomnota.ui.model.topRatedCoffees
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    currentLanguage: AppLanguage,
    coffees: List<CoffeeUiModel>,
    onNavigate: (AppDestination) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onSearch: (String) -> Unit,
    onOpenCoffee: (String) -> Unit
) {
    val showAppShareDialog = remember { mutableStateOf(false) }

    CafeResponsiveContent(
        innerPadding = innerPadding,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CafeHeader(strings = strings)

        Spacer(modifier = Modifier.height(10.dp))

        HomeLanguageCard(
            strings = strings,
            currentLanguage = currentLanguage,
            onLanguageChange = onLanguageChange
        )

        Spacer(modifier = Modifier.height(10.dp))

        VersionShareRow(
            onShowQrCode = { showAppShareDialog.value = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HomeSearchBar(
            strings = strings,
            onSearch = onSearch
        )

        Spacer(modifier = Modifier.height(14.dp))

        MainActionCard(
            iconType = ActionIcon.Ranking,
            title = strings.actionRankingTitle,
            subtitle = strings.actionRankingSubtitle,
            onClick = {
                onNavigate(AppDestination.Ranking)
            }
        )

        Spacer(modifier = Modifier.height(9.dp))

        MainActionCard(
            iconType = ActionIcon.Review,
            title = strings.actionReviewTitle,
            subtitle = strings.actionReviewSubtitle,
            onClick = {
                CafeAnalytics.logEvent(
                    eventName = AnalyticsEvents.START_REVIEW,
                    params = mapOf(
                        "source" to "home_action_card"
                    )
                )

                onNavigate(AppDestination.ReviewCoffee)
            }
        )

        Spacer(modifier = Modifier.height(9.dp))

        MainActionCard(
            iconType = ActionIcon.AddCoffee,
            title = strings.actionAddCoffeeTitle,
            subtitle = strings.actionAddCoffeeSubtitle,
            onClick = {
                CafeAnalytics.logEvent(
                    eventName = AnalyticsEvents.START_ADD_COFFEE,
                    params = mapOf(
                        "source" to "home_action_card"
                    )
                )

                onNavigate(AppDestination.AddCoffee)
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        SectionTitle(title = strings.sectionTopWeek)

        Spacer(modifier = Modifier.height(10.dp))

        val rankedCoffees = topRatedCoffees(coffees)

        if (rankedCoffees.isEmpty()) {
            Text(
                text = strings.searchNoCoffeeFound,
                color = CoffeeMuted
            )
        } else {
            rankedCoffees.take(3).forEachIndexed { index, coffee ->
                CoffeeRankingItem(
                    position = index + 1,
                    coffee = coffee,
                    reviewLabel = strings.detailReviews,
                    awaitingReviewsLabel = strings.coffeeAwaitingReviews,
                    priceNotInformedLabel = strings.coffeePriceNotInformed,
                    onClick = {
                        onOpenCoffee(coffee.id)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SectionTitle(title = strings.sectionShortcuts)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShortcutChip(
                modifier = Modifier.weight(1f),
                title = strings.shortcutBestValue,
                type = ShortcutType.Value,
                onClick = {
                    onNavigate(AppDestination.Ranking)
                }
            )

            ShortcutChip(
                modifier = Modifier.weight(1f),
                title = strings.shortcutMostReviewed,
                type = ShortcutType.Star,
                onClick = {
                    onNavigate(AppDestination.Ranking)
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showAppShareDialog.value) {
        AppShareDialog(
            onDismiss = { showAppShareDialog.value = false }
        )
    }
}

@Composable
private fun HomeLanguageCard(
    strings: AppStrings,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = strings.profileLanguage,
                color = CoffeeBrown,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = strings.profileLanguageInfo,
                color = CoffeeMuted,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Clip
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AppLanguage.values().toList()) { language ->
                    FilterChip(
                        selected = currentLanguage == language,
                        onClick = { onLanguageChange(language) },
                        label = { Text(language.nativeName, maxLines = 1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionShareRow(
    onShowQrCode: () -> Unit
) {
    val context = LocalContext.current
    val versionName = remember(context) {
        @Suppress("DEPRECATION")
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            .orEmpty()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = CoffeeCard,
                    shape = RoundedCornerShape(50)
                )
                .height(32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = versionName,
                color = CoffeeMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onShowQrCode) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = "QR Code",
                tint = CoffeeBrown
            )
        }
    }
}

@Composable
private fun HomeSearchBar(
    strings: AppStrings,
    onSearch: (String) -> Unit
) {
    val searchText = remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = searchText.value,
            onValueChange = {
                searchText.value = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = strings.searchHomePlaceholder,
                    color = Color.Gray
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = strings.navSearch,
                    tint = CoffeeBrown,
                    modifier = Modifier
                        .then(
                            androidx.compose.ui.Modifier
                        )
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(22.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = CoffeeGold,
                unfocusedIndicatorColor = CoffeeLine,
                cursorColor = CoffeeBrown
            )
        )

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            androidx.compose.material3.IconButton(
                onClick = {
                    onSearch(searchText.value)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = strings.navSearch,
                    tint = CoffeeBrown
                )
            }
        }
    }
}
