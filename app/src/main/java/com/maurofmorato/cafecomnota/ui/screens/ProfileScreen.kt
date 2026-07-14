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
import androidx.compose.material.icons.filled.Password
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
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
    isAdmin: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onLogin: (String, String) -> Unit,
    onGoogleLogin: () -> Unit,
    onRequestPasswordReset: (String) -> Unit,
    onChangePassword: (String, String) -> Unit,
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

        Spacer(modifier = Modifier.height(18.dp))

        SectionTitle(title = "Perfil")

        Spacer(modifier = Modifier.height(10.dp))

        if (authSession == null) {
            AuthCard(
                isLoggingIn = isLoggingIn,
                loginMessage = loginMessage,
                onLogin = onLogin,
                onGoogleLogin = onGoogleLogin,
                onRequestPasswordReset = onRequestPasswordReset
            )
        } else {
            AccountCard(
                authSession = authSession,
                isWorking = isLoggingIn,
                message = loginMessage,
                onChangePassword = onChangePassword,
                onLogout = onLogout
            )
        }

        if (authSession != null && isAdmin) {
            Spacer(modifier = Modifier.height(18.dp))

            SectionTitle(title = "Administração")

            Spacer(modifier = Modifier.height(10.dp))

            AdminCard()
        }

        Spacer(modifier = Modifier.height(18.dp))

        SectionTitle(title = "Privacidade e conta")

        Spacer(modifier = Modifier.height(10.dp))

        PrivacyCard(
            canRequestDeletion = authSession != null
        )

        Spacer(modifier = Modifier.height(18.dp))

        SectionTitle(title = "Idioma")

        Spacer(modifier = Modifier.height(10.dp))

        LanguageCard(
            currentLanguage = currentLanguage,
            onLanguageChange = onLanguageChange
        )

        Spacer(modifier = Modifier.height(18.dp))

        SectionTitle(title = "Conta e segurança")

        Spacer(modifier = Modifier.height(10.dp))

        InfoCard(
            title = "Conta e segurança",
            text = "Você pode entrar com e-mail e senha ou Google, recuperar a senha por e-mail e manter seus dados atualizados."
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun PrivacyCard(
    canRequestDeletion: Boolean
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "Seus dados",
                color = CoffeeBrownDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Veja como o Café com nota trata seus dados e quais informações ficam associadas à sua conta.",
                color = CoffeeMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )

            TextButton(
                onClick = {
                    uriHandler.openUri(PRIVACY_POLICY_URL)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ler Política de Privacidade")
            }

            if (canRequestDeletion) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Se desejar encerrar sua conta, você pode solicitar a exclusão dos dados associados a ela.",
                    color = CoffeeMuted,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )

                OutlinedButton(
                    onClick = {
                        uriHandler.openUri(ACCOUNT_DELETION_URL)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solicitar exclusão da conta")
                }
            }
        }
    }
}

private const val PRIVACY_POLICY_URL = "https://maurofmorato.github.io/cafecomnota/"
private const val ACCOUNT_DELETION_URL = "https://maurofmorato.github.io/cafecomnota/#exclusao-de-conta"

@Composable
private fun AuthCard(
    isLoggingIn: Boolean,
    loginMessage: String,
    onLogin: (String, String) -> Unit,
    onGoogleLogin: () -> Unit,
    onRequestPasswordReset: (String) -> Unit
) {
    val email = remember {
        mutableStateOf("")
    }

    val password = remember {
        mutableStateOf("")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    tint = CoffeeBrown
                )

                Text(
                    text = "Entrar na conta",
                    color = CoffeeBrownDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Entre com e-mail/senha ou use o Google. Para recuperar senha, informe seu e-mail e toque em Esqueci minha senha.",
                color = CoffeeMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(10.dp))

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
                    text = if (isLoggingIn) "Aguarde..." else "Entrar",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onGoogleLogin,
                enabled = !isLoggingIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null
                )

                Text(
                    text = "Entrar com Google",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            TextButton(
                onClick = {
                    onRequestPasswordReset(email.value)
                },
                enabled = !isLoggingIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Esqueci minha senha")
            }

            if (loginMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = loginMessage,
                    color = CoffeeBrown,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun AccountCard(
    authSession: AuthSession,
    isWorking: Boolean,
    message: String,
    onChangePassword: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    val newPassword = remember {
        mutableStateOf("")
    }

    val confirmPassword = remember {
        mutableStateOf("")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CoffeeBrown
                )

                Text(
                    text = "Conta conectada",
                    color = CoffeeBrownDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = authSession.email,
                color = CoffeeText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Você pode alterar sua senha aqui. Se veio por um link de recuperação, informe a nova senha e salve.",
                color = CoffeeMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Password,
                    contentDescription = null,
                    tint = CoffeeBrown
                )

                Text(
                    text = "Alterar senha",
                    color = CoffeeBrownDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newPassword.value,
                onValueChange = {
                    newPassword.value = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nova senha")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = CoffeeGold,
                    unfocusedIndicatorColor = CoffeeLine,
                    cursorColor = CoffeeBrown
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = {
                    confirmPassword.value = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Confirmar nova senha")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = CoffeeGold,
                    unfocusedIndicatorColor = CoffeeLine,
                    cursorColor = CoffeeBrown
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onChangePassword(
                        newPassword.value,
                        confirmPassword.value
                    )
                },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isWorking) "Salvando..." else "Salvar nova senha")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onLogout,
                enabled = !isWorking,
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

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = CoffeeBrown,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun AdminCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CoffeeBrown
                )

                Text(
                    text = "Administração ativa",
                    color = CoffeeBrownDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Este usuário pode moderar cafés pelo celular. Abra o detalhe de um café para ocultar ou marcar como pendente.",
                color = CoffeeMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(14.dp)
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CoffeeCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                color = CoffeeBrownDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                color = CoffeeMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        }
    }
}
