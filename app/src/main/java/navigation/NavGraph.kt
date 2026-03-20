package com.example.foodbanklocator.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodbanklocator.screens.LandingScreen
import com.example.foodbanklocator.screens.LoginScreen
import com.example.foodbanklocator.screens.MapScreen
import com.example.foodbanklocator.screens.DetailScreen

sealed class Screen(val route: String) {
    object Landing  : Screen("landing")
    object Login    : Screen("login")
    object Map      : Screen("map")
    object Detail   : Screen("detail/{foodBankId}") {
        fun createRoute(foodBankId: String) = "detail/$foodBankId"
    }
}

@Composable
fun KitchenFreshNavGraph(
    navController: NavHostController = rememberNavController(),
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Landing.route
    ) {

        /**Regular user screen */

        composable(Screen.Landing.route) {
            LandingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onAdminLogin = {
                    navController.navigate(Screen.Login.route)
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        /** Admin only screen */

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.navigateUp()
                }
            )
        }

        /** Map screen */

        composable(Screen.Map.route) {
            MapScreen(
                onFoodBankSelected = { foodBankId ->
                    navController.navigate(Screen.Detail.createRoute(foodBankId))
                },
                onAdminLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        /** Detail Screen */

        composable(Screen.Detail.route) { backStackEntry ->
            val foodBankId = backStackEntry.arguments?.getString("foodBankId") ?: ""
            DetailScreen(
                foodBankId = foodBankId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
