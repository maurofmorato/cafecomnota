package com.maurofmorato.cafecomnota

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.data.admin.SupabaseAdminRepository
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.data.auth.SupabaseAuthRepository
import com.maurofmorato.cafecomnota.data.repository.CoffeeDataSource
import com.maurofmorato.cafecomnota.data.repository.CoffeeRepository
import com.maurofmorato.cafecomnota.ui.components.CafeBottomBar
import com.maurofmorato.cafecomnota.ui.i18n.AppLanguage
import com.maurofmorato.cafecomnota.ui.i18n.stringsFor
import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
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
import kotlinx.coroutines.launch

@Composable
fun CafeComNotaApp() {
    CafeComNotaTheme {
        val context = LocalContext.current

        val coffeeRepository = remember {
            CoffeeRepository()
        }

        val authRepository = remember {
            SupabaseAuthRepository(context)
        }

        val adminRepository = remember {
            SupabaseAdminRepository()
        }

        val coroutineScope = rememberCoroutineScope()

        var currentDestination by rememberSaveable {
            mutableStateOf(AppDestination.Home.name)
        }

        var selectedCoffeeId by rememberSaveable {
            mutableStateOf(sampleCoffees().first().id)
        }

        var currentLanguageName by rememberSaveable {
            mutableStateOf(AppLanguage.Portuguese.name)
        }

        var initialSearchQuery by rememberSaveable {
            mutableStateOf("")
        }

        var coffeesForUi by remember {
            mutableStateOf(sampleCoffees())
        }

        var coffeeDataSource by remember {
            mutableStateOf(CoffeeDataSource.LocalFallback)
        }

        var authSession by remember {
            mutableStateOf<AuthSession?>(null)
        }

        var isLoggingIn by remember {
            mutableStateOf(false)
        }

        var loginMessage by remember {
            mutableStateOf("")
        }

        var isAdmin by remember {
            mutableStateOf(false)
        }

        var adminMessage by remember {
            mutableStateOf("")
        }

        val currentLanguage = AppLanguage.valueOf(currentLanguageName)
        val strings = stringsFor(currentLanguage)
        val destination = AppDestination.valueOf(currentDestination)

        val selectedCoffee = findCoffeeById(
            id = selectedCoffeeId,
            coffees = coffeesForUi
        ) ?: coffeesForUi.firstOrNull()
            ?: sampleCoffees().first()

        fun reloadCoffees(
            source: String
        ) {
            coroutineScope.launch {
                val result = coffeeRepository.loadCoffees()

                coffeesForUi = ensureSafeCoffeeList(result.coffees)
                coffeeDataSource = result.source

                CafeAnalytics.logEvent(
                    eventName = "reload_coffees",
                    params = mapOf(
                        "source" to source,
                        "data_source" to result.source.name,
                        "count" to result.coffees.size,
                        "has_error" to (result.error != null)
                    )
                )

                result.error?.let { error ->
                    CafeAnalytics.recordNonFatal(
                        throwable = error,
                        params = mapOf(
                            "screen" to "app",
                            "action" to "reload_coffees",
                            "source" to source
                        )
                    )
                }
            }
        }

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
                    "language" to currentLanguage.code,
                    "data_source" to coffeeDataSource.name,
                    "logged_in" to (authSession != null)
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
                    "language" to currentLanguage.code,
                    "data_source" to coffeeDataSource.name
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

        fun searchFromHome(query: String) {
            val cleanedQuery = query.trim()
            initialSearchQuery = cleanedQuery

            CafeAnalytics.logEvent(
                eventName = AnalyticsEvents.SEARCH_COFFEE,
                params = mapOf(
                    "source" to "home",
                    "search_length" to cleanedQuery.length,
                    "has_query" to cleanedQuery.isNotBlank()
                )
            )

            navigateTo(
                newDestination = AppDestination.Search,
                source = "home_search"
            )
        }

        fun refreshAdminStatus(
            session: AuthSession?
        ) {
            isAdmin = false
            adminMessage = ""

            if (session == null) {
                return
            }

            coroutineScope.launch {
                try {
                    isAdmin = adminRepository.isCurrentUserAdmin(
                        accessToken = session.accessToken
                    )

                    adminMessage = if (isAdmin) {
                        "Administração ativa."
                    } else {
                        ""
                    }
                } catch (throwable: Throwable) {
                    isAdmin = false
                    adminMessage = throwable.message ?: "Não foi possível verificar permissão administrativa."

                    CafeAnalytics.recordNonFatal(
                        throwable = throwable,
                        params = mapOf(
                            "screen" to "app",
                            "action" to "refresh_admin_status"
                        )
                    )
                }
            }
        }

        fun doLogin(
            email: String,
            password: String
        ) {
            if (isLoggingIn) {
                return
            }

            val cleanedEmail = email.trim()

            if (cleanedEmail.isBlank() || password.isBlank()) {
                loginMessage = "Informe email e senha."
                return
            }

            isLoggingIn = true
            loginMessage = "Entrando..."

            coroutineScope.launch {
                try {
                    val session = authRepository.loginWithEmailPassword(
                        email = cleanedEmail,
                        password = password
                    )

                    authSession = session
                    refreshAdminStatus(session)
                    CafeAnalytics.setUserId(session.userId)

                    CafeAnalytics.logEvent(
                        eventName = "login_success",
                        params = mapOf(
                            "provider" to "email",
                            "email_domain" to session.email.substringAfter("@", "")
                        )
                    )

                    loginMessage = "Login realizado com sucesso."
                } catch (throwable: Throwable) {
                    CafeAnalytics.recordNonFatal(
                        throwable = throwable,
                        params = mapOf(
                            "screen" to "profile",
                            "action" to "login"
                        )
                    )

                    CafeAnalytics.logEvent(
                        eventName = "login_error",
                        params = mapOf(
                            "provider" to "email",
                            "message" to (throwable.message ?: "erro")
                        )
                    )

                    loginMessage = throwable.message ?: "Não foi possível fazer login."
                } finally {
                    isLoggingIn = false
                }
            }
        }

        fun doLogout() {
            authRepository.logout()
            authSession = null
            isAdmin = false
            adminMessage = ""
            loginMessage = "Você saiu da conta."

            CafeAnalytics.setUserId(null)
            CafeAnalytics.logEvent(
                eventName = "logout",
                params = mapOf(
                    "source" to "profile"
                )
            )
        }

        fun afterReviewSaved() {
            currentDestination = AppDestination.CoffeeDetail.name
            reloadCoffees(source = "review_saved")
        }

        fun afterCoffeeModerated() {
            currentDestination = AppDestination.Home.name
            reloadCoffees(source = "admin_moderated_coffee")
        }

        fun afterCoffeeCreated() {
            currentDestination = AppDestination.Home.name
            reloadCoffees(source = "coffee_created")
        }

        LaunchedEffect(Unit) {
            val savedSession = authRepository.getSavedSession()
            authSession = savedSession

            savedSession?.let { session ->
                CafeAnalytics.setUserId(session.userId)
                refreshAdminStatus(session)
                loginMessage = "Sessão restaurada."
            }

            val result = coffeeRepository.loadCoffees()

            coffeesForUi = ensureSafeCoffeeList(result.coffees)
            coffeeDataSource = result.source

            CafeAnalytics.logEvent(
                eventName = AnalyticsEvents.LOAD_COFFEES,
                params = mapOf(
                    "source" to result.source.name,
                    "count" to result.coffees.size,
                    "has_error" to (result.error != null)
                )
            )

            if (result.source == CoffeeDataSource.LocalFallback) {
                CafeAnalytics.logEvent(
                    eventName = AnalyticsEvents.LOAD_COFFEES_FALLBACK,
                    params = mapOf(
                        "reason" to (result.error?.message ?: "empty_result")
                    )
                )

                result.error?.let { error ->
                    CafeAnalytics.recordNonFatal(
                        throwable = error,
                        params = mapOf(
                            "screen" to "app_start",
                            "action" to "load_coffees",
                            "fallback" to true
                        )
                    )
                }
            }
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
                            onNavigate = { nextDestination ->
                                if (nextDestination == AppDestination.Search) {
                                    initialSearchQuery = ""
                                }

                                navigateTo(
                                    newDestination = nextDestination,
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
                        coffees = coffeesForUi,
                        dataSource = coffeeDataSource,
                        onNavigate = {
                            navigateTo(
                                newDestination = it,
                                source = "home"
                            )
                        },
                        onSearch = ::searchFromHome,
                        onOpenCoffee = ::openCoffeeDetail
                    )

                    AppDestination.Search -> SearchScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        coffees = coffeesForUi,
                        initialQuery = initialSearchQuery,
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
                        coffees = coffeesForUi,
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
                        authSession = authSession,
                        isLoggingIn = isLoggingIn,
                        loginMessage = loginMessage,
                        isAdmin = isAdmin,
                        onLanguageChange = ::changeLanguage,
                        onLogin = ::doLogin,
                        onLogout = ::doLogout,
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
                        authSession = authSession,
                        isAdmin = isAdmin,
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
                                    "language" to currentLanguage.code,
                                    "data_source" to coffeeDataSource.name,
                                    "logged_in" to (authSession != null)
                                )
                            )

                            navigateTo(
                                newDestination = AppDestination.ReviewCoffee,
                                source = "coffee_detail"
                            )
                        },
                        onCoffeeModerated = ::afterCoffeeModerated
                    )

                    AppDestination.ReviewCoffee -> ReviewCoffeeScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        coffeeId = selectedCoffee.id,
                        coffeeName = selectedCoffee.name,
                        authSession = authSession,
                        onBack = {
                            navigateTo(
                                newDestination = AppDestination.CoffeeDetail,
                                source = "review_back"
                            )
                        },
                        onSaved = ::afterReviewSaved
                    )

                    AppDestination.AddCoffee -> AddCoffeeScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        authSession = authSession,
                        isAdmin = isAdmin,
                        onBack = {
                            navigateTo(
                                newDestination = AppDestination.Home,
                                source = "add_coffee_back"
                            )
                        },
                        onSaved = ::afterCoffeeCreated
                    )
                }
            }
        }
    }
}

private fun ensureSafeCoffeeList(
    coffees: List<CoffeeUiModel>
): List<CoffeeUiModel> {
    return coffees.ifEmpty {
        sampleCoffees()
    }
}
