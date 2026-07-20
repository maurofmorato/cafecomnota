package com.maurofmorato.cafecomnota

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
fun CafeComNotaApp(
    authDeepLink: String? = null,
    onAuthDeepLinkConsumed: () -> Unit = {},
    updateReadyToInstall: Boolean = false,
    onInstallUpdate: () -> Unit = {},
    onDismissUpdate: () -> Unit = {}
) {
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
        val saveableStateHolder = rememberSaveableStateHolder()

        var currentDestination by rememberSaveable {
            mutableStateOf(AppDestination.Home.name)
        }

        val navigationBackStack = remember {
            mutableStateListOf<String>()
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

        var pendingLoginDestinationName by rememberSaveable {
            mutableStateOf<String?>(null)
        }

        var showLoginRequiredDialog by rememberSaveable {
            mutableStateOf(false)
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
            if (newDestination == destination) {
                return
            }

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

            if (source == "bottom_bar") {
                navigationBackStack.clear()
            } else {
                navigationBackStack.add(currentDestination)
            }

            currentDestination = newDestination.name
        }

        fun navigateBack(source: String = "system_back") {
            val previousDestination = navigationBackStack.removeLastOrNull()
                ?: AppDestination.Home.name

            CafeAnalytics.logEvent(
                eventName = AnalyticsEvents.NAVIGATE,
                params = mapOf(
                    "from" to destination.analyticsName,
                    "to" to AppDestination.valueOf(previousDestination).analyticsName,
                    "source" to source,
                    "language" to currentLanguage.code,
                    "data_source" to coffeeDataSource.name,
                    "logged_in" to (authSession != null)
                )
            )

            currentDestination = previousDestination
        }

        fun resumePendingActionAfterLogin() {
            val pendingDestination = pendingLoginDestinationName ?: return

            if (navigationBackStack.lastOrNull() == pendingDestination) {
                navigationBackStack.removeLastOrNull()
            }

            pendingLoginDestinationName = null
            currentDestination = pendingDestination
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

            navigateTo(
                newDestination = AppDestination.CoffeeDetail,
                source = "open_coffee"
            )
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
                    resumePendingActionAfterLogin()
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

        fun requireLogin(
            message: String,
            continueTo: AppDestination
        ) {
            loginMessage = message
            pendingLoginDestinationName = continueTo.name
            showLoginRequiredDialog = true
        }

        fun handleExpiredSession(continueTo: AppDestination) {
            authRepository.logout()
            authSession = null
            isAdmin = false
            adminMessage = ""
            CafeAnalytics.setUserId(null)
            requireLogin(
                message = "Sua sessão expirou. Entre novamente para continuar. Os dados preenchidos serão preservados.",
                continueTo = continueTo
            )
        }

        fun doGoogleLogin() {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(authRepository.buildGoogleLoginUrl())
                )

                context.startActivity(intent)
                loginMessage = "Abrindo login do Google..."

                CafeAnalytics.logEvent(
                    eventName = "google_login_start",
                    params = mapOf(
                        "provider" to "google"
                    )
                )
            } catch (throwable: Throwable) {
                loginMessage = throwable.message ?: "Não foi possível abrir o login do Google."

                CafeAnalytics.recordNonFatal(
                    throwable = throwable,
                    params = mapOf(
                        "screen" to "profile",
                        "action" to "google_login_start"
                    )
                )
            }
        }

        fun doRequestPasswordReset(
            email: String
        ) {
            if (isLoggingIn) {
                return
            }

            val cleanedEmail = email.trim()

            if (cleanedEmail.isBlank()) {
                loginMessage = "Informe o e-mail para recuperar a senha."
                return
            }

            isLoggingIn = true
            loginMessage = "Enviando recuperação de senha..."

            coroutineScope.launch {
                try {
                    authRepository.requestPasswordReset(cleanedEmail)

                    loginMessage = "Enviamos um link para seu e-mail. Abra o link no celular e informe a nova senha no app."

                    CafeAnalytics.logEvent(
                        eventName = "password_reset_requested",
                        params = mapOf(
                            "email_domain" to cleanedEmail.substringAfter("@", "")
                        )
                    )
                } catch (throwable: Throwable) {
                    loginMessage = throwable.message ?: "Não foi possível enviar recuperação de senha."

                    CafeAnalytics.recordNonFatal(
                        throwable = throwable,
                        params = mapOf(
                            "screen" to "profile",
                            "action" to "request_password_reset"
                        )
                    )
                } finally {
                    isLoggingIn = false
                }
            }
        }

        fun doChangePassword(
            newPassword: String,
            confirmPassword: String
        ) {
            if (isLoggingIn) {
                return
            }

            val session = authSession

            if (session == null) {
                loginMessage = "Entre na conta antes de alterar a senha."
                return
            }

            if (newPassword.length < 6) {
                loginMessage = "A nova senha precisa ter pelo menos 6 caracteres."
                return
            }

            if (newPassword != confirmPassword) {
                loginMessage = "A confirmação da senha não confere."
                return
            }

            isLoggingIn = true
            loginMessage = "Alterando senha..."

            coroutineScope.launch {
                try {
                    authRepository.updatePassword(
                        accessToken = session.accessToken,
                        newPassword = newPassword
                    )

                    loginMessage = "Senha alterada com sucesso."

                    CafeAnalytics.logEvent(
                        eventName = "password_changed",
                        params = mapOf(
                            "source" to "profile"
                        )
                    )
                } catch (throwable: Throwable) {
                    loginMessage = throwable.message ?: "Não foi possível alterar a senha."

                    CafeAnalytics.recordNonFatal(
                        throwable = throwable,
                        params = mapOf(
                            "screen" to "profile",
                            "action" to "change_password"
                        )
                    )
                } finally {
                    isLoggingIn = false
                }
            }
        }

        fun afterReviewSaved() {
            saveableStateHolder.removeState("${AppDestination.ReviewCoffee.name}:$selectedCoffeeId")
            navigateBack(source = "review_saved")
            reloadCoffees(source = "review_saved")
        }

        fun afterCoffeeModerated() {
            currentDestination = AppDestination.Home.name
            reloadCoffees(source = "admin_moderated_coffee")
        }

        fun afterCoffeeCreated() {
            saveableStateHolder.removeState(AppDestination.AddCoffee.name)
            navigateBack(source = "coffee_created")
            reloadCoffees(source = "coffee_created")
        }

        LaunchedEffect(authDeepLink) {
            val deepLink = authDeepLink

            if (!deepLink.isNullOrBlank()) {
                val uri = Uri.parse(deepLink)

                if (uri.host == "coffee") {
                    uri.pathSegments.firstOrNull()?.let { coffeeId ->
                        selectedCoffeeId = coffeeId
                        navigationBackStack.clear()
                        currentDestination = AppDestination.CoffeeDetail.name
                    }

                    onAuthDeepLinkConsumed()
                    return@LaunchedEffect
                }

                isLoggingIn = true
                loginMessage = "Recebendo retorno de autenticação..."

                try {
                    val session = authRepository.loginFromDeepLink(deepLink)

                    authSession = session
                    refreshAdminStatus(session)
                    CafeAnalytics.setUserId(session.userId)

                    loginMessage = if (deepLink.contains("reset-password")) {
                        "Link de recuperação recebido. Informe sua nova senha abaixo."
                    } else {
                        "Login realizado com sucesso."
                    }

                    if (!deepLink.contains("reset-password")) {
                        resumePendingActionAfterLogin()
                    }

                    CafeAnalytics.logEvent(
                        eventName = "auth_deeplink_success",
                        params = mapOf(
                            "has_reset" to deepLink.contains("reset-password")
                        )
                    )
                } catch (throwable: Throwable) {
                    loginMessage = throwable.message ?: "Não foi possível concluir a autenticação."

                    CafeAnalytics.recordNonFatal(
                        throwable = throwable,
                        params = mapOf(
                            "screen" to "app",
                            "action" to "auth_deeplink"
                        )
                    )
                } finally {
                    isLoggingIn = false
                    onAuthDeepLinkConsumed()
                }
            }
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

        BackHandler(enabled = destination != AppDestination.Home) {
            navigateBack()
        }

        if (updateReadyToInstall) {
            AlertDialog(
                onDismissRequest = onDismissUpdate,
                title = { Text("Atualização pronta") },
                text = {
                    Text("Uma nova versão do Café com nota foi baixada. Reinicie agora para usar as melhorias.")
                },
                confirmButton = {
                    Button(onClick = onInstallUpdate) {
                        Text("Atualizar agora")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissUpdate) {
                        Text("Depois")
                    }
                }
            )
        }

        if (showLoginRequiredDialog) {
            AlertDialog(
                onDismissRequest = {
                    showLoginRequiredDialog = false
                    pendingLoginDestinationName = null
                },
                title = { Text("Entre para continuar") },
                text = {
                    Text(
                        "Para cadastrar cafés e publicar avaliações, você precisa entrar na sua conta. " +
                            "Depois do login, voltaremos automaticamente para o que você estava fazendo."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLoginRequiredDialog = false
                            navigateTo(
                                newDestination = AppDestination.Profile,
                                source = "login_required"
                            )
                        }
                    ) {
                        Text("Entrar agora")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showLoginRequiredDialog = false
                            pendingLoginDestinationName = null
                        }
                    ) {
                        Text("Agora não")
                    }
                }
            )
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
                val destinationStateKey = when (destination) {
                    AppDestination.CoffeeDetail,
                    AppDestination.ReviewCoffee -> "${destination.name}:$selectedCoffeeId"

                    else -> destination.name
                }

                saveableStateHolder.SaveableStateProvider(destinationStateKey) {
                    when (destination) {
                    AppDestination.Home -> HomeScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        coffees = coffeesForUi,
                        onNavigate = {
                            when {
                                authSession == null && it == AppDestination.ReviewCoffee -> {
                                    requireLogin(
                                        message = "Entre para dar sua nota a um café.",
                                        continueTo = it
                                    )
                                }

                                authSession == null && it == AppDestination.AddCoffee -> {
                                    requireLogin(
                                        message = "Entre para cadastrar um novo café.",
                                        continueTo = it
                                    )
                                }

                                else -> navigateTo(
                                    newDestination = it,
                                    source = "home"
                                )
                            }
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
                        onGoogleLogin = ::doGoogleLogin,
                        onRequestPasswordReset = ::doRequestPasswordReset,
                        onChangePassword = ::doChangePassword,
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
                            navigateBack(source = "coffee_detail_back")
                        },
                        onReview = {
                            if (authSession == null) {
                                requireLogin(
                                    message = "Entre para dar sua nota a este café.",
                                    continueTo = AppDestination.ReviewCoffee
                                )
                            } else {
                                CafeAnalytics.logEvent(
                                    eventName = AnalyticsEvents.START_REVIEW,
                                    params = mapOf(
                                        "coffee_id" to selectedCoffee.id,
                                        "coffee_name" to selectedCoffee.name,
                                        "language" to currentLanguage.code,
                                        "data_source" to coffeeDataSource.name,
                                        "logged_in" to true
                                    )
                                )

                                navigateTo(
                                    newDestination = AppDestination.ReviewCoffee,
                                    source = "coffee_detail"
                                )
                            }
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
                            navigateBack(source = "review_back")
                        },
                        onSaved = ::afterReviewSaved,
                        onRequireLogin = {
                            if (authSession == null) {
                                requireLogin(
                                    message = "Entre para salvar sua avaliação.",
                                    continueTo = AppDestination.ReviewCoffee
                                )
                            } else {
                                handleExpiredSession(AppDestination.ReviewCoffee)
                            }
                        }
                    )

                    AppDestination.AddCoffee -> AddCoffeeScreen(
                        innerPadding = innerPadding,
                        strings = strings,
                        authSession = authSession,
                        isAdmin = isAdmin,
                        existingCoffees = coffeesForUi,
                        onBack = {
                            navigateBack(source = "add_coffee_back")
                        },
                        onSaved = ::afterCoffeeCreated,
                        onOpenExistingCoffee = ::openCoffeeDetail,
                        onRequireLogin = { message ->
                            if (authSession == null) {
                                requireLogin(
                                    message = message,
                                    continueTo = AppDestination.AddCoffee
                                )
                            } else {
                                handleExpiredSession(AppDestination.AddCoffee)
                            }
                        }
                    )
                    }
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
