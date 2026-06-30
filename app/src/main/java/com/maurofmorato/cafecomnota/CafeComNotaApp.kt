package com.maurofmorato.cafecomnota

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.ui.components.CafeBottomBar
import com.maurofmorato.cafecomnota.ui.i18n.AppLanguage
import com.maurofmorato.cafecomnota.ui.i18n.stringsFor
import com.maurofmorato.cafecomnota.ui.model.findCoffeeById
import com.maurofmorato.cafecomnota.ui.model.sampleCoffees
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.screens.AddCoffeeScreen
import com.maurofmorato.cafecomnota.ui.screens.CoffeeDetailScreen
import com.maurofmorato.cafecomnota.ui.screens.HomeScreen
import com.maurofmorato.cafecomnota.ui.screens.ProfileScreen
import com.maurofmorato.cafecomnota.ui.screens.RankingScreen
import com.maurofmorato.cafecomnota.ui.screens.ReviewCoffeeScreen
import com.maurofmorato.cafecomnota.ui.screens.SearchScreen
import com.maurofmorato.cafecomnota.ui.theme.CafeComNotaTheme
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCream

@Composable
fun CafeComNotaApp() {
    CafeComNotaTheme {
        var currentDestination by rememberSaveable {
            mutableStateOf(AppDestination.Home.name)
        }

        var selectedCoffeeId by rememberSaveable {
            mutableStateOf(sampleCoffees().first().id)
        }

        var currentLanguageName by rememberSaveable {
            mutableStateOf(AppLanguage.Portuguese.name)
        }

        val currentLanguage = AppLanguage.valueOf(currentLanguageName)
        val strings = stringsFor(currentLanguage)

        val destination = AppDestination.valueOf(currentDestination)

        val selectedCoffee = findCoffeeById(selectedCoffeeId)
            ?: sampleCoffees().first()

        fun navigateTo(
            newDestination: AppDestination,
            source: String = "app"
        ) {
            CafeAnalytics.logEvent(
                eventName = AnalyticsEvents.NAVIGATE,
                params = mapOf(
                    "from" to destination.analyticsName,
                    "to" to newDestination.analyticsName,
                    "source" to source,
                    "language" to currentLanguage.code
                )
            )

            currentDestination = newDestination.name
        }

        fun openCoffeeDetail(coffeeId: String) {
            selectedCoffeeId = coffeeId

            CafeAnalytics.logEvent(
                eventName = AnalyticsEvents.VIEW_COFFEE_DETAIL,
                params = mapOf(
                    "coffee_id" to coffeeId,
                    "source" to destination.analyticsName,
                    "language" to currentLanguage.code
                )
            )

            currentDestination = AppDestination.CoffeeDetail.name
        }

        fun changeLanguage(language: AppLanguage) {
            currentLanguageName = language.name

            CafeAnalytics.logEvent(
                eventName = AnalyticsEvents.CHANGE_LANGUAGE,
                params = mapOf(
                    "language" to language.code,
                    "language_name" to language.nativeName
                )
            )
        }

        LaunchedEffect(destination, currentLanguage) {
            CafeAnalytics.logScreen("${destination.analyticsName}_${currentLanguage.code}")
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CoffeeCream
        ) {
            Scaffold(
                containerColor = CoffeeCream,
                bottomBar = {
                    if (destination.showInBottomBar) {
                        CafeBottomBar(
                            currentDestination = destination,
                            strings = strings,
                            onNavigate = {
                                navigateTo(
                                    newDestination = it,
                                    source = "bottom_bar"
                                )
                            }
                        )
                    }
                }
            ) { innerPadding ->
                when (destination) {
                    AppDestination.Home -> HomeScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        onNavigate = {
                            navigateTo(
                                newDestination = it,
                                source = "home"
                            )
                        },
                        onOpenCoffee = ::openCoffeeDetail
                    )

                    AppDestination.Search -> SearchScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        onNavigate = {
                            navigateTo(
                                newDestination = it,
                                source = "search"
                            )
                        },
                        onOpenCoffee = ::openCoffeeDetail
                    )

                    AppDestination.Ranking -> RankingScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        onNavigate = {
                            navigateTo(
                                newDestination = it,
                                source = "ranking"
                            )
                        },
                        onOpenCoffee = ::openCoffeeDetail
                    )

                    AppDestination.Profile -> ProfileScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        currentLanguage = currentLanguage,
                        onLanguageChange = ::changeLanguage,
                        onNavigate = {
                            navigateTo(
                                newDestination = it,
                                source = "profile"
                            )
                        }
                    )

                    AppDestination.CoffeeDetail -> CoffeeDetailScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        coffee = selectedCoffee,
                        onBack = {
                            navigateTo(
                                newDestination = AppDestination.Home,
                                source = "coffee_detail_back"
                            )
                        },
                        onReview = {
                            CafeAnalytics.logEvent(
                                eventName = AnalyticsEvents.START_REVIEW,
                                params = mapOf(
                                    "coffee_id" to selectedCoffee.id,
                                    "coffee_name" to selectedCoffee.name,
                                    "language" to currentLanguage.code
                                )
                            )

                            navigateTo(
                                newDestination = AppDestination.ReviewCoffee,
                                source = "coffee_detail"
                            )
                        }
                    )

                    AppDestination.ReviewCoffee -> ReviewCoffeeScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        coffeeId = selectedCoffee.id,
                        coffeeName = selectedCoffee.name,
                        onBack = {
                            navigateTo(
                                newDestination = AppDestination.CoffeeDetail,
                                source = "review_back"
                            )
                        }
                    )

                    AppDestination.AddCoffee -> AddCoffeeScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        onBack = {
                            navigateTo(
                                newDestination = AppDestination.Home,
                                source = "add_coffee_back"
                            )
                        }
                    )
                }
            }
        }
    }
}
