package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.model.topRatedCoffees
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    onNavigate: (AppDestination) -> Unit,
    onOpenCoffee: (String) -> Unit
) {
    CafeResponsiveContent(
        innerPadding = innerPadding,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CafeHeader(strings = strings)

        Spacer(modifier = Modifier.height(22.dp))

        HomeSearchBar(
            strings = strings,
            onSearchClick = {
                onNavigate(AppDestination.Search)
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        MainActionCard(
            iconType = ActionIcon.Ranking,
            title = strings.actionRankingTitle,
            subtitle = strings.actionRankingSubtitle,
            onClick = {
                onNavigate(AppDestination.Ranking)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(26.dp))

        SectionTitle(title = strings.sectionTopWeek)

        Spacer(modifier = Modifier.height(10.dp))

        topRatedCoffees().take(3).forEachIndexed { index, coffee ->
            CoffeeRankingItem(
                position = index + 1,
                coffee = coffee,
                reviewLabel = strings.detailReviews,
                onClick = {
                    onOpenCoffee(coffee.id)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

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
}

@Composable
private fun HomeSearchBar(
    strings: AppStrings,
    onSearchClick: () -> Unit
) {
    val searchText = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSearchClick()
            }
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
                    modifier = Modifier.clickable {
                        onSearchClick()
                    }
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
    }
}
