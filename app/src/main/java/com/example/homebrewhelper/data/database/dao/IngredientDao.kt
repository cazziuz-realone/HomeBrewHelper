package com.example.homebrewhelper.data.database.dao

import androidx.room.*
import com.example.homebrewhelper.data.database.entity.Ingredient
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.model.IngredientType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Ingredient operations
 * Manages the ingredient database with support for searching, filtering, and categorization
 */
@Dao
interface IngredientDao {
    
    // Create operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredientsIgnoreConflicts(ingredients: List<Ingredient>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredientsReplaceConflicts(ingredients: List<Ingredient>)
    
    // Read operations - Basic
    @Query("SELECT * FROM ingredients WHERE id = :ingredientId AND isDeleted = 0")
    suspend fun getIngredientById(ingredientId: String): Ingredient?
    
    @Query("SELECT * FROM ingredients WHERE id = :ingredientId AND isDeleted = 0")
    fun observeIngredientById(ingredientId: String): Flow<Ingredient?>
    
    @Query("SELECT * FROM ingredients WHERE isDeleted = 0 ORDER BY name ASC")
    fun observeAllIngredients(): Flow<List<Ingredient>>
    
    @Query("SELECT * FROM ingredients WHERE isDeleted = 0 ORDER BY name ASC")
    suspend fun getAllIngredients(): List<Ingredient>
    
    // Read operations - By type and category
    @Query("SELECT * FROM ingredients WHERE type = :type AND isDeleted = 0 ORDER BY name ASC")
    fun observeIngredientsByType(type: IngredientType): Flow<List<Ingredient>>
    
    @Query("SELECT * FROM ingredients WHERE type = :type AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getIngredientsByType(type: IngredientType): List<Ingredient>
    
    @Query("SELECT * FROM ingredients WHERE category = :category AND isDeleted = 0 ORDER BY name ASC")
    fun getIngredientsByCategory(category: String): Flow<List<Ingredient>>
    
    @Query("SELECT DISTINCT category FROM ingredients WHERE category != '' AND isDeleted = 0 ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    @Query("SELECT DISTINCT subcategory FROM ingredients WHERE subcategory != '' AND category = :category AND isDeleted = 0 ORDER BY subcategory ASC")
    suspend fun getSubcategoriesByCategory(category: String): List<String>
    
    // Read operations - Search and filter
    @Query("""
        SELECT * FROM ingredients 
        WHERE (name LIKE '%' || :searchQuery || '%' 
            OR description LIKE '%' || :searchQuery || '%'
            OR category LIKE '%' || :searchQuery || '%'
            OR brand LIKE '%' || :searchQuery || '%')
        AND isDeleted = 0 
        ORDER BY 
            CASE WHEN name LIKE :searchQuery || '%' THEN 1 ELSE 2 END,
            name ASC
    """)
    fun searchIngredients(searchQuery: String): Flow<List<Ingredient>>
    
    @Query("""
        SELECT * FROM ingredients 
        WHERE type = :type
        AND (name LIKE '%' || :searchQuery || '%' 
            OR description LIKE '%' || :searchQuery || '%'
            OR category LIKE '%' || :searchQuery || '%')
        AND isDeleted = 0 
        ORDER BY name ASC
    """)
    fun searchIngredientsByType(type: IngredientType, searchQuery: String): Flow<List<Ingredient>>
    
    // Read operations - By beverage compatibility
    @Query("""
        SELECT * FROM ingredients 
        WHERE applicableBeverages LIKE '%' || :beverageType || '%'
        AND isDeleted = 0 
        ORDER BY 
            CASE WHEN preferredBeverages LIKE '%' || :beverageType || '%' THEN 1 ELSE 2 END,
            name ASC
    """)
    fun getIngredientsForBeverage(beverageType: String): Flow<List<Ingredient>>
    
    @Query("""
        SELECT * FROM ingredients 
        WHERE type = :type
        AND applicableBeverages LIKE '%' || :beverageType || '%'
        AND isDeleted = 0 
        ORDER BY name ASC
    """)
    fun getIngredientsForBeverageAndType(type: IngredientType, beverageType: String): Flow<List<Ingredient>>
    
    // Read operations - By availability and quality
    @Query("SELECT * FROM ingredients WHERE isCommonlyAvailable = 1 AND isDeleted = 0 ORDER BY name ASC")
    fun getCommonlyAvailableIngredients(): Flow<List<Ingredient>>
    
    @Query("SELECT * FROM ingredients WHERE isCustom = 1 AND isDeleted = 0 ORDER BY name ASC")
    fun getCustomIngredients(): Flow<List<Ingredient>>
    
    @Query("SELECT * FROM ingredients WHERE qualityGrade = :grade AND isDeleted = 0 ORDER BY name ASC")
    fun getIngredientsByQuality(grade: String): Flow<List<Ingredient>>
    
    // Read operations - By supplier and brand
    @Query("SELECT DISTINCT brand FROM ingredients WHERE brand != '' AND isDeleted = 0 ORDER BY brand ASC")
    suspend fun getAllBrands(): List<String>
    
    @Query("SELECT DISTINCT supplier FROM ingredients WHERE supplier != '' AND isDeleted = 0 ORDER BY supplier ASC")
    suspend fun getAllSuppliers(): List<String>
    
    @Query("SELECT * FROM ingredients WHERE brand = :brand AND isDeleted = 0 ORDER BY name ASC")
    fun getIngredientsByBrand(brand: String): Flow<List<Ingredient>>
    
    @Query("SELECT * FROM ingredients WHERE supplier = :supplier AND isDeleted = 0 ORDER BY name ASC")
    fun getIngredientsBySupplier(supplier: String): Flow<List<Ingredient>>
    
    // Read operations - Substitutions
    @Query("""
        SELECT * FROM ingredients 
        WHERE substitutes LIKE '%' || :ingredientId || '%'
        AND isDeleted = 0 
        ORDER BY substitutionRatio ASC, name ASC
    """)
    suspend fun getSubstitutesFor(ingredientId: String): List<Ingredient>
    
    @Query("""
        SELECT * FROM ingredients 
        WHERE id IN (
            SELECT DISTINCT id FROM ingredients 
            WHERE substitutes LIKE '%' || :ingredientId || '%'
        )
        AND type = :type
        AND isDeleted = 0 
        ORDER BY substitutionRatio ASC, name ASC
    """)
    suspend fun getSubstitutesForByType(ingredientId: String, type: IngredientType): List<Ingredient>
    
    // Read operations - Advanced filtering
    @Query("""
        SELECT * FROM ingredients 
        WHERE type = :type
        AND (:minIntensity IS NULL OR intensity >= :minIntensity)
        AND (:maxIntensity IS NULL OR intensity <= :maxIntensity)
        AND (:minCost IS NULL OR averageCostPerUnit >= :minCost)
        AND (:maxCost IS NULL OR averageCostPerUnit <= :maxCost)
        AND (:requireCommon = 0 OR isCommonlyAvailable = 1)
        AND isDeleted = 0
        ORDER BY name ASC
    """)
    fun getFilteredIngredients(
        type: IngredientType,
        minIntensity: Int?,
        maxIntensity: Int?,
        minCost: Double?,
        maxCost: Double?,
        requireCommon: Boolean = false
    ): Flow<List<Ingredient>>
    
    // Read operations - Statistics
    @Query("SELECT COUNT(*) FROM ingredients WHERE type = :type AND isDeleted = 0")
    suspend fun getIngredientCountByType(type: IngredientType): Int
    
    @Query("SELECT COUNT(*) FROM ingredients WHERE isDeleted = 0")
    suspend fun getTotalIngredientCount(): Int
    
    @Query("SELECT COUNT(*) FROM ingredients WHERE isCustom = 1 AND isDeleted = 0")
    suspend fun getCustomIngredientCount(): Int
    
    @Query("SELECT AVG(averageCostPerUnit) FROM ingredients WHERE averageCostPerUnit IS NOT NULL AND isDeleted = 0")
    suspend fun getAverageIngredientCost(): Double?
    
    // Update operations
    @Update
    suspend fun updateIngredient(ingredient: Ingredient)
    
    @Query("UPDATE ingredients SET averageCostPerUnit = :cost WHERE id = :ingredientId")
    suspend fun updateIngredientCost(ingredientId: String, cost: Double?)
    
    @Query("UPDATE ingredients SET isCommonlyAvailable = :available WHERE id = :ingredientId")
    suspend fun updateAvailability(ingredientId: String, available: Boolean)
    
    @Query("UPDATE ingredients SET updatedAt = :timestamp WHERE id = :ingredientId")
    suspend fun touchIngredient(ingredientId: String, timestamp: kotlinx.datetime.Instant)
    
    // Delete operations
    @Query("UPDATE ingredients SET isDeleted = 1 WHERE id = :ingredientId")
    suspend fun softDeleteIngredient(ingredientId: String)
    
    @Query("DELETE FROM ingredients WHERE id = :ingredientId")
    suspend fun hardDeleteIngredient(ingredientId: String)
    
    @Query("UPDATE ingredients SET isDeleted = 0 WHERE id = :ingredientId")
    suspend fun restoreIngredient(ingredientId: String)
    
    @Query("DELETE FROM ingredients WHERE isDeleted = 1")
    suspend fun permanentlyDeleteAllSoftDeleted()
    
    // Bulk operations
    @Transaction
    suspend fun bulkUpdateCosts(ingredientCosts: Map<String, Double>) {
        ingredientCosts.forEach { (id, cost) ->
            updateIngredientCost(id, cost)
        }
    }
    
    @Transaction
    suspend fun importIngredients(ingredients: List<Ingredient>, replaceExisting: Boolean = false) {
        android.util.Log.d("HomeBrewHelper", "Importing ${ingredients.size} ingredients (replaceExisting: $replaceExisting)")
        
        if (replaceExisting) {
            // Replace all existing ingredients
            android.util.Log.d("HomeBrewHelper", "Using REPLACE strategy for import")
            insertIngredientsReplaceConflicts(ingredients)
        } else {
            // Only insert ingredients that don't already exist (based on ID)
            android.util.Log.d("HomeBrewHelper", "Using IGNORE strategy for import")
            insertIngredientsIgnoreConflicts(ingredients)
        }
        
        android.util.Log.d("HomeBrewHelper", "Import completed successfully")
    }
}