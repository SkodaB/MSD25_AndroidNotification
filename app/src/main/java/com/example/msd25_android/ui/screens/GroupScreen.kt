package com.example.msd25_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.msd25_android.logic.NotificationHelper
import androidx.compose.ui.platform.LocalContext


data class Expense(val name: String, val amount: Double, val note: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    groupName: String,
    members: List<String>,
    currentUser: String,
    expenses: SnapshotStateList<Expense>,
    onOpenDetails: (members: List<String>, expenses: List<Expense>, currentUser: String) -> Unit,
    onBack: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(groupName) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Expense",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Box(Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = { onOpenDetails(members, expenses, currentUser) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("DETAILS") }
                }
            }
        }
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(expenses) { e ->
                    ExpenseBubble(expense = e)
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            members = members,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, note ->
                expenses.add(Expense(name = name, amount = amount, note = note.trim()))
                showAddDialog = false



                NotificationHelper.showExpenseNotification(
                    context,
                    "Expense Added",
                    "You added ${name} for ${amount} kr"
                )
            }
        )
    }
}

@Composable
private fun ExpenseBubble(expense: Expense) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = "${expense.name} spent ${"%.2f".format(expense.amount)} kr" +
                            if (expense.note.isNotBlank()) " on ${expense.note}" else "",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    members: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, note: String) -> Unit
) {
    var selectedName by remember { mutableStateOf(members.first()) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val amountIsValid = amountText.toDoubleOrNull()?.let { it >= 0.0 } == true
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Who paid?") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Open menu"
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        members.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedName = name
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                            .filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }
                            .replace(',', '.')
                    },
                    label = { Text("Amount (kr)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (what for?)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = amountIsValid,
                onClick = { onConfirm(selectedName, amountText.toDouble(), note) }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
