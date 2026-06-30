package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.CoffeeRankingItem
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
import com.maurofmorato.cafecomnota.ui.model.bestValueCoffees
import com.maurofmorato.cafecomnota.ui.model.mostReviewedCoffees
import com.maurofmorato.cafecomnota.ui.model.topRatedCoffees
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted

private enum class RankingFilter(
    val analyticsValue: String
) {
    Best("best"),
    Value("value"),
    Reviews("reviews")
}

@Composable
fun RankingScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    coffees: List<CoffeeUiModel>,
    onOpenCoffee: (String) -> Unit,
    onNavigate: (AppDestination) -> Unit
) {
    val selectedFilter = remember {
        mutableStateOf(RankingFilter.Best)
    }

    val rankedCoffees = when (selectedFilter.value) {
        RankingFilter.Best -> topRatedCoffees(coffees)
        RankingFilter.Value -> bestValueCoffees(coffees)
        RankingFilter.Reviews -> mostReviewedCoffees(coffees)
    }

    CafeResponsiveContent(
        innerPadding = innerPadding
    ) {
        CafeHeader(
            strings = strings,
            compact = true
        )

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = strings.rankingTitle)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RankingFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter.value == filter,
                    onClick = {
                        selectedFilter.value = filter

                        CafeAnalytics.logEvent(
                            eventName = AnalyticsEvents.CHANGE_RANKING_FILTER,
                            params = mapOf(
                                "filter" to filter.analyticsValue
                            )
                        )
                    },
                    label = {
                        Text(filterLabel(filter, strings))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (rankedCoffees.isEmpty()) {
            Text(
                text = "O ranking aparecerá depois das primeiras avaliações. Os cafés do catálogo continuam disponíveis na busca.",
                color = CoffeeMuted
            )
        } else {
            rankedCoffees.forEachIndexed { index, coffee ->
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
        }
    }
}

private fun filterLabel(
    filter: RankingFilter,
    strings: AppStrings
): String {
    return when (filter) {
        RankingFilter.Best -> strings.rankingBest
        RankingFilter.Value -> strings.rankingValue
        RankingFilter.Reviews -> strings.rankingMostReviewed
    }
}
