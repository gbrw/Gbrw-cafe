package com.example

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.admin.AdminEditProductScreen
import com.example.ui.admin.AdminLoginScreen

@Composable
fun AppNavigation(viewModel: ProductViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            CoffeeMenuScreen(viewModel = viewModel, navController = navController)
        }
        composable("admin_login") {
            AdminLoginScreen(navController = navController)
        }
        composable("admin_dashboard") {
            AdminDashboardScreen(navController = navController, viewModel = viewModel)
        }
        composable("admin_edit_product/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            AdminEditProductScreen(navController = navController, viewModel = viewModel, productId = productId)
        }
    }
}
