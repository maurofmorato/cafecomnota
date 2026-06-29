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
                    "source" to source
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
                    "source" to destination.analyticsName
                )
            )

            currentDestination = AppDestination.CoffeeDetail.name
        }

        LaunchedEffect(destination) {
            CafeAnalytics.logScreen(destination.analyticsName)
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
                        onNavigate = {
                            navigateTo(
                                newDestination = it,
                                source = "profile"
                            )
                        }
                    )

                    AppDestination.CoffeeDetail -> CoffeeDetailScreen(
                        innerPadding = innerPadding,
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
                                    "coffee_name" to selectedCoffee.name
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
