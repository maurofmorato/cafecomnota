package com.maurofmorato.cafecomnota

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.maurofmorato.cafecomnota.ui.components.CafeBottomBar
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.screens.AddCoffeeScreen
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

        val destination = AppDestination.valueOf(currentDestination)

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
                                currentDestination = it.name
                            }
                        )
                    }
                }
            ) { innerPadding ->
                when (destination) {
                    AppDestination.Home -> HomeScreen(
                        innerPadding = innerPadding,
                        onNavigate = {
                            currentDestination = it.name
                        }
                    )

                    AppDestination.Search -> SearchScreen(
                        innerPadding = innerPadding,
                        onNavigate = {
                            currentDestination = it.name
                        }
                    )

                    AppDestination.Ranking -> RankingScreen(
                        innerPadding = innerPadding,
                        onNavigate = {
                            currentDestination = it.name
                        }
                    )

                    AppDestination.Profile -> ProfileScreen(
                        innerPadding = innerPadding,
                        onNavigate = {
                            currentDestination = it.name
                        }
                    )

                    AppDestination.ReviewCoffee -> ReviewCoffeeScreen(
                        innerPadding = innerPadding,
                        onBack = {
                            currentDestination = AppDestination.Home.name
                        }
                    )

                    AppDestination.AddCoffee -> AddCoffeeScreen(
                        innerPadding = innerPadding,
                        onBack = {
                            currentDestination = AppDestination.Home.name
                        }
                    )
                }
            }
        }
    }
}
