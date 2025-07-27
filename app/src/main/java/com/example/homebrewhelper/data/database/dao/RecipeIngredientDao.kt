package com.example.homebrewhelper.data.database.dao

import androidx.room.*
import com.example.homebrewhelper.data.database.entity.RecipeIngredient
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for RecipeIngredient operations
 * Manages the relationship between recipes and ingredients with usage-specific data
 */
@Dao
interface RecipeIngredientDao {
    
    // Create operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(recipeIngredients: List<RecipeIngredient>)
    
    // Read operations - Basic
    @Query("SELECT * FROM recipe_ingredients WHERE id = :id")
    suspend fun getRecipeIngredientById(id: String): RecipeIngredient?
    
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY step ASC, additionTime ASC, priority DESC")
    fun observeRecipeIngredients(recipeId: String): Flow<List<RecipeIngredient>>
    
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY step ASC, additionTime ASC, priority DESC")
    suspend fun getRecipeIngredients(recipeId: String): List<RecipeIngredient>
    
    // Read operations - By step and timing
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId AND step = :step ORDER BY additionTime ASC, priority DESC")
    suspend fun getRecipeIngredientsByStep(recipeId: String, step: String): List<RecipeIngredient>
    
    @Query("SELECT DISTINCT step FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY additionTime ASC")
    suspend fun getRecipeSteps(recipeId: String): List<String>
    
    @Query("""
        SELECT * FROM recipe_ingredients 
        WHERE recipeId = :recipeId 
        AND step = :step 
        AND additionTime >= :startTime 
        AND additionTime <= :endTime
        ORDER BY additionTime ASC, priority DESC
    """)
    suspend fun getRecipeIngredientsInTimeRange(
        recipeId: String, 
        step: String, 
        startTime: Int, 
        endTime: Int
    ): List<RecipeIngredient>
    
    // Read operations - By ingredient
    @Query("SELECT * FROM recipe_ingredients WHERE ingredientId = :ingredientId ORDER BY additionTime ASC")
    suspend fun getUsagesOfIngredient(ingredientId: String): List<RecipeIngredient>
    
    @Query("""
        SELECT COUNT(DISTINCT recipeId) FROM recipe_ingredients 
        WHERE ingredientId = :ingredientId
    """)
    suspend fun getRecipeCountUsingIngredient(ingredientId: String): Int
    
    // Read operations - Substitutions
    @Query("SELECT * FROM recipe_ingredients WHERE originalIngredientId = :originalId")
    suspend fun getSubstitutionsForIngredient(originalId: String): List<RecipeIngredient>
    
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId AND isSubstitution = 1")
    suspend fun getSubstitutionsInRecipe(recipeId: String): List<RecipeIngredient>
    
    // Read operations - Timing groups
    @Query("""
        SELECT * FROM recipe_ingredients 
        WHERE recipeId = :recipeId 
        AND timingGroup = :group 
        ORDER BY priority DESC, additionTime ASC
    """)
    suspend fun getRecipeIngredientsByTimingGroup(recipeId: String, group: String): List<RecipeIngredient>
    
    @Query("SELECT DISTINCT timingGroup FROM recipe_ingredients WHERE recipeId = :recipeId AND timingGroup != ''")
    suspend fun getTimingGroups(recipeId: String): List<String>
    
    // Read operations - Critical timing
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId AND criticalTiming = 1 ORDER BY additionTime ASC")
    suspend fun getCriticalTimingIngredients(recipeId: String): List<RecipeIngredient>
    
    // Read operations - Optional ingredients
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId AND isOptional = 1 ORDER BY step ASC, additionTime ASC")
    suspend fun getOptionalIngredients(recipeId: String): List<RecipeIngredient>
    
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId AND isOptional = 0 ORDER BY step ASC, additionTime ASC")
    suspend fun getRequiredIngredients(recipeId: String): List<RecipeIngredient>
    
    // Read operations - Calculations and analysis
    @Query("SELECT SUM(gravityContribution) FROM recipe_ingredients WHERE recipeId = :recipeId AND gravityContribution IS NOT NULL")
    suspend fun getTotalGravityContribution(recipeId: String): Double?
    
    @Query("SELECT SUM(bitternessContribution) FROM recipe_ingredients WHERE recipeId = :recipeId AND bitternessContribution IS NOT NULL")
    suspend fun getTotalBitternessContribution(recipeId: String): Double?
    
    @Query("SELECT SUM(colorContribution) FROM recipe_ingredients WHERE recipeId = :recipeId AND colorContribution IS NOT NULL")
    suspend fun getTotalColorContribution(recipeId: String): Double?
    
    @Query("SELECT SUM(estimatedCost) FROM recipe_ingredients WHERE recipeId = :recipeId AND estimatedCost IS NOT NULL")
    suspend fun getTotalEstimatedCost(recipeId: String): Double?
    
    @Query("SELECT SUM(actualCost) FROM recipe_ingredients WHERE recipeId = :recipeId AND actualCost IS NOT NULL")
    suspend fun getTotalActualCost(recipeId: String): Double?
    
    // Update operations
    @Update
    suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient)
    
    @Query("UPDATE recipe_ingredients SET quantity = :quantity, unit = :unit WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Double, unit: String)
    
    @Query("UPDATE recipe_ingredients SET additionTime = :time WHERE id = :id")
    suspend fun updateAdditionTime(id: String, time: Int)
    
    @Query("UPDATE recipe_ingredients SET isOptional = :optional WHERE id = :id")
    suspend fun updateOptionalStatus(id: String, optional: Boolean)
    
    @Query("UPDATE recipe_ingredients SET actualCost = :cost WHERE id = :id")
    suspend fun updateActualCost(id: String, cost: Double?)
    
    @Query("UPDATE recipe_ingredients SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String)
    
    // Delete operations
    @Delete
    suspend fun deleteRecipeIngredient(recipeIngredient: RecipeIngredient)
    
    @Query("DELETE FROM recipe_ingredients WHERE id = :id")
    suspend fun deleteRecipeIngredientById(id: String)
    
    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteAllRecipeIngredients(recipeId: String)
    
    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId AND step = :step")
    suspend fun deleteRecipeIngredientsByStep(recipeId: String, step: String)
    
    @Query("DELETE FROM recipe_ingredients WHERE ingredientId = :ingredientId")
    suspend fun deleteAllUsagesOfIngredient(ingredientId: String)
    
    // Complex operations
    @Transaction
    suspend fun replaceRecipeIngredients(recipeId: String, newIngredients: List<RecipeIngredient>) {
        deleteAllRecipeIngredients(recipeId)
        insertRecipeIngredients(newIngredients)
    }
    
    @Transaction
    suspend fun scaleRecipeIngredients(recipeId: String, scaleFactor: Double) {
        val ingredients = getRecipeIngredients(recipeId)
        val scaledIngredients = ingredients.map { ingredient ->
            ingredient.copy(
                quantity = ingredient.quantity * scaleFactor,
                gravityContribution = ingredient.gravityContribution?.times(scaleFactor),
                bitternessContribution = ingredient.bitternessContribution?.times(scaleFactor),
                colorContribution = ingredient.colorContribution?.times(scaleFactor),
                estimatedCost = ingredient.estimatedCost?.times(scaleFactor)
            )
        }
        replaceRecipeIngredients(recipeId, scaledIngredients)
    }
    
    @Transaction
    suspend fun duplicateRecipeIngredients(sourceRecipeId: String, targetRecipeId: String) {
        val sourceIngredients = getRecipeIngredients(sourceRecipeId)
        val duplicatedIngredients = sourceIngredients.map { ingredient ->
            ingredient.copy(
                id = java.util.UUID.randomUUID().toString(),
                recipeId = targetRecipeId
            )
        }
        insertRecipeIngredients(duplicatedIngredients)
    }
    
    // Batch operations for recipe creation
    @Transaction
    suspend fun addIngredientsToRecipe(recipeId: String, ingredients: List<RecipeIngredient>) {
        val recipeIngredients = ingredients.map { it.copy(recipeId = recipeId) }
        insertRecipeIngredients(recipeIngredients)
    }
    
    // Analytics queries
    @Query("""
        SELECT ri.ingredientId, COUNT(*) as usage_count 
        FROM recipe_ingredients ri 
        GROUP BY ri.ingredientId 
        ORDER BY usage_count DESC 
        LIMIT :limit
    """)
    suspend fun getMostUsedIngredients(limit: Int = 20): List<IngredientUsage>
    
    @Query("""
        SELECT AVG(quantity) FROM recipe_ingredients 
        WHERE ingredientId = :ingredientId AND unit = :unit
    """)
    suspend fun getAverageQuantityForIngredient(ingredientId: String, unit: String): Double?
    
    // Data classes for query results
    data class IngredientUsage(
        val ingredientId: String,
        val usage_count: Int
    )
}