package com.weeklylotto.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.weeklylotto.app.feature.generator.NumberGeneratorScreen
import com.weeklylotto.app.feature.home.HomeScreen
import com.weeklylotto.app.feature.importticket.ImportScreen
import com.weeklylotto.app.feature.manage.ManageScreen
import com.weeklylotto.app.feature.manage.TicketDetailScreen
import com.weeklylotto.app.feature.manualadd.ManualAddScreen
import com.weeklylotto.app.feature.qr.QrScanScreen
import com.weeklylotto.app.feature.result.ResultScreen
import com.weeklylotto.app.feature.settings.SettingsScreen
import com.weeklylotto.app.feature.stats.StatsScreen
import com.weeklylotto.app.ui.component.LottoBottomBar
import com.weeklylotto.app.ui.component.LottoBottomBarItem

@Composable
fun WeeklyLottoApp(initialRoute: String? = null) {
    val navController = rememberNavController()
    val destinations = AppDestination.bottomTabs.filterNotNull()

    LaunchedEffect(initialRoute) {
        val route = initialRoute ?: return@LaunchedEffect
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute =
                destinations.firstOrNull { destination ->
                    currentDestination
                        ?.hierarchy
                        ?.any { navDestination -> navDestination.route == destination.route } == true
                }?.route

            if (currentRoute != null) {
                LottoBottomBar(
                    items = destinations.map { LottoBottomBarItem(it.route, it.label, it.icon) },
                    currentRoute = currentRoute,
                    onSelect = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppDestination.Home.route) {
                HomeScreen(
                    onClickGenerator = { navController.navigate(AppDestination.Generator.route) },
                    onClickManage = { navController.navigate(AppDestination.Manage.route) },
                    onClickResult = { navController.navigate(AppDestination.Result.route) },
                    onClickSettings = { navController.navigate(AppDestination.Settings.route) },
                    onClickQr = { navController.navigate(AppDestination.QrScan.route) },
                )
            }
            composable(AppDestination.Manage.route) {
                ManageScreen(
                    onOpenQr = { navController.navigate(AppDestination.QrScan.route) },
                    onOpenManualAdd = { navController.navigate(AppDestination.ManualAdd.route) },
                    onOpenImport = { navController.navigate(AppDestination.Import.route) },
                    onOpenTicketDetail = { ticketId ->
                        navController.navigate(AppDestination.ManageTicketDetail.detailRoute(ticketId))
                    },
                )
            }
            composable(AppDestination.Generator.route) {
                NumberGeneratorScreen()
            }
            composable(AppDestination.ManualAdd.route) {
                ManualAddScreen(onBack = { navController.popBackStack() })
            }
            composable(AppDestination.Import.route) {
                ImportScreen(onBack = { navController.popBackStack() })
            }
            composable(AppDestination.QrScan.route) {
                QrScanScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = AppDestination.ManageTicketDetail.route,
                arguments = listOf(navArgument("ticketId") { type = NavType.LongType }),
            ) { entry ->
                val ticketId = entry.arguments?.getLong("ticketId") ?: return@composable
                TicketDetailScreen(
                    ticketId = ticketId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.Result.route) {
                ResultScreen()
            }
            composable(AppDestination.Stats.route) {
                StatsScreen()
            }
            composable(AppDestination.Settings.route) {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
