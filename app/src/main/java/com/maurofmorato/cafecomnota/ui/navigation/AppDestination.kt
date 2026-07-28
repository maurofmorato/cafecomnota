package com.maurofmorato.cafecomnota.ui.navigation

enum class AppDestination(
    val label: String,
    val showInBottomBar: Boolean,
    val analyticsName: String
) {
    Home(
        label = "Início",
        showInBottomBar = true,
        analyticsName = "home"
    ),

    Search(
        label = "Buscar",
        showInBottomBar = true,
        analyticsName = "search"
    ),

    Ranking(
        label = "Ranking",
        showInBottomBar = true,
        analyticsName = "ranking"
    ),

    Profile(
        label = "Perfil",
        showInBottomBar = true,
        analyticsName = "profile"
    ),

    CoffeeDetail(
        label = "Detalhe",
        showInBottomBar = false,
        analyticsName = "coffee_detail"
    ),

    ReviewCoffee(
        label = "Avaliar",
        showInBottomBar = false,
        analyticsName = "review_coffee"
    ),

    AddCoffee(
        label = "Cadastrar",
        showInBottomBar = false,
        analyticsName = "add_coffee"
    ),

    MyContributions(
        label = "Contribuições",
        showInBottomBar = false,
        analyticsName = "my_contributions"
    ),

    CoffeeAdministration(
        label = "Administração",
        showInBottomBar = false,
        analyticsName = "coffee_administration"
    ),

    CoffeeAdministrationDetail(
        label = "Conferir café",
        showInBottomBar = false,
        analyticsName = "coffee_administration_detail"
    )
}
