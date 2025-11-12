package com.example.msd25_android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.Preferences
import com.example.msd25_android.logic.SessionManager
import com.example.msd25_android.ui.nav.AuthNav
import com.example.msd25_android.ui.nav.HomeNav
import com.example.msd25_android.ui.screens.*
import com.example.msd25_android.ui.theme.MSD25_AndroidTheme
import com.example.msd25_android.ui.user_repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        enableEdgeToEdge()
        setContent {
            MSD25_AndroidTheme(dynamicColor = false) {
                MSD25_AndroidApp()
            }
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "expense_channel",
                "Expense Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows notifications for created expenses"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

enum class UserAuthState {
    UNKNOWN,
    AUTHENTICATED,
    UNAUTHENTICATED
}

enum class AuthDestinations {
    LOGIN,
    SIGNUP
}

enum class AppDestinations(val label: String, val icon: ImageVector) {
    FRIENDS("Friends", Icons.Default.Person),
    HOME("Home", Icons.Default.Home),
    PROFILE("Profile", Icons.Default.AccountBox),
    ADD_FRIEND("AddFriend", Icons.Default.Home),
    ADD_GROUP("AddGroup", Icons.Default.Home),
    GROUP("Group", Icons.Default.Home),
    GROUP_DETAILS("GroupDetails", Icons.Default.Home),
    PAY("Pay", Icons.Default.Home),
    EDIT_PROFILE("EditProfile", Icons.Default.Home)
}

data class GroupModel(
    val id: String,
    val name: String,
    val members: List<String>,
    val expenses: SnapshotStateList<Expense>
)

fun myBalanceFor(user: String, members: List<String>, expenses: List<Expense>): Double {
    val balances = members.associateWith { 0.0 }.toMutableMap()
    if (members.isNotEmpty()) {
        expenses.forEach { e ->
            val share = e.amount / members.size
            members.forEach { m -> balances[m] = (balances[m] ?: 0.0) - share }
            balances[e.name] = (balances[e.name] ?: 0.0) + e.amount
        }
    }
    return balances[user] ?: 0.0
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "setting"
)

@PreviewScreenSizes
@Preview(showBackground = true)
@Composable
fun MSD25_AndroidApp() {

    val (userAuthState, setUserAuthState) = remember { mutableStateOf(UserAuthState.UNKNOWN) }
    val (appCurrent, setAppCurrent) = remember { mutableStateOf(AppDestinations.HOME) }
    val (authCurrent, setAuthCurrent) = remember { mutableStateOf(AuthDestinations.LOGIN) }
    val bottom = listOf(AppDestinations.FRIENDS, AppDestinations.HOME, AppDestinations.PROFILE)
    val cs = MaterialTheme.colorScheme

    val application = LocalContext.current.applicationContext as Application
    val userRepository = UserRepository(application.dataStore)

    val sessionManager = SessionManager(application, setUserAuthState, userRepository)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) { sessionManager.restoreToken() }
    }

    Scaffold(
        bottomBar = {
            when (userAuthState) {
                UserAuthState.UNKNOWN -> {}
                UserAuthState.AUTHENTICATED -> NavigationBar(containerColor = cs.surface, contentColor = cs.onSurface) {
                    bottom.forEach { d ->
                        NavigationBarItem(
                            selected = d == appCurrent,
                            onClick = { setAppCurrent(d) },
                            icon = { Icon(d.icon, contentDescription = d.label) },
                            label = { Text(d.label) },
                            modifier = Modifier.height(56.dp),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = cs.primary,
                                selectedTextColor = cs.primary,
                                indicatorColor = cs.primary.copy(alpha = 0.15f),
                                unselectedIconColor = cs.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = cs.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
                UserAuthState.UNAUTHENTICATED -> {}
            }

        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (userAuthState) {
                UserAuthState.UNKNOWN -> {}
                UserAuthState.AUTHENTICATED -> HomeNav(appCurrent, setAppCurrent, sessionManager)
                UserAuthState.UNAUTHENTICATED -> AuthNav(authCurrent, setAuthCurrent, sessionManager)
            }
        }
    }
}
