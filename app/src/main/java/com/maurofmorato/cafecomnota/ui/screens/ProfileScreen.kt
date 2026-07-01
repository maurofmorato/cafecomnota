package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.i18n.AppLanguage
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import com.maurofmorato.cafecomnota.ui.theme.CoffeeText

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    currentLanguage: AppLanguage,
    authSession: AuthSession?,
    isLoggingIn: Boolean,
    loginMessage: String,
    onLanguageChange: (AppLanguage) -> Unit,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
    onNavigate: (AppDestination) -> Unit
) {
    CafeResponsiveContent(
        innerPadding = innerPadding
    ) {
        CafeHeader(
            strings = strings,
            compact = true
        )

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Perfil")

        Spacer(modifier = Modifier.height(12.dp))

        AuthCard(
            authSession = authSession,
            isLoggingIn = isLoggingIn,
            loginMessage = loginMessage,
            onLogin = onLogin,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Idioma")

        Spacer(modifier = Modifier.height(12.dp))

        LanguageCard(
            currentLanguage = currentLanguage,
            onLanguageChange = onLanguageChange
        )

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Próximos recursos")

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            title = "Conta e dados reais",
            text = "O login é a base para salvar avaliações, informar preços e cadastrar cafés com segurança."
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun AuthCard(
    authSession: AuthSession?,
    isLoggingIn: Boolean,
    loginMessage: String,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    val email = remember {
        mutableStateOf("")
    }

    val password = remember {
        mutableStateOf("")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (authSession == null) Icons.Default.Login else Icons.Default.Person,
                    contentDescription = null,
                    tint = CoffeeBrown
                )

                Text(
                    text = if (authSession == null) "Entrar na conta" else "Conta conectada",
                    color = CoffeeBrownDark,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (authSession == null) {
                Text(
                    text = "Entre com o usuário criado no Supabase Auth. Depois vamos usar essa sessão para salvar preços e avaliações.",
                    color = CoffeeMuted,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email.value,
                    onValueChange = {
                        email.value = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Email")
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = CoffeeGold,
                        unfocusedIndicatorColor = CoffeeLine,
                        cursorColor = CoffeeBrown
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password.value,
                    onValueChange = {
                        password.value = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Senha")
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = CoffeeGold,
                        unfocusedIndicatorColor = CoffeeLine,
                        cursorColor = CoffeeBrown
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onLogin(
                            email.value,
                            password.value
                        )
                    },
                    enabled = !isLoggingIn,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null
                    )

                    Text(
                        text = if (isLoggingIn) "Entrando..." else "Entrar",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                Text(
                    text = authSession.email,
                    color = CoffeeText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Usuário autenticado. Pronto para as próximas etapas de gravação no Supabase.",
                    color = CoffeeMuted,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null
                    )

                    Text(
                        text = "Sair",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (loginMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = loginMessage,
                    color = CoffeeBrown,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun LanguageCard(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = CoffeeBrown
                )

                Text(
                    text = "Idioma do aplicativo",
                    color = CoffeeBrownDark,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AppLanguage.values().forEach { language ->
                FilterChip(
                    selected = currentLanguage == language,
                    onClick = {
                        onLanguageChange(language)
                    },
                    label = {
                        Text(language.nativeName)
                    },
                    modifier = Modifier.padding(
                        end = 8.dp,
                        bottom = 8.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = CoffeeBrownDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                color = CoffeeMuted,
                fontSize = 14.sp
            )
        }
    }
}
