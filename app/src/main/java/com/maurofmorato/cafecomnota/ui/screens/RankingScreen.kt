package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CoffeeRankingItem
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.model.bestValueCoffees
import com.maurofmorato.cafecomnota.ui.model.mostReviewedCoffees
import com.maurofmorato.cafecomnota.ui.model.topRatedCoffees
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination

private enum class RankingFilter(
    val label: String
) {
    Best("Melhores"),
    Value("Custo-benefício"),
    Reviews("Mais avaliados")
}

@Composable
fun RankingScreen(
    innerPadding: PaddingValues,
    onNavigate: (AppDestination) -> Unit,
    onOpenCoffee: (String) -> Unit
) {
    val selectedFilter = remember {
        mutableStateOf(RankingFilter.Best)
    }

    val coffees = when (selectedFilter.value) {
        RankingFilter.Best -> topRatedCoffees()
        RankingFilter.Value -> bestValueCoffees()
        RankingFilter.Reviews -> mostReviewedCoffees()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 18.dp)
    ) {
        CafeHeader(compact = true)

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Ranking")

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
                    },
                    label = {
                        Text(filter.label)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        coffees.forEachIndexed { index, coffee ->
            CoffeeRankingItem(
                position = index + 1,
                coffee = coffee,
                onClick = {
                    onOpenCoffee(coffee.id)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
