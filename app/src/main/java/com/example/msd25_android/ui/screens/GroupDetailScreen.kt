package com.example.msd25_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.msd25_android.logic.NotificationHelper


private fun calculateBalances(
    members: List<String>,
    expenses: List<Expense>
): Map<String, Double> {
    val balances = members.associateWith { 0.0 }.toMutableMap()
    if (members.isEmpty()) return balances
    expenses.forEach { e ->
        val share = e.amount / members.size
        members.forEach { m -> balances[m] = (balances[m] ?: 0.0) - share }
        balances[e.name] = (balances[e.name] ?: 0.0) + e.amount
    }
    return balances
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupName: String,
    members: List<String>,
    expenses: List<Expense>,
    currentUser: String,
    onPay: () -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val balances = remember(members, expenses) { calculateBalances(members, expenses) }
    val myBalance = balances[currentUser] ?: 0.0
    val myBalanceText = "%.2f kr".format(myBalance)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("$groupName • Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Box(Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = onPay,
                        enabled = myBalance < 0.0,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary,
                            disabledContainerColor = cs.surfaceVariant,
                            disabledContentColor = cs.onSurfaceVariant
                        )
                    ) {
                        Text(if (myBalance < 0.0) "Pay Now" else "All Set")
                    }
                }
            }
        }
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(members) { name ->
                    MemberBalanceRow(name = name, balance = balances[name] ?: 0.0)
                }
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = cs.surfaceVariant),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Your balance", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = myBalanceText,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = when {
                            myBalance > 0 -> cs.primary
                            myBalance < 0 -> cs.error
                            else -> cs.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberBalanceRow(name: String, balance: Double) {
    val cs = MaterialTheme.colorScheme

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cs.surfaceVariant,
            contentColor = cs.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(cs.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = cs.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            }
            val text = "%.2f kr".format(balance)
            val color = when {
                balance > 0 -> cs.primary
                balance < 0 -> cs.error
                else -> cs.onSurfaceVariant
            }
            Text(text, color = color, style = MaterialTheme.typography.titleMedium)
        }
    }
}
