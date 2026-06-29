package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maurofmorato.cafecomnota.analytics.AnalyticsEvents
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics
import com.maurofmorato.cafecomnota.ui.components.CafeHeader
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SectionTitle
import com.maurofmorato.cafecomnota.ui.navigation.AppDestination
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCream
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    onNavigate: (AppDestination) -> Unit
) {
    CafeResponsiveContent(
        innerPadding = innerPadding
    ) {
        CafeHeader(compact = true)

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Perfil")

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = CoffeeCard
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Visitante",
                    color = CoffeeBrown,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Em breve: login para avaliar cafés, cadastrar novos produtos e ver seu histórico.",
                    color = CoffeeMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = CoffeeCream,
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            ListItem(
                headlineContent = {
                    Text("Entrar com Google")
                },
                supportingContent = {
                    Text("Será ligado ao Supabase Auth depois")
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        tint = CoffeeBrown
                    )
                }
            )

            ListItem(
                headlineContent = {
                    Text("Minhas avaliações")
                },
                supportingContent = {
                    Text("Histórico dos cafés que você avaliou")
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CoffeeBrown
                    )
                }
            )

            ListItem(
                headlineContent = {
                    Text("Cafés que compraria novamente")
                },
                supportingContent = {
                    Text("Sua lista pessoal de bons cafés")
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = CoffeeBrown
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                CafeAnalytics.logEvent(
                    eventName = AnalyticsEvents.CRASHLYTICS_NON_FATAL_TEST,
                    params = mapOf(
                        "source" to "profile",
                        "type" to "non_fatal"
                    )
                )

                CafeAnalytics.recordNonFatal(
                    throwable = IllegalStateException("Teste nao fatal do Crashlytics - Cafe com nota"),
                    params = mapOf(
                        "screen" to "profile",
                        "action" to "test_non_fatal"
                    )
                )
            }
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null
            )

            Text(
                text = "Registrar teste não fatal",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
