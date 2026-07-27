package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.data.admin.ModerationCoffee
import com.maurofmorato.cafecomnota.data.admin.SupabaseAdminRepository
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.SubScreenHero
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import kotlinx.coroutines.launch

@Composable
fun CoffeeModerationDetailScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    authSession: AuthSession?,
    coffee: ModerationCoffee,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onChanged: (coffeeId: String, newStatus: String) -> Unit
) {
    val repository = remember { SupabaseAdminRepository() }
    val scope = rememberCoroutineScope()
    var name by rememberSaveable(coffee.id) { mutableStateOf(coffee.name) }
    var brand by rememberSaveable(coffee.id) { mutableStateOf(coffee.brand) }
    var status by rememberSaveable(coffee.id) { mutableStateOf(coffee.status) }
    var reason by rememberSaveable(coffee.id) { mutableStateOf(coffee.moderationReason) }
    var message by rememberSaveable { mutableStateOf("") }
    var working by rememberSaveable { mutableStateOf(false) }
    var showRemovalConfirmation by rememberSaveable { mutableStateOf(false) }

    fun saveChanges(forceRemoval: Boolean = false) {
        val session = authSession ?: return
        val desiredStatus = if (forceRemoval) "removido" else status
        val desiredReason = if (forceRemoval && reason.isBlank()) {
            "Removido pelo administrador."
        } else {
            reason.trim()
        }

        working = true
        scope.launch {
            try {
                if (!forceRemoval && (name.trim() != coffee.name || brand.trim() != coffee.brand)) {
                    repository.updateCoffeeBasics(
                        coffeeId = coffee.id,
                        accessToken = session.accessToken,
                        name = name,
                        brand = brand
                    )
                }
                if (desiredStatus != coffee.status || desiredReason != coffee.moderationReason) {
                    repository.updateCoffeeStatus(
                        coffeeId = coffee.id,
                        accessToken = session.accessToken,
                        newStatus = desiredStatus,
                        reason = desiredReason
                    )
                }
                onChanged(coffee.id, desiredStatus)
                onBack()
            } catch (_: Throwable) {
                message = "Não foi possível salvar esta decisão agora. Confira a conexão e tente novamente."
            } finally {
                working = false
            }
        }
    }

    CafeResponsiveContent(innerPadding) {
        SubScreenHero(
            strings = strings,
            title = if (isAdmin) "Revisar café" else "Detalhes da contribuição",
            subtitle = if (isAdmin) {
                "Confirme os dados, escolha a decisão e registre um motivo quando for necessário."
            } else {
                "Veja os dados enviados e o andamento da moderação."
            },
            onBack = onBack
        )

        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CoffeeCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = coffee.name,
                    color = CoffeeBrownDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${coffee.brand} • ${statusLabel(coffee.status)}", color = CoffeeMuted)
                if (coffee.expectedPhotos > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Fotos enviadas: ${coffee.uploadedPhotos}/${coffee.expectedPhotos} • ${statusLabel(coffee.photosStatus)}",
                        color = CoffeeBrown,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (coffee.moderationReason.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Motivo atual: ${coffee.moderationReason}", color = CoffeeMuted)
                }
            }
        }

        if (isAdmin) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CoffeeCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Dados e decisão", color = CoffeeBrownDark, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nome") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(9.dp))
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Marca / torrefação") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Status", color = CoffeeBrownDark, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        listOf("pendente", "ativo", "oculto", "rejeitado").forEach { option ->
                            FilterChip(
                                selected = status == option,
                                onClick = { status = option },
                                label = { Text(statusLabel(option)) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Atalhos de motivo", color = CoffeeBrownDark, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        moderationReasonPresets.forEach { preset ->
                            FilterChip(
                                selected = reason == preset,
                                onClick = { reason = preset },
                                label = { Text(preset) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        label = { Text("Motivo da decisão") },
                        supportingText = { Text("Recomendado ao ocultar, rejeitar ou remover.") }
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { saveChanges() },
                        enabled = !working && name.isNotBlank() && brand.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (working) "Salvando…" else "Salvar alteração")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showRemovalConfirmation = true },
                        enabled = !working && status != "removido",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remover do aplicativo")
                    }
                }
            }
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CoffeeCard)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Não foi possível concluir", color = CoffeeBrownDark, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(message, color = CoffeeMuted, fontSize = 13.sp)
                    TextButton(onClick = { message = "" }) { Text("Fechar aviso") }
                }
            }
        }
    }

    if (showRemovalConfirmation) {
        AlertDialog(
            onDismissRequest = { showRemovalConfirmation = false },
            title = { Text("Remover este café?") },
            text = {
                Text(
                    "Ele deixará a busca, o ranking e as telas públicas. O registro da decisão continuará disponível na auditoria administrativa."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRemovalConfirmation = false
                        saveChanges(forceRemoval = true)
                    }
                ) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { showRemovalConfirmation = false }) { Text("Cancelar") }
            }
        )
    }
}

private val moderationReasonPresets = listOf(
    "Possível duplicata",
    "Fotos insuficientes",
    "Dados incompletos",
    "Produto não encontrado"
)
