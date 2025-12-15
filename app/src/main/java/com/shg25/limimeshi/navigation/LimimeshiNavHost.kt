package com.shg25.limimeshi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shg25.limimeshi.feature.chainlist.ChainListScreen

/**
 * Limimeshiアプリのナビゲーションホスト
 */
@Composable
fun LimimeshiNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.ChainList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Route.ChainList.route) {
            ChainListScreen()
        }

        // TODO: 他の画面を追加
    }
}

/**
 * ナビゲーションルート定義
 */
sealed class Route(val route: String) {
    data object ChainList : Route("chain_list")
    // TODO: 他のルートを追加
}
