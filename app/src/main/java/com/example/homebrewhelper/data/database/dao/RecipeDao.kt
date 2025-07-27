package com.example.homebrewhelper.data.database.dao

import androidx.room.*
import com.example.homebrewhelper.data.database.entity.Recipe
import com.example.homebrewhelper.data.model.BeverageType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Recipe operations
 * Provides comprehensive CRUD operations and complex queries for recipe management
 */
@Dao
interface RecipeDao {
    
    // Create operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)
    
    // Read operations - Basic
    @Query("SELECT * FROM recipes WHERE id = :recipeId AND isDeleted = 0")
    suspend fun getRecipeById(recipeId: String): Recipe?
    
    @Query("SELECT * FROM recipes WHERE id = :recipeId AND isDeleted = 0")
    fun observeRecipeById(recipeId: String): Flow<Recipe?>
    
    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeAllRecipes(): Flow<List<Recipe>>
    
    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun getAllRecipes(): List<Recipe>
    
    // Read operations - By beverage type
    @Query("SELECT * FROM recipes WHERE beverageType = :beverageType AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeRecipesByType(beverageType: BeverageType): Flow<List<Recipe>>
    
    @Query("SELECT * FROM recipes WHERE beverageType = :beverageType AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun getRecipesByType(beverageType: BeverageType): List<Recipe>
    
    // Read operations - Search and filter
    @Query("""
        SELECT * FROM recipes 
        WHERE (name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        AND isDeleted = 0 
        ORDER BY 
            CASE WHEN name LIKE :searchQuery || '%' THEN 1 ELSE 2 END,
            updatedAt DESC
    """)
    fun searchRecipes(searchQuery: String): Flow<List<Recipe>>
    
    @Query("""
        SELECT * FROM recipes 
        WHERE beverageType = :beverageType 
        AND (name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        AND isDeleted = 0 
        ORDER BY updatedAt DESC
    """)
    fun searchRecipesByType(beverageType: BeverageType, searchQuery: String): Flow<List<Recipe>>
    
    @Query("""
        SELECT * FROM recipes 
        WHERE difficulty = :difficulty 
        AND isDeleted = 0 
        ORDER BY updatedAt DESC
    """)
    fun getRecipesByDifficulty(difficulty: Int): Flow<List<Recipe>>
    
    @Query("""
        SELECT * FROM recipes 
        WHERE difficulty >= :minDifficulty AND difficulty <= :maxDifficulty 
        AND isDeleted = 0 
        ORDER BY difficulty ASC, updatedAt DESC
    """)
    fun getRecipesByDifficultyRange(minDifficulty: Int, maxDifficulty: Int): Flow<List<Recipe>>
    
    // Read operations - Favorites and recently used
    @Query("SELECT * FROM recipes WHERE isFavorite = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeFavoriteRecipes(): Flow<List<Recipe>>
    
    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentRecipes(limit: Int = 10): Flow<List<Recipe>>
    
    // Read operations - Recipe variations
    @Query("SELECT * FROM recipes WHERE parentRecipeId = :parentId AND isDeleted = 0 ORDER BY version ASC")
    fun getRecipeVariations(parentId: String): Flow<List<Recipe>>
    
    @Query("SELECT * FROM recipes WHERE parentRecipeId = :parentId AND isDeleted = 0 ORDER BY version DESC LIMIT 1")
    suspend fun getLatestRecipeVariation(parentId: String): Recipe?
    
    // Read operations - Statistics
    @Query("SELECT COUNT(*) FROM recipes WHERE beverageType = :beverageType AND isDeleted = 0")
    suspend fun getRecipeCountByType(beverageType: BeverageType): Int
    
    @Query("SELECT COUNT(*) FROM recipes WHERE isDeleted = 0")
    suspend fun getTotalRecipeCount(): Int
    
    @Query("SELECT COUNT(*) FROM recipes WHERE isFavorite = 1 AND isDeleted = 0")
    suspend fun getFavoriteRecipeCount(): Int
    
    @Query("""
        SELECT AVG(difficulty) FROM recipes 
        WHERE beverageType = :beverageType AND isDeleted = 0
    """)
    suspend fun getAverageDifficultyByType(beverageType: BeverageType): Double?
    
    // Update operations
    @Update
    suspend fun updateRecipe(recipe: Recipe)
    
    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean)
    
    @Query("UPDATE recipes SET updatedAt = :timestamp WHERE id = :recipeId")
    suspend fun touchRecipe(recipeId: String, timestamp: kotlinx.datetime.Instant)
    
    @Query("""
        UPDATE recipes SET 
            originalGravity = :og, 
            finalGravity = :fg, 
            estimatedAbv = :abv,
            updatedAt = :timestamp
        WHERE id = :recipeId
    """)
    suspend fun updateGravityReadings(
        recipeId: String, 
        og: Double?, 
        fg: Double?, 
        abv: Double?,
        timestamp: kotlinx.datetime.Instant
    )
    
    // Delete operations
    @Query("UPDATE recipes SET isDeleted = 1 WHERE id = :recipeId")
    suspend fun softDeleteRecipe(recipeId: String)
    
    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun hardDeleteRecipe(recipeId: String)
    
    @Query("UPDATE recipes SET isDeleted = 0 WHERE id = :recipeId")
    suspend fun restoreRecipe(recipeId: String)
    
    @Query("DELETE FROM recipes WHERE isDeleted = 1")
    suspend fun permanentlyDeleteAllSoftDeleted()
    
    // Complex queries for recipe management
    @Query("""
        SELECT * FROM recipes 
        WHERE beverageType = :beverageType 
        AND batchSizeGallons >= :minBatchSize 
        AND batchSizeGallons <= :maxBatchSize
        AND difficulty >= :minDifficulty 
        AND difficulty <= :maxDifficulty
        AND isDeleted = 0
        ORDER BY 
            CASE :sortBy
                WHEN 'name' THEN name
                WHEN 'difficulty' THEN CAST(difficulty AS TEXT)
                WHEN 'batch_size' THEN CAST(batchSizeGallons AS TEXT)
                ELSE CAST(updatedAt AS TEXT)
            END ASC
    """)
    fun getFilteredRecipes(
        beverageType: BeverageType,
        minBatchSize: Double,
        maxBatchSize: Double,
        minDifficulty: Int,
        maxDifficulty: Int,
        sortBy: String = "updated"
    ): Flow<List<Recipe>>
    
    @Query("""
        SELECT DISTINCT author FROM recipes 
        WHERE author != '' AND isDeleted = 0 
        ORDER BY author ASC
    """)
    suspend fun getAllAuthors(): List<String>
    
    @Query("""
        SELECT DISTINCT source FROM recipes 
        WHERE source != '' AND isDeleted = 0 
        ORDER BY source ASC
    """)
    suspend fun getAllSources(): List<String>
    
    // Recipe scaling helper queries
    @Query("""
        SELECT * FROM recipes 
        WHERE batchSizeGallons = :batchSize 
        AND beverageType = :beverageType 
        AND isDeleted = 0
        ORDER BY updatedAt DESC
    """)
    suspend fun getRecipesByBatchSize(batchSize: Double, beverageType: BeverageType): List<Recipe>
    
    // Batch operations
    @Transaction
    suspend fun duplicateRecipe(originalId: String, newName: String): String {
        val original = getRecipeById(originalId)
        return if (original != null) {
            val newId = java.util.UUID.randomUUID().toString()
            val duplicate = original.copy(
                id = newId,
                name = newName,
                parentRecipeId = originalId,
                version = 1,
                createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis())
            )
            insertRecipe(duplicate)
            newId
        } else {
            throw IllegalArgumentException("Original recipe not found: $originalId")
        }
    }
}