package com.homeops.grocery.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.homeops.grocery.ui.screens.CaptureScreen
import com.homeops.grocery.ui.screens.HomeScreen
import com.homeops.grocery.ui.screens.ReceiptScreen
import com.homeops.grocery.ui.screens.ShoppingListScreen

object Routes {
    const val HOME = "home"
    const val CAPTURE = "capture"
    const val SHOPPING_LIST = "shopping_list"
    const val RECEIPTS = "receipts"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToCapture = { navController.navigate(Routes.CAPTURE) },
                onNavigateToShoppingList = { navController.navigate(Routes.SHOPPING_LIST) },
                onNavigateToReceipts = { navController.navigate(Routes.RECEIPTS) },
            )
        }

        composable(Routes.CAPTURE) {
            CaptureScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SHOPPING_LIST) {
            ShoppingListScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.RECEIPTS) {
            ReceiptScreen(onBack = { navController.popBackStack() })
        }
    }
}
