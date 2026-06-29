package com.maurofmorato.cafecomnota.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CoffeeBrown = Color(0xFF4A2613)
val CoffeeBrownDark = Color(0xFF2B160C)
val CoffeeGold = Color(0xFFC28A2E)
val CoffeeCream = Color(0xFFFFF8EC)
val CoffeeCard = Color(0xFFFFFCF6)
val CoffeeLine = Color(0xFFE7D3B4)
val CoffeeText = Color(0xFF2E1A10)
val CoffeeMuted = Color(0xFF7B604E)

private val CafeColorScheme = lightColorScheme(
    primary = CoffeeBrown,
    secondary = CoffeeGold,
    background = CoffeeCream,
    surface = CoffeeCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = CoffeeText,
    onSurface = CoffeeText
)

@Composable
fun CafeComNotaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CafeColorScheme,
        content = content
    )
}
