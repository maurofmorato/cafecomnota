package com.maurofmorato.cafecomnota.ui.navigation

enum class AppDestination(
    val label: String,
    val showInBottomBar: Boolean
) {
    Home(
        label = "Início",
        showInBottomBar = true
    ),

    Search(
        label = "Buscar",
        showInBottomBar = true
    ),

    Ranking(
        label = "Ranking",
        showInBottomBar = true
    ),

    Profile(
        label = "Perfil",
        showInBottomBar = true
    ),

    ReviewCoffee(
        label = "Avaliar",
        showInBottomBar = false
    ),

    AddCoffee(
        label = "Cadastrar",
        showInBottomBar = false
    )
}
