package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
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
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    currentLanguage: AppLanguage,
    isAuthenticated: Boolean,
    coffees: List<CoffeeUiModel>,
    onNavigate: (AppDestination) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onSearch: (String) -> Unit,
    onOpenCoffee: (String) -> Unit
) {
    val showAppShareDialog = remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val versionName = remember(context) {
        @Suppress("DEPRECATION")
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            .orEmpty()
    }

    CafeResponsiveContent(
        innerPadding = innerPadding,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CafeHeader(
            strings = strings,
            versionLabel = "v$versionName",
            actionLabel = if (isAuthenticated) {
                strings.homeAccount
            } else {
                strings.homeSignIn
            },
            onAction = {
                onNavigate(AppDestination.Profile)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LanguageVersionRow(
            strings = strings,
            currentLanguage = currentLanguage,
            onLanguageChange = onLanguageChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        HomeSearchBar(
            strings = strings,
            onSearch = onSearch
        )

        Spacer(modifier = Modifier.height(10.dp))

        ShareAppCard(
            strings = strings,
            onClick = {
                showAppShareDialog.value = true
            }
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
            strings = strings,
            onDismiss = {
                showAppShareDialog.value = false
            }
        )
    }
}

@Composable
private fun ShareAppCard(
    strings: AppStrings,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                tint = CoffeeGold
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = strings.shareAppTitle,
                    color = CoffeeBrown,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = strings.shareAppSubtitle,
                    color = CoffeeMuted,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LanguageVersionRow(
    strings: AppStrings,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val menuExpanded = remember {
        mutableStateOf(false)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { menuExpanded.value = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = CoffeeBrown
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${strings.profileLanguage}:",
                        color = CoffeeBrown,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = currentLanguage.nativeName,
                        color = CoffeeBrown,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = CoffeeBrown,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = menuExpanded.value,
                    onDismissRequest = {
                        menuExpanded.value = false
                    }
                ) {
                    AppLanguage.values().forEach { language ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Text(
                                    text = language.nativeName,
                                    fontWeight = if (
                                        language == currentLanguage
                                    ) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            },
                            onClick = {
                                menuExpanded.value = false
                                onLanguageChange(language)
                            }
                        )
                    }
                }
            }

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

    OutlinedTextField(
        value = searchText.value,
        onValueChange = {
            searchText.value = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = CoffeeCard.copy(alpha = 0.55f),
                shape = RoundedCornerShape(22.dp)
            ),
        label = {
            Text(
                text = strings.navSearch,
                color = CoffeeBrown,
                fontWeight = FontWeight.SemiBold
            )
        },
        placeholder = {
            Text(
                text = strings.searchHomePlaceholder,
                color = CoffeeMuted
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = CoffeeGold
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    onSearch(searchText.value.trim())
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = strings.navSearch,
                    tint = CoffeeBrown
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch(searchText.value.trim())
            }
        ),
        singleLine = true,
        shape = RoundedCornerShape(22.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = CoffeeGold,
            unfocusedIndicatorColor = CoffeeGold.copy(alpha = 0.75f),
            cursorColor = CoffeeBrown
        )
    )
}
