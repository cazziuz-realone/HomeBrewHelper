package com.example.homebrewhelper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.homebrewhelper.ui.screens.recipe.RecipeListScreen
import com.example.homebrewhelper.ui.screens.recipe.RecipeDetailScreen
import com.example.homebrewhelper.ui.screens.recipe.RecipeBuilderScreen

/**
 * Navigation setup for HomeBrewHelper app
 * Defines routes and navigation between screens
 */
@Composable
fun HomeBrewNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HomeBrewDestinations.RECIPE_LIST
    ) {
        // Recipe List Screen
        composable(HomeBrewDestinations.RECIPE_LIST) {
            RecipeListScreen(
                onNavigateToRecipe = { recipeId ->
                    navController.navigate("${HomeBrewDestinations.RECIPE_DETAIL}/$recipeId")
                },
                onNavigateToNewRecipe = {
                    navController.navigate(HomeBrewDestinations.RECIPE_BUILDER)
                },
                onNavigateToEditRecipe = { recipeId ->
                    navController.navigate("${HomeBrewDestinations.RECIPE_BUILDER}/$recipeId")
                }
            )
        }
        
        // Recipe Detail Screen
        composable("${HomeBrewDestinations.RECIPE_DETAIL}/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
            RecipeDetailScreen(
                recipeId = recipeId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate("${HomeBrewDestinations.RECIPE_BUILDER}/$id")
                },
                onNavigateToRecipe = { id ->
                    navController.navigate("${HomeBrewDestinations.RECIPE_DETAIL}/$id")
                }
            )
        }
        
        // Recipe Builder Screen (New Recipe)
        composable(HomeBrewDestinations.RECIPE_BUILDER) {
            RecipeBuilderScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRecipeSaved = { recipeId ->
                    navController.navigate("${HomeBrewDestinations.RECIPE_DETAIL}/$recipeId") {
                        popUpTo(HomeBrewDestinations.RECIPE_LIST)
                    }
                }
            )
        }
        
        // Recipe Builder Screen (Edit Recipe)
        composable("${HomeBrewDestinations.RECIPE_BUILDER}/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
            RecipeBuilderScreen(
                recipeId = recipeId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRecipeSaved = { savedRecipeId ->
                    navController.navigate("${HomeBrewDestinations.RECIPE_DETAIL}/$savedRecipeId") {
                        popUpTo(HomeBrewDestinations.RECIPE_LIST)
                    }
                }
            )
        }
    }
}

/**
 * Navigation destinations for the app
 */
object HomeBrewDestinations {
    const val RECIPE_LIST = "recipe_list"
    const val RECIPE_DETAIL = "recipe_detail"
    const val RECIPE_BUILDER = "recipe_builder"
    const val BATCH_TRACKING = "batch_tracking"
    const val INVENTORY = "inventory"
    const val SETTINGS = "settings"
}