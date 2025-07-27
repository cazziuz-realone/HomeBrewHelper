package com.example.homebrewhelper.data.repository

import com.example.homebrewhelper.data.database.dao.RecipeDao
import com.example.homebrewhelper.data.database.dao.RecipeIngredientDao
import com.example.homebrewhelper.data.database.entity.Recipe
import com.example.homebrewhelper.data.database.entity.RecipeIngredient
import com.example.homebrewhelper.data.model.BeverageType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for recipe-related operations
 * Provides a clean API for recipe management with business logic
 */
@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao
) {
    
    // Recipe CRUD operations
    suspend fun createRecipe(recipe: Recipe): Result<String> {
        return try {
            recipeDao.insertRecipe(recipe)
            Result.success(recipe.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRecipe(id: String): Recipe? {
        return recipeDao.getRecipeById(id)
    }
    
    fun observeRecipe(id: String): Flow<Recipe?> {
        return recipeDao.observeRecipeById(id)
    }
    
    fun observeAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.observeAllRecipes()
    }
    
    suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        return try {
            val updatedRecipe = recipe.copy(
                updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            )
            recipeDao.updateRecipe(updatedRecipe)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteRecipe(id: String): Result<Unit> {
        return try {
            recipeDao.softDeleteRecipe(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Recipe filtering and search
    fun getRecipesByType(beverageType: BeverageType): Flow<List<Recipe>> {
        return recipeDao.observeRecipesByType(beverageType)
    }
    
    fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes(query)
    }
    
    fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return recipeDao.observeFavoriteRecipes()
    }
    
    fun getRecentRecipes(limit: Int = 10): Flow<List<Recipe>> {
        return recipeDao.getRecentRecipes(limit)
    }
    
    suspend fun toggleFavorite(recipeId: String): Result<Unit> {
        return try {
            val recipe = recipeDao.getRecipeById(recipeId)
            if (recipe != null) {
                recipeDao.updateFavoriteStatus(recipeId, !recipe.isFavorite)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Recipe not found: $recipeId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Recipe scaling
    suspend fun scaleRecipe(recipeId: String, newBatchSize: Double): Result<String> {
        return try {
            val originalRecipe = recipeDao.getRecipeById(recipeId)
                ?: return Result.failure(IllegalArgumentException("Recipe not found: $recipeId"))
            
            val scaleFactor = newBatchSize / originalRecipe.batchSizeGallons
            
            // Create scaled recipe
            val scaledRecipe = originalRecipe.copy(
                id = java.util.UUID.randomUUID().toString(),
                name = "${originalRecipe.name} (${newBatchSize}gal)",
                batchSizeGallons = newBatchSize,
                parentRecipeId = recipeId,
                version = 1,
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            )
            
            // Insert scaled recipe
            recipeDao.insertRecipe(scaledRecipe)
            
            // Scale ingredients
            recipeIngredientDao.duplicateRecipeIngredients(recipeId, scaledRecipe.id)
            recipeIngredientDao.scaleRecipeIngredients(scaledRecipe.id, scaleFactor)
            
            Result.success(scaledRecipe.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Recipe duplication
    suspend fun duplicateRecipe(originalId: String, newName: String): Result<String> {
        return try {
            val newRecipeId = recipeDao.duplicateRecipe(originalId, newName)
            recipeIngredientDao.duplicateRecipeIngredients(originalId, newRecipeId)
            Result.success(newRecipeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Recipe variations
    fun getRecipeVariations(parentId: String): Flow<List<Recipe>> {
        return recipeDao.getRecipeVariations(parentId)
    }
    
    suspend fun createRecipeVariation(parentId: String, changes: String): Result<String> {
        return try {
            val parentRecipe = recipeDao.getRecipeById(parentId)
                ?: return Result.failure(IllegalArgumentException("Parent recipe not found: $parentId"))
            
            val variations = recipeDao.getRecipeVariations(parentId).toString() // This needs to be converted properly
            val nextVersion = variations.split(",").size + 1
            
            val variation = parentRecipe.copy(
                id = java.util.UUID.randomUUID().toString(),
                name = "${parentRecipe.name} v$nextVersion",
                version = nextVersion,
                notes = "${parentRecipe.notes}\n\nChanges in v$nextVersion: $changes",
                createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            )
            
            recipeDao.insertRecipe(variation)
            recipeIngredientDao.duplicateRecipeIngredients(parentId, variation.id)
            
            Result.success(variation.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Recipe statistics
    suspend fun getRecipeStats(): RecipeStats {
        return RecipeStats(
            totalRecipes = recipeDao.getTotalRecipeCount(),
            favoriteRecipes = recipeDao.getFavoriteRecipeCount(),
            beerRecipes = recipeDao.getRecipeCountByType(BeverageType.BEER),
            wineRecipes = recipeDao.getRecipeCountByType(BeverageType.WINE),
            meadRecipes = recipeDao.getRecipeCountByType(BeverageType.MEAD),
            ciderRecipes = recipeDao.getRecipeCountByType(BeverageType.CIDER),
            kombuchaRecipes = recipeDao.getRecipeCountByType(BeverageType.KOMBUCHA),
            specialtyRecipes = recipeDao.getRecipeCountByType(BeverageType.SPECIALTY)
        )
    }
    
    // Recipe ingredients management
    fun observeRecipeIngredients(recipeId: String): Flow<List<RecipeIngredient>> {
        return recipeIngredientDao.observeRecipeIngredients(recipeId)
    }
    
    suspend fun addIngredientToRecipe(
        recipeId: String,
        ingredientId: String,
        quantity: Double,
        unit: String,
        step: String,
        additionTime: Int = 0
    ): Result<String> {
        return try {
            val recipeIngredient = RecipeIngredient(
                recipeId = recipeId,
                ingredientId = ingredientId,
                quantity = quantity,
                unit = unit,
                step = step,
                additionTime = additionTime
            )
            recipeIngredientDao.insertRecipeIngredient(recipeIngredient)
            Result.success(recipeIngredient.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient): Result<Unit> {
        return try {
            recipeIngredientDao.updateRecipeIngredient(recipeIngredient)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeIngredientFromRecipe(recipeIngredientId: String): Result<Unit> {
        return try {
            recipeIngredientDao.deleteRecipeIngredientById(recipeIngredientId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Recipe cost calculations
    suspend fun calculateRecipeCost(recipeId: String): Double? {
        return recipeIngredientDao.getTotalEstimatedCost(recipeId)
    }
    
    suspend fun updateActualRecipeCost(recipeId: String, actualCost: Double): Result<Unit> {
        return try {
            val ingredients = recipeIngredientDao.getRecipeIngredients(recipeId)
            val totalEstimated = recipeIngredientDao.getTotalEstimatedCost(recipeId) ?: 0.0
            
            if (totalEstimated > 0) {
                val costRatio = actualCost / totalEstimated
                ingredients.forEach { ingredient ->
                    val newActualCost = ingredient.estimatedCost?.times(costRatio)
                    recipeIngredientDao.updateActualCost(ingredient.id, newActualCost)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    data class RecipeStats(
        val totalRecipes: Int,
        val favoriteRecipes: Int,
        val beerRecipes: Int,
        val wineRecipes: Int,
        val meadRecipes: Int,
        val ciderRecipes: Int,
        val kombuchaRecipes: Int,
        val specialtyRecipes: Int
    ) {
        val mostPopularType: BeverageType
            get() = when (maxOf(beerRecipes, wineRecipes, meadRecipes, ciderRecipes, kombuchaRecipes, specialtyRecipes)) {
                beerRecipes -> BeverageType.BEER
                wineRecipes -> BeverageType.WINE
                meadRecipes -> BeverageType.MEAD
                ciderRecipes -> BeverageType.CIDER
                kombuchaRecipes -> BeverageType.KOMBUCHA
                else -> BeverageType.SPECIALTY
            }
    }
}