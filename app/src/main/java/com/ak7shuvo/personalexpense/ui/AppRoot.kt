package com.ak7shuvo.personalexpense.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ak7shuvo.personalexpense.domain.TxType
import com.ak7shuvo.personalexpense.ui.screens.ActivityScreen
import com.ak7shuvo.personalexpense.ui.screens.EntryScreen
import com.ak7shuvo.personalexpense.ui.screens.LoansScreen
import com.ak7shuvo.personalexpense.ui.screens.OverviewScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("overview", "Overview", Icons.Filled.Home),
    Tab("activity", "History", Icons.AutoMirrored.Filled.List),
    Tab("loans", "Loans", Icons.Filled.Person),
)

private const val ENTRY = "entry/{type}/{id}/{loanId}"
private fun entryRoute(type: TxType, id: Long = 0, loanId: Long = 0) = "entry/${type.name}/$id/$loanId"

@Composable
fun AppRoot(viewModel: LedgerViewModel) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route

    fun switchTab(target: String) {
        nav.navigate(target) {
            popUpTo("overview") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(visible = tabs.any { it.route == route }, enter = fadeIn(), exit = fadeOut()) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = { if (route != tab.route) switchTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = "overview",
            modifier = Modifier.padding(bottom = inner.calculateBottomPadding()),
            enterTransition = { fadeIn(tween(220)) },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(120)) },
        ) {
            composable("overview") {
                OverviewScreen(
                    ui = ui,
                    onAdd = { nav.navigate(entryRoute(it)) },
                    onOpen = { nav.navigate(entryRoute(it.type, it.id)) },
                    onSeeActivity = { switchTab("activity") },
                    onSeeLoans = { switchTab("loans") },
                )
            }
            composable("activity") {
                ActivityScreen(ui, onOpen = { nav.navigate(entryRoute(it.type, it.id)) })
            }
            composable("loans") {
                LoansScreen(
                    ui = ui,
                    onLend = { nav.navigate(entryRoute(TxType.LOAN)) },
                    onEditLoan = { nav.navigate(entryRoute(TxType.LOAN, it.id)) },
                    onRepay = { nav.navigate(entryRoute(TxType.REPAYMENT, loanId = it.loan.id)) },
                    onEditRepayment = { nav.navigate(entryRoute(TxType.REPAYMENT, it.id)) },
                )
            }
            composable(
                route = ENTRY,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("id") { type = NavType.LongType },
                    navArgument("loanId") { type = NavType.LongType },
                ),
                enterTransition = { slideInVertically(tween(320)) { it / 8 } + fadeIn(tween(260)) },
                exitTransition = { fadeOut(tween(120)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { slideOutVertically(tween(260)) { it / 8 } + fadeOut(tween(200)) },
            ) { entry ->
                val args = entry.arguments
                EntryScreen(
                    ui = ui,
                    initialType = runCatching { TxType.valueOf(args?.getString("type").orEmpty()) }.getOrDefault(TxType.EXPENSE),
                    editId = args?.getLong("id") ?: 0L,
                    presetLoanId = args?.getLong("loanId") ?: 0L,
                    onSave = { draft, done -> viewModel.save(draft, done) },
                    onDelete = viewModel::delete,
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}
