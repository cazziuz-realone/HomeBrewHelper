package com.example.homebrewhelper.data.repository

import com.example.homebrewhelper.data.database.dao.IngredientDao
import com.example.homebrewhelper.data.database.entity.Ingredient
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.model.IngredientType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for ingredient-related operations
 * Provides ingredient management with search, filtering, and substitution logic
 */
@Singleton
class IngredientRepository @Inject constructor(
    private val ingredientDao: IngredientDao
) {
    
    // Ingredient CRUD operations
    suspend fun createIngredient(ingredient: Ingredient): Result<String> {
        return try {
            ingredientDao.insertIngredient(ingredient)
            Result.success(ingredient.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getIngredient(id: String): Ingredient? {
        return ingredientDao.getIngredientById(id)
    }
    
    fun observeIngredient(id: String): Flow<Ingredient?> {
        return ingredientDao.observeIngredientById(id)
    }
    
    fun observeAllIngredients(): Flow<List<Ingredient>> {
        return ingredientDao.observeAllIngredients()
    }
    
    suspend fun updateIngredient(ingredient: Ingredient): Result<Unit> {
        return try {
            val updatedIngredient = ingredient.copy(
                updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            )
            ingredientDao.updateIngredient(updatedIngredient)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteIngredient(id: String): Result<Unit> {
        return try {
            ingredientDao.softDeleteIngredient(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Ingredient filtering and search
    fun getIngredientsByType(type: IngredientType): Flow<List<Ingredient>> {
        return ingredientDao.observeIngredientsByType(type)
    }
    
    fun getIngredientsForBeverage(beverageType: BeverageType): Flow<List<Ingredient>> {
        return ingredientDao.getIngredientsForBeverage(beverageType.name)
    }
    
    fun getIngredientsForBeverageAndType(
        beverageType: BeverageType,
        ingredientType: IngredientType
    ): Flow<List<Ingredient>> {
        return ingredientDao.getIngredientsForBeverageAndType(ingredientType, beverageType.name)
    }
    
    fun searchIngredients(query: String): Flow<List<Ingredient>> {
        return ingredientDao.searchIngredients(query)
    }
    
    fun searchIngredientsByType(type: IngredientType, query: String): Flow<List<Ingredient>> {
        return ingredientDao.searchIngredientsByType(type, query)
    }
    
    // Ingredient categories and organization
    suspend fun getCategories(): List<String> {
        return ingredientDao.getAllCategories()
    }
    
    suspend fun getSubcategories(category: String): List<String> {
        return ingredientDao.getSubcategoriesByCategory(category)
    }
    
    suspend fun getBrands(): List<String> {
        return ingredientDao.getAllBrands()
    }
    
    suspend fun getSuppliers(): List<String> {
        return ingredientDao.getAllSuppliers()
    }
    
    fun getIngredientsByBrand(brand: String): Flow<List<Ingredient>> {
        return ingredientDao.getIngredientsByBrand(brand)
    }
    
    fun getIngredientsBySupplier(supplier: String): Flow<List<Ingredient>> {
        return ingredientDao.getIngredientsBySupplier(supplier)
    }
    
    // Ingredient availability and quality
    fun getCommonlyAvailableIngredients(): Flow<List<Ingredient>> {
        return ingredientDao.getCommonlyAvailableIngredients()
    }
    
    fun getCustomIngredients(): Flow<List<Ingredient>> {
        return ingredientDao.getCustomIngredients()
    }
    
    fun getIngredientsByQuality(grade: String): Flow<List<Ingredient>> {
        return ingredientDao.getIngredientsByQuality(grade)
    }
    
    // Ingredient substitutions
    suspend fun getSubstitutesFor(ingredientId: String): List<Ingredient> {
        return ingredientDao.getSubstitutesFor(ingredientId)
    }
    
    suspend fun getSubstitutesForByType(ingredientId: String, type: IngredientType): List<Ingredient> {
        return ingredientDao.getSubstitutesForByType(ingredientId, type)
    }
    
    suspend fun findBestSubstitute(
        originalIngredientId: String,
        beverageType: BeverageType,
        prioritizeAvailability: Boolean = true
    ): Ingredient? {
        val substitutes = getSubstitutesFor(originalIngredientId)
        val original = getIngredient(originalIngredientId) ?: return null
        
        return substitutes
            .filter { it.isSuitableFor(beverageType) }
            .sortedWith { a, b ->
                when {
                    prioritizeAvailability && a.isCommonlyAvailable != b.isCommonlyAvailable -> {
                        if (a.isCommonlyAvailable) -1 else 1
                    }
                    a.substitutionRatio != b.substitutionRatio -> {
                        a.substitutionRatio.compareTo(b.substitutionRatio)
                    }
                    a.intensity != b.intensity -> {
                        kotlin.math.abs(a.intensity - original.intensity) - kotlin.math.abs(b.intensity - original.intensity)
                    }
                    else -> a.name.compareTo(b.name)
                }
            }
            .firstOrNull()
    }
    
    // Advanced filtering
    fun getFilteredIngredients(
        type: IngredientType,
        minIntensity: Int? = null,
        maxIntensity: Int? = null,
        minCost: Double? = null,
        maxCost: Double? = null,
        requireCommon: Boolean = false
    ): Flow<List<Ingredient>> {
        return ingredientDao.getFilteredIngredients(
            type = type,
            minIntensity = minIntensity,
            maxIntensity = maxIntensity,
            minCost = minCost,
            maxCost = maxCost,
            requireCommon = requireCommon
        )
    }
    
    // Ingredient statistics
    suspend fun getIngredientStats(): IngredientStats {
        return IngredientStats(
            totalIngredients = ingredientDao.getTotalIngredientCount(),
            customIngredients = ingredientDao.getCustomIngredientCount(),
            grainCount = ingredientDao.getIngredientCountByType(IngredientType.GRAIN),
            hopCount = ingredientDao.getIngredientCountByType(IngredientType.HOPS),
            yeastCount = ingredientDao.getIngredientCountByType(IngredientType.YEAST),
            averageCost = ingredientDao.getAverageIngredientCost()
        )
    }
    
    // Cost management
    suspend fun updateIngredientCost(ingredientId: String, cost: Double?): Result<Unit> {
        return try {
            ingredientDao.updateIngredientCost(ingredientId, cost)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateAvailability(ingredientId: String, available: Boolean): Result<Unit> {
        return try {
            ingredientDao.updateAvailability(ingredientId, available)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun bulkUpdateCosts(ingredientCosts: Map<String, Double>): Result<Unit> {
        return try {
            ingredientDao.bulkUpdateCosts(ingredientCosts)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Import/Export functionality
    suspend fun importIngredients(
        ingredients: List<Ingredient>,
        replaceExisting: Boolean = false
    ): Result<Unit> {
        return try {
            ingredientDao.importIngredients(ingredients, replaceExisting)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Default ingredients initialization - FIXED METHOD CALL
    suspend fun initializeDefaultIngredients(): Result<Unit> {
        return try {
            val existingCount = ingredientDao.getTotalIngredientCount()
            if (existingCount == 0) {
                val defaultIngredients = createDefaultIngredients()
                // Fixed: Use the correct method name from updated DAO
                ingredientDao.insertIngredientsIgnoreConflicts(defaultIngredients)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createDefaultIngredients(): List<Ingredient> {
        return listOf(
            // Common beer grains
            Ingredient(
                name = "Pale 2-Row Malt",
                type = IngredientType.GRAIN,
                description = "Base malt for most beer styles",
                category = "Base Malt",
                subcategory = "2-Row",
                potential = 1.037,
                lovibond = 2.0,
                defaultUnit = "pounds",
                alternateUnits = "[\"kilograms\", \"ounces\"]",
                typicalUsageMin = 8.0,
                typicalUsageMax = 12.0,
                applicableBeverages = "[\"BEER\"]",
                preferredBeverages = "[\"BEER\"]",
                isCommonlyAvailable = true
            ),
            // Common hops
            Ingredient(
                name = "Cascade Hops",
                type = IngredientType.HOPS,
                description = "Citrusy American hop variety",
                category = "Aroma",
                subcategory = "American",
                alphaAcid = 5.5,
                defaultUnit = "ounces",
                alternateUnits = "[\"grams\"]",
                typicalUsageMin = 0.5,
                typicalUsageMax = 2.0,
                applicableBeverages = "[\"BEER\"]",
                preferredBeverages = "[\"BEER\"]",
                flavorProfile = "{\"citrus\": 8, \"floral\": 6, \"piney\": 3}",
                intensity = 4,
                isCommonlyAvailable = true
            ),
            // Common yeast
            Ingredient(
                name = "Safale US-05",
                type = IngredientType.YEAST,
                description = "American ale yeast strain",
                category = "Ale Yeast",
                subcategory = "American",
                defaultUnit = "packets",
                alternateUnits = "[\"grams\"]",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.0,
                applicableBeverages = "[\"BEER\"]",
                preferredBeverages = "[\"BEER\"]",
                isCommonlyAvailable = true
            )
            // Add more default ingredients as needed
        )
    }
    
    data class IngredientStats(
        val totalIngredients: Int,
        val customIngredients: Int,
        val grainCount: Int,
        val hopCount: Int,
        val yeastCount: Int,
        val averageCost: Double?
    )
}