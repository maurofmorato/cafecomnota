package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.components.SubScreenHero
import com.maurofmorato.cafecomnota.ui.components.responsiveTextSize
import com.maurofmorato.cafecomnota.ui.i18n.AppLanguage
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeGold
import com.maurofmorato.cafecomnota.ui.theme.CoffeeLine
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted

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
    val uriHandler = LocalUriHandler.current
    val showPasswordFields = remember { mutableStateOf(false) }
    val showLanguageChoices = remember { mutableStateOf(false) }
    val copy = profileCopyFor(currentLanguage)

    CafeResponsiveContent(
        innerPadding = innerPadding
    ) {
        if (authSession == null) {
            SubScreenHero(
                strings = strings,
                title = strings.profileTitle,
                subtitle = strings.profileVisitorInfo
            )
            Spacer(modifier = Modifier.height(10.dp))
            AuthCard(
                isLoggingIn = isLoggingIn,
                loginMessage = loginMessage,
                onLogin = onLogin,
                onGoogleLogin = onGoogleLogin,
                onRequestPasswordReset = onRequestPasswordReset
            )
        } else {
            ProfilePageHeading(title = strings.profileTitle)
            Spacer(modifier = Modifier.height(14.dp))

            ProfileIdentityCard(authSession = authSession, copy = copy)

            if (isAdmin) {
                Spacer(modifier = Modifier.height(14.dp))
                ModeratorToolsCard(
                    copy = copy,
                    onOpenAdministration = { onNavigate(AppDestination.CoffeeAdministration) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(title = copy.activity)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileMenuCard {
                ProfileMenuRow(
                    icon = Icons.Default.LocalCafe,
                    title = copy.myCoffees,
                    subtitle = copy.followSubmissions,
                    onClick = { onNavigate(AppDestination.MyContributions) }
                )
                HorizontalDivider(color = CoffeeLine)
                ProfileMenuRow(
                    icon = Icons.Default.Star,
                    title = copy.myReviews,
                    subtitle = copy.reviewsSoon
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(title = copy.preferences)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileMenuCard {
                ProfileMenuRow(
                    icon = Icons.Default.Language,
                    title = strings.profileLanguage,
                    trailingText = currentLanguage.nativeName,
                    onClick = { showLanguageChoices.value = !showLanguageChoices.value }
                )
                if (showLanguageChoices.value) {
                    HorizontalDivider(color = CoffeeLine)
                    Column(modifier = Modifier.padding(12.dp)) {
                        AppLanguage.values().forEach { language ->
                            FilterChip(
                                selected = currentLanguage == language,
                                onClick = {
                                    onLanguageChange(language)
                                    showLanguageChoices.value = false
                                },
                                label = { Text(language.nativeName) },
                                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = CoffeeLine)
                ProfileMenuRow(
                    icon = Icons.Default.Lock,
                    title = copy.privacy,
                    onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(title = copy.accountSecurity)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileSecurityCard(
                isWorking = isLoggingIn,
                message = loginMessage,
                showPasswordFields = showPasswordFields.value,
                onTogglePasswordFields = { showPasswordFields.value = !showPasswordFields.value },
                onChangePassword = onChangePassword,
                onLogout = onLogout,
                onRequestDeletion = { uriHandler.openUri(ACCOUNT_DELETION_URL) },
                copy = copy
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ProfilePageHeading(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Default.LocalCafe, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(34.dp))
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.width(1.dp).height(40.dp).background(CoffeeGold.copy(alpha = 0.7f))
        )
        Text(
            text = title,
            color = CoffeeBrownDark,
            fontSize = responsiveTextSize(34f, 25f),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
    }
}

@Composable
private fun ProfileIdentityCard(authSession: AuthSession, copy: ProfileCopy) {
    val initial = authSession.email.trim().firstOrNull()?.uppercase() ?: "C"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(38.dp),
                colors = CardDefaults.cardColors(containerColor = CoffeeGold)
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(initial, color = CoffeeCard, fontSize = responsiveTextSize(40f, 30f), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(copy.connectedAccount, color = CoffeeBrownDark, fontSize = responsiveTextSize(21f, 16f), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(authSession.email, color = CoffeeMuted, fontSize = responsiveTextSize(14f, 11f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Text("●  ${copy.connected}", color = CoffeeBrown, fontSize = responsiveTextSize(13f, 11f), fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun ModeratorToolsCard(copy: ProfileCopy, onOpenAdministration: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenAdministration),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(34.dp))
                Column(Modifier.weight(1f)) {
                    Text(copy.moderatorTools, color = CoffeeBrownDark, fontSize = responsiveTextSize(20f, 16f), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(copy.moderatorInfo, color = CoffeeMuted, fontSize = responsiveTextSize(13f, 11f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = CoffeeGold.copy(alpha = 0.55f))
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(copy.openReviewQueue, color = CoffeeBrown, fontSize = responsiveTextSize(16f, 13f), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = CoffeeBrown)
            }
        }
    }
}

@Composable
private fun ProfileMenuCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(28.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = CoffeeBrownDark, fontSize = responsiveTextSize(18f, 14f), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, color = CoffeeMuted, fontSize = responsiveTextSize(13f, 11f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (trailingText != null) Text(trailingText, color = CoffeeMuted, fontSize = responsiveTextSize(14f, 11f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (onClick != null) Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = CoffeeBrown)
    }
}

@Composable
private fun ProfileSecurityCard(
    isWorking: Boolean,
    message: String,
    showPasswordFields: Boolean,
    onTogglePasswordFields: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    onLogout: () -> Unit,
    onRequestDeletion: () -> Unit,
    copy: ProfileCopy
) {
    val newPassword = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    Column {
        ProfileMenuCard {
            ProfileMenuRow(
                icon = Icons.Default.Password,
                title = copy.changePassword,
                subtitle = if (showPasswordFields) copy.passwordInfo else null,
                onClick = onTogglePasswordFields
            )
            if (showPasswordFields) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    OutlinedTextField(value = newPassword.value, onValueChange = { newPassword.value = it }, modifier = Modifier.fillMaxWidth(), label = { Text(copy.newPassword) }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = confirmPassword.value, onValueChange = { confirmPassword.value = it }, modifier = Modifier.fillMaxWidth(), label = { Text(copy.confirmPassword) }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { onChangePassword(newPassword.value, confirmPassword.value) }, enabled = !isWorking, modifier = Modifier.fillMaxWidth()) { Text(if (isWorking) copy.saving else copy.savePassword) }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onLogout, enabled = !isWorking, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Text(copy.logout, modifier = Modifier.padding(start = 8.dp))
        }
        TextButton(onClick = onRequestDeletion, modifier = Modifier.fillMaxWidth()) { Text(copy.deleteAccount) }
        if (message.isNotBlank()) Text(message, color = CoffeeBrown, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
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

private const val PRIVACY_POLICY_URL = "https://maurofmorato.github.io/cafecomnota-privacidade/"
private const val ACCOUNT_DELETION_URL = "https://maurofmorato.github.io/cafecomnota-privacidade/#exclusao-de-conta"

private data class ProfileCopy(
    val connectedAccount: String,
    val connected: String,
    val moderatorTools: String,
    val moderatorInfo: String,
    val openReviewQueue: String,
    val activity: String,
    val myCoffees: String,
    val followSubmissions: String,
    val myReviews: String,
    val reviewsSoon: String,
    val preferences: String,
    val privacy: String,
    val accountSecurity: String,
    val changePassword: String,
    val passwordInfo: String,
    val newPassword: String,
    val confirmPassword: String,
    val savePassword: String,
    val saving: String,
    val logout: String,
    val deleteAccount: String
)

private fun profileCopyFor(language: AppLanguage): ProfileCopy = when (language) {
    AppLanguage.Portuguese -> ProfileCopy(
        "Conta conectada", "Conectado", "Ferramentas de moderador",
        "Revise cafés e mantenha a base confiável", "Abrir fila de revisão",
        "Minha atividade", "Meus cafés cadastrados", "Acompanhe seus envios",
        "Minhas avaliações", "Em breve: histórico das suas notas", "Preferências",
        "Privacidade e dados", "Conta e segurança", "Alterar senha",
        "Informe e confirme a nova senha", "Nova senha", "Confirmar nova senha",
        "Salvar nova senha", "Salvando…", "Sair da conta", "Solicitar exclusão da conta"
    )
    AppLanguage.English -> ProfileCopy(
        "Connected account", "Connected", "Moderator tools",
        "Review coffees and keep the database reliable", "Open review queue",
        "My activity", "My submitted coffees", "Track your submissions",
        "My ratings", "Coming soon: your rating history", "Preferences",
        "Privacy and data", "Account and security", "Change password",
        "Enter and confirm your new password", "New password", "Confirm new password",
        "Save new password", "Saving…", "Sign out", "Request account deletion"
    )
    AppLanguage.Spanish -> ProfileCopy(
        "Cuenta conectada", "Conectado", "Herramientas de moderación",
        "Revisa cafés y mantén la base confiable", "Abrir cola de revisión",
        "Mi actividad", "Mis cafés registrados", "Sigue tus envíos",
        "Mis valoraciones", "Próximamente: historial de valoraciones", "Preferencias",
        "Privacidad y datos", "Cuenta y seguridad", "Cambiar contraseña",
        "Ingresa y confirma la nueva contraseña", "Nueva contraseña", "Confirmar contraseña",
        "Guardar contraseña", "Guardando…", "Cerrar sesión", "Solicitar eliminación de cuenta"
    )
    AppLanguage.French -> ProfileCopy(
        "Compte connecté", "Connecté", "Outils de modération",
        "Vérifiez les cafés et gardez une base fiable", "Ouvrir la file de vérification",
        "Mon activité", "Mes cafés enregistrés", "Suivez vos envois",
        "Mes notes", "Bientôt : historique de vos notes", "Préférences",
        "Confidentialité et données", "Compte et sécurité", "Modifier le mot de passe",
        "Saisissez et confirmez le nouveau mot de passe", "Nouveau mot de passe", "Confirmer le mot de passe",
        "Enregistrer", "Enregistrement…", "Se déconnecter", "Demander la suppression du compte"
    )
    AppLanguage.German -> ProfileCopy(
        "Verbundenes Konto", "Verbunden", "Moderationswerkzeuge",
        "Kaffees prüfen und die Datenbank zuverlässig halten", "Prüfliste öffnen",
        "Meine Aktivität", "Meine eingetragenen Kaffees", "Einreichungen verfolgen",
        "Meine Bewertungen", "Demnächst: Bewertungsverlauf", "Einstellungen",
        "Datenschutz und Daten", "Konto und Sicherheit", "Passwort ändern",
        "Neues Passwort eingeben und bestätigen", "Neues Passwort", "Passwort bestätigen",
        "Passwort speichern", "Speichern…", "Abmelden", "Kontolöschung beantragen"
    )
    AppLanguage.Chinese -> ProfileCopy(
        "已连接的账号", "已连接", "审核工具", "审核咖啡并维护可靠数据库", "打开审核队列",
        "我的活动", "我提交的咖啡", "查看提交进度", "我的评分", "即将推出：评分记录",
        "偏好设置", "隐私与数据", "账号与安全", "修改密码", "输入并确认新密码",
        "新密码", "确认新密码", "保存新密码", "保存中…", "退出账号", "申请删除账号"
    )
    AppLanguage.Japanese -> ProfileCopy(
        "接続済みアカウント", "接続済み", "モデレーター用ツール",
        "コーヒーを確認し、信頼できるデータベースを保ちます", "審査キューを開く",
        "マイアクティビティ", "登録したコーヒー", "投稿状況を確認",
        "自分の評価", "近日公開：評価履歴", "設定", "プライバシーとデータ",
        "アカウントとセキュリティ", "パスワードを変更", "新しいパスワードを入力して確認",
        "新しいパスワード", "パスワードの確認", "パスワードを保存",
        "保存中…", "ログアウト", "アカウント削除を申請"
    )
}

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
    val showPasswordFields = remember { mutableStateOf(false) }

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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CoffeeBrown
                )

                androidx.compose.foundation.layout.Column {
                    Text(
                        text = "Conta conectada",
                        color = CoffeeBrownDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = authSession.email,
                        color = CoffeeMuted,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sua conta mantém seus cafés, avaliações e contribuições sincronizados.",
                color = CoffeeMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showPasswordFields.value = !showPasswordFields.value },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Password, contentDescription = null)
                Text(
                    text = if (showPasswordFields.value) "Fechar alteração de senha" else "Alterar senha",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (showPasswordFields.value) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newPassword.value,
                    onValueChange = { newPassword.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nova senha") },
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
                    onValueChange = { confirmPassword.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirmar nova senha") },
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
                    onClick = { onChangePassword(newPassword.value, confirmPassword.value) },
                    enabled = !isWorking,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (isWorking) "Salvando..." else "Salvar nova senha") }
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
private fun AdminCard(onOpenAdministration: () -> Unit) {
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
                    text = "Ferramentas de moderador",
                    color = CoffeeBrownDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Revise cafés, preserve o histórico e mantenha a base confiável.",
                color = CoffeeMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onOpenAdministration, modifier = Modifier.fillMaxWidth()) {
                Text("Abrir fila de revisão")
            }
        }
    }
}

@Composable
private fun ContributionsCard(onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(Modifier.padding(14.dp)) {
            Text("Minhas contribuições", color = CoffeeBrownDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Veja se cada café está pendente, publicado ou precisa de uma nova tentativa de envio das fotos.",
                color = CoffeeMuted, fontSize = 13.sp, lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) { Text("Ver contribuições") }
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
