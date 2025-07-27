package com.example.homebrewhelper.data.repository

import com.example.homebrewhelper.data.database.entity.Ingredient
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.model.IngredientType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for initializing default data on first app launch
 */
@Singleton
class InitializationRepository @Inject constructor(
    private val ingredientRepository: IngredientRepository
) {
    
    /**
     * Initialize the app with default data if needed
     */
    suspend fun initializeDefaultData(): Result<Unit> {
        return try {
            // Check if we already have ingredients
            val stats = ingredientRepository.getIngredientStats()
            if (stats.totalIngredients == 0) {
                // Initialize with basic ingredients
                val defaultIngredients = createDefaultIngredients()
                ingredientRepository.importIngredients(defaultIngredients, false)
                    .getOrThrow()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createDefaultIngredients(): List<Ingredient> {
        return listOf(
            // Basic Beer ingredients
            Ingredient(
                name = "Pale 2-Row Malt",
                type = IngredientType.GRAIN,
                description = "Base malt for most beer styles providing fermentable sugars",
                category = "Base Malt",
                subcategory = "2-Row",
                potential = 1.037,
                lovibond = 2.0,
                defaultUnit = "pounds",
                alternateUnits = "kilograms,ounces",
                typicalUsageMin = 8.0,
                typicalUsageMax = 12.0,
                applicableBeverages = "BEER",
                preferredBeverages = "BEER",
                flavorProfile = "Clean, neutral, malty",
                intensity = 2,
                contributes = "Fermentable sugars, body, color",
                isCommonlyAvailable = true
            ),
            
            Ingredient(
                name = "Cascade Hops",
                type = IngredientType.HOPS,
                description = "Classic American hop variety with citrus and floral notes",
                category = "Aroma",
                subcategory = "American",
                alphaAcid = 5.5,
                defaultUnit = "ounces",
                alternateUnits = "grams",
                typicalUsageMin = 0.5,
                typicalUsageMax = 2.0,
                applicableBeverages = "BEER",
                preferredBeverages = "BEER",
                flavorProfile = "Citrus, floral, grapefruit",
                intensity = 4,
                contributes = "Bitterness, aroma, flavor",
                isCommonlyAvailable = true
            ),
            
            Ingredient(
                name = "Safale US-05",
                type = IngredientType.YEAST,
                description = "American ale yeast strain for clean fermentation",
                category = "Ale Yeast",
                subcategory = "American",
                defaultUnit = "packets",
                alternateUnits = "grams",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.0,
                applicableBeverages = "BEER",
                preferredBeverages = "BEER",
                flavorProfile = "Clean, neutral",
                intensity = 1,
                contributes = "Alcohol, carbonation",
                isCommonlyAvailable = true
            ),
            
            // Basic Wine ingredients
            Ingredient(
                name = "Cabernet Sauvignon Grapes",
                type = IngredientType.GRAPES,
                description = "Noble red wine grape variety",
                category = "Red Grapes",
                subcategory = "Bordeaux",
                defaultUnit = "pounds",
                alternateUnits = "kilograms",
                typicalUsageMin = 12.0,
                typicalUsageMax = 18.0,
                applicableBeverages = "WINE",
                preferredBeverages = "WINE",
                flavorProfile = "Dark fruit, tannins, oak",
                intensity = 5,
                contributes = "Sugars, flavor, color, tannins",
                isCommonlyAvailable = true
            ),
            
            Ingredient(
                name = "Tartaric Acid",
                type = IngredientType.ACID,
                description = "Primary acid for wine pH adjustment",
                category = "Acid",
                defaultUnit = "teaspoons",
                alternateUnits = "grams,ounces",
                typicalUsageMin = 0.25,
                typicalUsageMax = 1.0,
                applicableBeverages = "WINE,CIDER,MEAD",
                preferredBeverages = "WINE",
                flavorProfile = "Tart, clean acid",
                intensity = 3,
                contributes = "Acidity, preservation, balance",
                isCommonlyAvailable = true
            ),
            
            // Basic Mead ingredients
            Ingredient(
                name = "Wildflower Honey",
                type = IngredientType.HONEY,
                description = "Light, floral honey perfect for traditional mead",
                category = "Light Honey",
                defaultUnit = "pounds",
                alternateUnits = "kilograms,ounces",
                typicalUsageMin = 2.5,
                typicalUsageMax = 4.0,
                applicableBeverages = "MEAD",
                preferredBeverages = "MEAD",
                flavorProfile = "Floral, sweet, delicate",
                intensity = 3,
                contributes = "Fermentable sugars, flavor, aroma",
                isCommonlyAvailable = true
            ),
            
            Ingredient(
                name = "Yeast Nutrient",
                type = IngredientType.NUTRIENTS,
                description = "Essential nutrients for healthy mead fermentation",
                category = "Nutrients",
                defaultUnit = "teaspoons",
                alternateUnits = "grams",
                typicalUsageMin = 0.5,
                typicalUsageMax = 1.5,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Neutral",
                intensity = 1,
                contributes = "Yeast health, fermentation vigor",
                isCommonlyAvailable = true
            ),
            
            // Basic Cider ingredients
            Ingredient(
                name = "Apple Juice (Fresh)",
                type = IngredientType.APPLE_JUICE,
                description = "Fresh apple juice without preservatives",
                category = "Apple Juice",
                defaultUnit = "gallons",
                alternateUnits = "liters",
                typicalUsageMin = 5.0,
                typicalUsageMax = 6.0,
                applicableBeverages = "CIDER",
                preferredBeverages = "CIDER",
                flavorProfile = "Apple, sweet, fresh",
                intensity = 4,
                contributes = "Fermentable sugars, apple flavor",
                isCommonlyAvailable = true
            ),
            
            // Basic Kombucha ingredients
            Ingredient(
                name = "Black Tea",
                type = IngredientType.TEA,
                description = "Traditional black tea for kombucha brewing",
                category = "Black Tea",
                defaultUnit = "tea bags",
                alternateUnits = "tablespoons,grams",
                typicalUsageMin = 8.0,
                typicalUsageMax = 12.0,
                applicableBeverages = "KOMBUCHA",
                preferredBeverages = "KOMBUCHA",
                flavorProfile = "Robust, tannic, malty",
                intensity = 4,
                contributes = "Flavor, nutrients for SCOBY",
                isCommonlyAvailable = true
            ),
            
            Ingredient(
                name = "Cane Sugar",
                type = IngredientType.SUGAR,
                description = "Pure cane sugar for kombucha fermentation",
                category = "Sugar",
                defaultUnit = "cups",
                alternateUnits = "pounds,grams",
                typicalUsageMin = 1.0,
                typicalUsageMax = 1.5,
                applicableBeverages = "KOMBUCHA,WINE,CIDER",
                preferredBeverages = "KOMBUCHA",
                flavorProfile = "Sweet, clean",
                intensity = 2,
                contributes = "Fermentable sugars",
                isCommonlyAvailable = true
            ),
            
            // Universal ingredients
            Ingredient(
                name = "Filtered Water",
                type = IngredientType.WATER,
                description = "Clean, filtered water free of chlorine and chloramines",
                category = "Water",
                defaultUnit = "gallons",
                alternateUnits = "liters,quarts",
                typicalUsageMin = 5.0,
                typicalUsageMax = 6.0,
                applicableBeverages = "BEER,WINE,MEAD,CIDER,KOMBUCHA,SPECIALTY",
                preferredBeverages = "BEER,WINE,MEAD,CIDER,KOMBUCHA,SPECIALTY",
                flavorProfile = "Neutral, clean",
                intensity = 1,
                contributes = "Base for fermentation",
                isCommonlyAvailable = true
            ),
            
            Ingredient(
                name = "Star San Sanitizer",
                type = IngredientType.SANITIZER,
                description = "No-rinse acid sanitizer for brewing equipment",
                category = "Sanitizer",
                defaultUnit = "ounces",
                alternateUnits = "tablespoons",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.0,
                applicableBeverages = "BEER,WINE,MEAD,CIDER,KOMBUCHA,SPECIALTY",
                preferredBeverages = "BEER,WINE,MEAD,CIDER,KOMBUCHA,SPECIALTY",
                flavorProfile = "Neutral (no-rinse)",
                intensity = 1,
                contributes = "Equipment sanitation",
                isCommonlyAvailable = true
            )
        )
    }
}