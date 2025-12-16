package com.shg25.limimeshi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shg25.limimeshi.feature.chainlist.ChainListScreen
import com.shg25.limimeshi.ui.login.LoginScreen

/**
 * Limimeshiアプリのナビゲーションホスト
 */
@Composable
fun LimimeshiNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Route.Login.route) {
            LoginScreen(
                onNavigateToChainList = {
                    navController.navigate(Route.ChainList.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.ChainList.route) {
            ChainListScreen()
        }
    }
}

/**
 * ナビゲーションルート定義
 */
sealed class Route(val route: String) {
    data object Login : Route("login")
    data object ChainList : Route("chain_list")
}
