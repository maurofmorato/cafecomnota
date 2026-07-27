package com.maurofmorato.cafecomnota.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maurofmorato.cafecomnota.data.admin.ModerationCoffee
import com.maurofmorato.cafecomnota.data.admin.SupabaseAdminRepository
import com.maurofmorato.cafecomnota.data.auth.AuthSession
import com.maurofmorato.cafecomnota.ui.components.CafeResponsiveContent
import com.maurofmorato.cafecomnota.ui.components.CoffeePackagePlaceholder
import com.maurofmorato.cafecomnota.ui.components.SubScreenHero
import com.maurofmorato.cafecomnota.ui.i18n.AppStrings
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted
import kotlinx.coroutines.launch
import java.text.Normalizer

@Composable
fun CoffeeModerationScreen(
    innerPadding: PaddingValues,
    strings: AppStrings,
    authSession: AuthSession?,
    isAdmin: Boolean,
    mineOnly: Boolean,
    refreshKey: Int = 0,
    hiddenCoffeeIds: Set<String> = emptySet(),
    onOpenCoffee: (ModerationCoffee) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { SupabaseAdminRepository() }
    val scope = rememberCoroutineScope()
    var coffees by remember { mutableStateOf<List<ModerationCoffee>>(emptyList()) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var actionCoffeeId by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf(if (mineOnly) "todos" else "pendente") }

    suspend fun reload() {
        val session = authSession ?: return
        isLoading = true
        try {
            coffees = repository.loadCoffeesForAdministration(
                accessToken = session.accessToken,
                mineOnlyUserId = if (mineOnly) session.userId else null
            )
        } catch (_: Throwable) {
            message = "Não foi possível atualizar a lista agora. Verifique a conexão e tente novamente."
        } finally {
            isLoading = false
        }
    }

    fun approveQuickly(coffee: ModerationCoffee) {
        val session = authSession ?: return
        actionCoffeeId = coffee.id
        scope.launch {
            try {
                repository.updateCoffeeStatus(
                    coffeeId = coffee.id,
                    accessToken = session.accessToken,
                    newStatus = "ativo",
                    reason = ""
                )
                coffees = coffees.map {
                    if (it.id == coffee.id) it.copy(status = "ativo", moderationReason = "") else it
                }
                message = "Café aprovado e publicado."
            } catch (_: Throwable) {
                message = "Não foi possível aprovar este café. Tente novamente em instantes."
            } finally {
                actionCoffeeId = null
            }
        }
    }

    LaunchedEffect(authSession?.userId, mineOnly, refreshKey) { reload() }

    val visibleCoffees = coffees.filter { it.id !in hiddenCoffeeIds }
    val duplicateKeys = visibleCoffees
        .filter { it.status != "removido" }
        .groupingBy { moderationKey(it) }
        .eachCount()
    val filteredCoffees = visibleCoffees.filter { coffee ->
        when (selectedStatus) {
            "todos" -> coffee.status != "removido"
            else -> coffee.status == selectedStatus
        }
    }

    CafeResponsiveContent(innerPadding) {
        SubScreenHero(
            strings = strings,
            title = if (mineOnly) "Minhas contribuições" else "Fila de revisão",
            subtitle = if (mineOnly) {
                "Acompanhe seus envios, fotos e decisões da moderação."
            } else {
                "Cafés aguardando sua decisão. Toque em Revisar para analisar com calma."
            },
            onBack = onBack
        )

        Spacer(Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            moderationStatuses.forEach { status ->
                val count = when (status) {
                    "todos" -> visibleCoffees.count { it.status != "removido" }
                    else -> visibleCoffees.count { it.status == status }
                }
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { selectedStatus = status },
                    label = { Text("${statusLabel(status)} · $count", maxLines = 1) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        if (isLoading) {
            Text("Atualizando a fila…", color = CoffeeMuted)
        } else {
            Text(
                text = if (mineOnly) "${filteredCoffees.size} café(s) neste filtro" else "${filteredCoffees.size} café(s) para revisar",
                color = CoffeeMuted,
                fontSize = 13.sp
            )
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            ModerationNotice(message = message, onDismiss = { message = "" })
        }

        Spacer(Modifier.height(10.dp))
        if (!isLoading && filteredCoffees.isEmpty()) {
            Text(
                text = if (mineOnly) "Você ainda não tem contribuições neste filtro." else "Nenhum café nesta etapa da fila.",
                color = CoffeeMuted
            )
        }

        filteredCoffees.forEach { coffee ->
            ModerationCoffeeListItem(
                coffee = coffee,
                packageLabel = strings.coffeeWord,
                showAdminActions = isAdmin && !mineOnly,
                isLikelyDuplicate = duplicateKeys[moderationKey(coffee)].orZero() > 1,
                isWorking = actionCoffeeId == coffee.id,
                onApprove = { approveQuickly(coffee) },
                onClick = { onOpenCoffee(coffee) }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

private val moderationStatuses = listOf("pendente", "todos", "ativo", "oculto", "rejeitado", "removido")

internal fun statusLabel(status: String): String = when (status) {
    "todos" -> "Todos"
    "pendente" -> "Pendente"
    "ativo" -> "Ativo"
    "oculto" -> "Oculto"
    "rejeitado" -> "Rejeitado"
    "removido" -> "Removido"
    "nao_solicitada" -> "Não solicitada"
    "enviando" -> "Enviando"
    "concluida" -> "Concluída"
    "falhou" -> "Falhou"
    else -> status.replaceFirstChar { it.uppercase() }
}

@Composable
private fun ModerationNotice(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(message, color = CoffeeBrown, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("Fechar", color = CoffeeBrown, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onDismiss))
        }
    }
}

@Composable
private fun ModerationCoffeeListItem(
    coffee: ModerationCoffee,
    packageLabel: String,
    showAdminActions: Boolean,
    isLikelyDuplicate: Boolean,
    isWorking: Boolean,
    onApprove: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CoffeeCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoffeePackagePlaceholder(
                    label = packageLabel,
                    modifier = Modifier.size(width = 72.dp, height = 96.dp)
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = coffee.name,
                        color = CoffeeBrownDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = coffee.brand,
                        color = CoffeeMuted,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    FilterChip(
                        selected = coffee.status == "pendente",
                        onClick = onClick,
                        label = { Text(statusLabel(coffee.status)) }
                    )
                }
                if (coffee.expectedPhotos > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = CoffeeMuted, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.size(3.dp))
                        Text("${coffee.uploadedPhotos}/${coffee.expectedPhotos}", color = CoffeeMuted, fontSize = 12.sp)
                    }
                }
            }

            if (isLikelyDuplicate && coffee.status != "removido") {
                Spacer(Modifier.height(8.dp))
                Text("Possível duplicata: confira nome e marca antes de publicar.", color = CoffeeBrown, fontSize = 13.sp)
            }
            if (coffee.moderationReason.isNotBlank()) {
                Spacer(Modifier.height(7.dp))
                Text(coffee.moderationReason, color = CoffeeMuted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.height(11.dp))
            if (showAdminActions && coffee.status == "pendente") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onApprove, enabled = !isWorking, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Text(if (isWorking) "Aprovando…" else "Aprovar", modifier = Modifier.padding(start = 6.dp))
                    }
                    OutlinedButton(onClick = onClick, enabled = !isWorking, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Text("Revisar", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            } else {
                OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text(if (showAdminActions) "Abrir revisão" else "Ver detalhes")
                }
            }
        }
    }
}

private fun moderationKey(coffee: ModerationCoffee): String = Normalizer
    .normalize("${coffee.name} ${coffee.brand}", Normalizer.Form.NFD)
    .replace("\\p{M}+".toRegex(), "")
    .lowercase()
    .replace("[^a-z0-9]+".toRegex(), "")

private fun Int?.orZero(): Int = this ?: 0
