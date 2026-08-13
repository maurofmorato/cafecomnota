package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.CoffeeRankingItem
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.components.SubScreenHero
import com.maurofmorato.cafecomnota.ui.components.formatRating
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
    val showBayesianInfo = remember {
        mutableStateOf(false)
    }

    val rankedCoffees = when (selectedFilter.value) {
        RankingFilter.Best -> topRatedCoffees(coffees)
        RankingFilter.Value -> bestValueCoffees(coffees)
        RankingFilter.Reviews -> mostReviewedCoffees(coffees)
    }

    CafeResponsiveContent(
        innerPadding = innerPadding
    ) {
        SubScreenHero(
            strings = strings,
            title = strings.rankingTitle,
            subtitle = strings.actionRankingSubtitle
        )

        Spacer(modifier = Modifier.height(10.dp))

        RankingFilterChips(
            selectedFilter = selectedFilter.value,
            strings = strings,
            onSelected = { filter ->
                selectedFilter.value = filter

                CafeAnalytics.logEvent(
                    eventName = AnalyticsEvents.CHANGE_RANKING_FILTER,
                    params = mapOf(
                        "filter" to filter.analyticsValue
                    )
                )
            }
        )

        if (selectedFilter.value != RankingFilter.Reviews) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.rankingBayesianMethod,
                    color = CoffeeMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = {
                        showBayesianInfo.value = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = strings.rankingBayesianMethod,
                        tint = CoffeeMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (rankedCoffees.isEmpty()) {
            Text(
                text = strings.searchNoCoffeeFound,
                color = CoffeeMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        } else {
            rankedCoffees.forEachIndexed { index, coffee ->
                val showingValue = selectedFilter.value == RankingFilter.Value
                CoffeeRankingItem(
                    position = index + 1,
                    coffee = coffee,
                    reviewLabel = if (showingValue) {
                        strings.rankingValueOpinions
                    } else {
                        strings.detailReviews
                    },
                    awaitingReviewsLabel = if (showingValue) {
                        strings.rankingValueAwaiting
                    } else {
                        strings.coffeeAwaitingReviews
                    },
                    priceNotInformedLabel = strings.coffeePriceNotInformed,
                    displayedRating = if (showingValue) {
                        coffee.valueRating
                    } else {
                        coffee.rating
                    },
                    displayedReviewCount = if (showingValue) {
                        coffee.totalValueReviews
                    } else {
                        coffee.totalReviews
                    },
                    ratingPrefix = if (showingValue) {
                        strings.rankingValue
                    } else {
                        null
                    },
                    secondaryRatingText = if (
                        showingValue && coffee.hasRating
                    ) {
                        "${strings.detailAverageRating}: " +
                            "${formatRating(coffee.rating)} ★ • " +
                            "${coffee.totalReviews} ${strings.detailReviews}"
                    } else {
                        null
                    },
                    onClick = {
                        onOpenCoffee(coffee.id)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showBayesianInfo.value) {
        AlertDialog(
            onDismissRequest = {
                showBayesianInfo.value = false
            },
            title = {
                Text(text = strings.rankingBayesianMethod)
            },
            text = {
                Text(
                    text = if (
                        selectedFilter.value == RankingFilter.Value
                    ) {
                        strings.rankingValueBayesianExplanation
                    } else {
                        strings.rankingBayesianExplanation
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBayesianInfo.value = false
                    }
                ) {
                    Text(text = strings.commonBack)
                }
            }
        )
    }
}

@Composable
private fun RankingFilterChips(
    selectedFilter: RankingFilter,
    strings: AppStrings,
    onSelected: (RankingFilter) -> Unit
) {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RankingChip(
                modifier = Modifier.weight(0.8f),
                selected = selectedFilter == RankingFilter.Best,
                text = filterLabel(RankingFilter.Best, strings),
                onClick = {
                    onSelected(RankingFilter.Best)
                }
            )

            RankingChip(
                modifier = Modifier.weight(1.2f),
                selected = selectedFilter == RankingFilter.Value,
                text = filterLabel(RankingFilter.Value, strings),
                onClick = {
                    onSelected(RankingFilter.Value)
                }
            )
        }

        RankingChip(
            modifier = Modifier.fillMaxWidth(),
            selected = selectedFilter == RankingFilter.Reviews,
            text = filterLabel(RankingFilter.Reviews, strings),
            onClick = {
                onSelected(RankingFilter.Reviews)
            }
        )
    }
}

@Composable
private fun RankingChip(
    modifier: Modifier,
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
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
