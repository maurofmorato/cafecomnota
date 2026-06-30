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
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import java.text.Normalizer
import kotlin.math.min

@Composable
fun SearchScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    coffees: List<CoffeeUiModel>,
    onNavigate: (AppDestination) -> Unit,
    onOpenCoffee: (String) -> Unit
) {
    val searchText = remember { mutableStateOf("") }

    val searchResults = remember(
        searchText.value,
        coffees
    ) {
        searchCoffees(
            coffees = coffees,
            query = searchText.value
        )
    }

    LaunchedEffect(searchText.value, searchResults.size) {
        val term = searchText.value.trim()

        if (term.length >= 2) {
            CafeAnalytics.logEvent(
                eventName = AnalyticsEvents.SEARCH_COFFEE,
                params = mapOf(
                    "search_length" to term.length,
                    "result_count" to searchResults.size,
                    "has_result" to searchResults.isNotEmpty()
                )
            )

            if (searchResults.isEmpty()) {
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
        CafeHeader(
            strings = strings,
            compact = true
        )

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = strings.searchScreenTitle)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchText.value,
            onValueChange = {
                searchText.value = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = strings.searchScreenPlaceholder,
                    color = Color.Gray
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = strings.navSearch,
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

        if (searchResults.isEmpty()) {
            Text(
                text = strings.searchNoCoffeeFound,
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
                    text = strings.commonRegisterNewCoffee,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else {
            searchResults.forEachIndexed { index, coffee ->
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

private fun searchCoffees(
    coffees: List<CoffeeUiModel>,
    query: String
): List<CoffeeUiModel> {
    val normalizedQuery = normalizeSearchText(query)

    if (normalizedQuery.isBlank()) {
        return coffees.sortedBy { coffee ->
            normalizeSearchText("${coffee.brand} ${coffee.name}")
        }
    }

    val terms = normalizedQuery
        .split(" ")
        .map { term ->
            term.trim()
        }
        .filter { term ->
            term.isNotBlank()
        }

    if (terms.isEmpty()) {
        return coffees
    }

    return coffees
        .mapNotNull { coffee ->
            val score = scoreCoffee(
                coffee = coffee,
                terms = terms
            )

            if (score > 0) {
                SearchResult(
                    coffee = coffee,
                    score = score
                )
            } else {
                null
            }
        }
        .sortedWith(
            compareByDescending<SearchResult> { result ->
                result.score
            }.thenByDescending { result ->
                result.coffee.totalReviews
            }.thenBy { result ->
                normalizeSearchText("${result.coffee.brand} ${result.coffee.name}")
            }
        )
        .map { result ->
            result.coffee
        }
}

private data class SearchResult(
    val coffee: CoffeeUiModel,
    val score: Int
)

private fun scoreCoffee(
    coffee: CoffeeUiModel,
    terms: List<String>
): Int {
    val searchableFields = listOf(
        coffee.name,
        coffee.brand,
        coffee.type,
        coffee.roast
    ) + coffee.tags

    val normalizedFields = searchableFields
        .map { field ->
            normalizeSearchText(field)
        }
        .filter { field ->
            field.isNotBlank()
        }

    var totalScore = 0

    terms.forEach { term ->
        val termScore = normalizedFields.maxOfOrNull { field ->
            scoreField(
                field = field,
                term = term
            )
        } ?: 0

        if (termScore <= 0) {
            return 0
        }

        totalScore += termScore
    }

    if (coffee.hasRating) {
        totalScore += 5
    }

    return totalScore
}

private fun scoreField(
    field: String,
    term: String
): Int {
    val words = field.split(" ")
        .filter { word ->
            word.isNotBlank()
        }

    if (field == term) {
        return 120
    }

    if (field.startsWith(term)) {
        return 100
    }

    if (words.any { word -> word == term }) {
        return 95
    }

    if (words.any { word -> word.startsWith(term) }) {
        return 85
    }

    // Para termos pequenos, não usamos "contains".
    // Isso evita que "Mel" encontre "Vermelho".
    if (term.length >= 4 && field.contains(term)) {
        return 45
    }

    // Tolerância a pequeno erro de digitação.
    // Ex.: "Mell" encontra "Melitta".
    if (term.length >= 4) {
        val fuzzyScore = words.maxOfOrNull { word ->
            scoreFuzzyPrefix(
                word = word,
                term = term
            )
        } ?: 0

        if (fuzzyScore > 0) {
            return fuzzyScore
        }
    }

    return 0
}

private fun scoreFuzzyPrefix(
    word: String,
    term: String
): Int {
    if (word.length < 3) {
        return 0
    }

    val comparedSize = min(
        word.length,
        term.length
    )

    if (comparedSize < 4) {
        return 0
    }

    val wordPrefix = word.take(comparedSize)
    val distance = levenshteinDistance(
        first = wordPrefix,
        second = term
    )

    return when {
        distance == 0 -> 75
        distance == 1 -> 65
        term.length >= 5 && distance == 2 -> 45
        else -> 0
    }
}

private fun normalizeSearchText(value: String): String {
    val withoutAccents = Normalizer
        .normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

    return withoutAccents
        .lowercase()
        .replace("ç", "c")
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")
}

private fun levenshteinDistance(
    first: String,
    second: String
): Int {
    if (first == second) {
        return 0
    }

    if (first.isEmpty()) {
        return second.length
    }

    if (second.isEmpty()) {
        return first.length
    }

    val previous = IntArray(second.length + 1) { index ->
        index
    }

    val current = IntArray(second.length + 1)

    for (i in 1..first.length) {
        current[0] = i

        for (j in 1..second.length) {
            val cost = if (first[i - 1] == second[j - 1]) {
                0
            } else {
                1
            }

            current[j] = minOf(
                current[j - 1] + 1,
                previous[j] + 1,
                previous[j - 1] + cost
            )
        }

        for (j in previous.indices) {
            previous[j] = current[j]
        }
    }

    return previous[second.length]
}
