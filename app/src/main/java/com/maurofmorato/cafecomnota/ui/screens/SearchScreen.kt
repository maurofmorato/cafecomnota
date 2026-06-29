package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.CoffeeRankingItem
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.model.sampleCoffees
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted

@Composable
fun SearchScreen(
    innerPadding: PaddingValues,
    onNavigate: (AppDestination) -> Unit,
    onOpenCoffee: (String) -> Unit
) {
    val searchText = remember { mutableStateOf("") }

    val filteredCoffees = sampleCoffees().filter { coffee ->
        val term = searchText.value.trim()
        term.isBlank() ||
            coffee.name.contains(term, ignoreCase = true) ||
            coffee.brand.contains(term, ignoreCase = true) ||
            coffee.type.contains(term, ignoreCase = true) ||
            coffee.roast.contains(term, ignoreCase = true)
    }

    LaunchedEffect(searchText.value, filteredCoffees.size) {
        val term = searchText.value.trim()

        if (term.length >= 2) {
            CafeAnalytics.logEvent(
                eventName = AnalyticsEvents.SEARCH_COFFEE,
                params = mapOf(
                    "search_length" to term.length,
                    "result_count" to filteredCoffees.size,
                    "has_result" to filteredCoffees.isNotEmpty()
                )
            )

            if (filteredCoffees.isEmpty()) {
                CafeAnalytics.logEvent(
                    eventName = AnalyticsEvents.SEARCH_NOT_FOUND,
                    params = mapOf(
                        "search_length" to term.length
                    )
                )
            }
        }
    }

    CafeResponsiveContent(
        innerPadding = innerPadding
    ) {
        CafeHeader(compact = true)

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Buscar café")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchText.value,
            onValueChange = {
                searchText.value = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Digite café, marca ou torrefação",
                    color = Color.Gray
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Pesquisar",
                    tint = CoffeeBrown
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

        Spacer(modifier = Modifier.height(18.dp))

        if (filteredCoffees.isEmpty()) {
            Text(
                text = "Nenhum café encontrado.",
                color = CoffeeMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    CafeAnalytics.logEvent(
                        eventName = AnalyticsEvents.START_ADD_COFFEE,
                        params = mapOf(
                            "source" to "search_not_found"
                        )
                    )

                    onNavigate(AppDestination.AddCoffee)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Text(
                    text = "Cadastrar café novo",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else {
            filteredCoffees.forEachIndexed { index, coffee ->
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
}
