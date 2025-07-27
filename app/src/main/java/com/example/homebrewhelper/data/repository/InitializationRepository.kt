package com.example.homebrewhelper.data.repository

import com.example.homebrewhelper.data.database.entity.Ingredient
import com.example.homebrewhelper.data.model.IngredientType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for initializing default data on first app launch
 * Focuses primarily on mead brewing with comprehensive ingredient database
 */
@Singleton
class InitializationRepository @Inject constructor(
    private val ingredientRepository: IngredientRepository
) {
    
    private var hasInitialized = false
    
    /**
     * Initialize the app with default data if needed
     */
    suspend fun initializeDefaultData(): Result<Unit> {
        return try {
            if (hasInitialized) {
                return Result.success(Unit)
            }
            
            // Check if we already have ingredients
            val stats = ingredientRepository.getIngredientStats()
            if (stats.totalIngredients == 0) {
                // Initialize with mead-focused ingredients
                val defaultIngredients = createMeadFocusedIngredients()
                ingredientRepository.importIngredients(defaultIngredients, false)
                    .getOrThrow()
                
                android.util.Log.d("HomeBrewHelper", "Initialized ${defaultIngredients.size} ingredients")
            }
            hasInitialized = true
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("HomeBrewHelper", "Failed to initialize ingredients", e)
            Result.failure(e)
        }
    }
    
    /**
     * Force initialization - useful for testing or manual refresh
     */
    suspend fun forceInitialization(): Result<Unit> {
        return try {
            val ingredients = createMeadFocusedIngredients()
            ingredientRepository.importIngredients(ingredients, true)
                .getOrThrow()
            
            android.util.Log.d("HomeBrewHelper", "Force initialized ${ingredients.size} ingredients")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("HomeBrewHelper", "Failed to force initialize ingredients", e)
            Result.failure(e)
        }
    }
    
    private fun createMeadFocusedIngredients(): List<Ingredient> {
        return listOf(
            // ===== HONEY VARIETIES (Primary Mead Ingredient) =====
            Ingredient(
                name = "Wildflower Honey",
                type = IngredientType.HONEY,
                description = "Light, floral honey perfect for traditional mead. Clean fermentation profile.",
                category = "Light Honey",
                subcategory = "Polyfloral",
                defaultUnit = "pounds",
                alternateUnits = "kilograms,ounces",
                typicalUsageMin = 2.5,
                typicalUsageMax = 4.0,
                applicableBeverages = "MEAD",
                preferredBeverages = "MEAD",
                flavorProfile = "Floral, sweet, delicate, clean",
                intensity = 3,
                contributes = "Fermentable sugars, subtle floral notes",
                isCommonlyAvailable = true,
                averageCostPerUnit = 8.0
            ),
            
            Ingredient(
                name = "Orange Blossom Honey",
                type = IngredientType.HONEY,
                description = "Citrusy, aromatic honey with bright orange notes. Excellent for traditional and fruit meads.",
                category = "Light Honey",
                subcategory = "Monofloral",
                defaultUnit = "pounds",
                alternateUnits = "kilograms,ounces",
                typicalUsageMin = 2.5,
                typicalUsageMax = 4.0,
                applicableBeverages = "MEAD",
                preferredBeverages = "MEAD",
                flavorProfile = "Citrus, orange, bright, aromatic",
                intensity = 4,
                contributes = "Fermentable sugars, citrus aroma and flavor",
                isCommonlyAvailable = true,
                averageCostPerUnit = 10.0
            ),
            
            Ingredient(
                name = "Clover Honey",
                type = IngredientType.HONEY,
                description = "Mild, sweet honey with classic honey flavor. Most common honey variety.",
                category = "Light Honey",
                subcategory = "Monofloral",
                defaultUnit = "pounds",
                alternateUnits = "kilograms,ounces",
                typicalUsageMin = 2.5,
                typicalUsageMax = 4.0,
                applicableBeverages = "MEAD",
                preferredBeverages = "MEAD",
                flavorProfile = "Mild, sweet, classic honey",
                intensity = 2,
                contributes = "Fermentable sugars, traditional honey character",
                isCommonlyAvailable = true,
                averageCostPerUnit = 7.0
            ),
            
            Ingredient(
                name = "Buckwheat Honey",
                type = IngredientType.HONEY,
                description = "Dark, robust honey with malty, molasses-like flavors. Creates complex meads.",
                category = "Dark Honey",
                subcategory = "Monofloral",
                defaultUnit = "pounds",
                alternateUnits = "kilograms,ounces",
                typicalUsageMin = 2.0,
                typicalUsageMax = 3.5,
                applicableBeverages = "MEAD",
                preferredBeverages = "MEAD",
                flavorProfile = "Dark, malty, molasses, robust",
                intensity = 5,
                contributes = "Fermentable sugars, complex dark flavors",
                isCommonlyAvailable = false,
                averageCostPerUnit = 12.0
            ),
            
            Ingredient(
                name = "Tupelo Honey",
                type = IngredientType.HONEY,
                description = "Premium light honey that doesn't crystallize. Buttery, delicate flavor.",
                category = "Light Honey",
                subcategory = "Monofloral",
                defaultUnit = "pounds",
                alternateUnits = "kilograms,ounces",
                typicalUsageMin = 2.5,
                typicalUsageMax = 4.0,
                applicableBeverages = "MEAD",
                preferredBeverages = "MEAD",
                flavorProfile = "Buttery, delicate, smooth, premium",
                intensity = 3,
                contributes = "Fermentable sugars, smooth buttery notes",
                isCommonlyAvailable = false,
                averageCostPerUnit = 15.0
            ),
            
            Ingredient(
                name = "Basswood Honey",
                type = IngredientType.HONEY,
                description = "Light honey with a slight minty finish. Great for traditional meads.",
                category = "Light Honey",
                subcategory = "Monofloral",
                defaultUnit = "pounds",
                alternateUnits = "kilograms,ounces",
                typicalUsageMin = 2.5,
                typicalUsageMax = 4.0,
                applicableBeverages = "MEAD",
                preferredBeverages = "MEAD",
                flavorProfile = "Light, minty finish, fresh",
                intensity = 3,
                contributes = "Fermentable sugars, subtle mint notes",
                isCommonlyAvailable = false,
                averageCostPerUnit = 11.0
            ),
            
            // ===== MEAD YEAST STRAINS =====
            Ingredient(
                name = "Lallemand DistilaMax MW",
                type = IngredientType.YEAST,
                description = "Premium mead yeast with high alcohol tolerance (18% ABV). Clean fermentation profile.",
                category = "Mead Yeast",
                subcategory = "High Alcohol",
                defaultUnit = "packets",
                alternateUnits = "grams",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.0,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Clean, neutral, high alcohol tolerance",
                intensity = 1,
                contributes = "Alcohol, clean fermentation, high ABV capability",
                isCommonlyAvailable = true,
                averageCostPerUnit = 3.5
            ),
            
            Ingredient(
                name = "Red Star Premier Blanc",
                type = IngredientType.YEAST,
                description = "Excellent mead yeast that emphasizes fruit and floral characteristics. Moderate alcohol tolerance.",
                category = "Mead Yeast",
                subcategory = "Aromatic",
                defaultUnit = "packets",
                alternateUnits = "grams",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.0,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Enhances fruit and floral notes",
                intensity = 2,
                contributes = "Alcohol, enhanced aromatics, fruit character",
                isCommonlyAvailable = true,
                averageCostPerUnit = 2.5
            ),
            
            Ingredient(
                name = "Lallemand 71B-1122",
                type = IngredientType.YEAST,
                description = "Popular mead yeast that reduces malic acid. Great for fruit meads and melomels.",
                category = "Mead Yeast",
                subcategory = "Fruit Forward",
                defaultUnit = "packets",
                alternateUnits = "grams",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.0,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Fruit-forward, reduces acidity",
                intensity = 2,
                contributes = "Alcohol, enhanced fruit character, acid reduction",
                isCommonlyAvailable = true,
                averageCostPerUnit = 2.8
            ),
            
            Ingredient(
                name = "Wyeast 4184 Sweet Mead",
                type = IngredientType.YEAST,
                description = "Low alcohol tolerance yeast perfect for sweet meads. Retains residual honey character.",
                category = "Mead Yeast",
                subcategory = "Sweet Mead",
                defaultUnit = "packets",
                alternateUnits = "vials",
                typicalUsageMin = 1.0,
                typicalUsageMax = 1.0,
                applicableBeverages = "MEAD",
                preferredBeverages = "MEAD",
                flavorProfile = "Preserves sweetness, honey character",
                intensity = 2,
                contributes = "Moderate alcohol, residual sweetness",
                isCommonlyAvailable = true,
                averageCostPerUnit = 8.0
            ),
            
            Ingredient(
                name = "Mangrove Jack's M05 Mead Yeast",
                type = IngredientType.YEAST,
                description = "Specialized mead yeast with excellent honey character retention and moderate alcohol tolerance.",
                category = "Mead Yeast",
                subcategory = "Traditional",
                defaultUnit = "packets",
                alternateUnits = "grams",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.0,
                applicableBeverages = "MEAD",
                preferredBeverages = "MEAD",
                flavorProfile = "Retains honey character, balanced",
                intensity = 2,
                contributes = "Alcohol, honey character retention",
                isCommonlyAvailable = true,
                averageCostPerUnit = 4.0
            ),
            
            // ===== MEAD NUTRIENTS =====
            Ingredient(
                name = "Fermaid O",
                type = IngredientType.NUTRIENTS,
                description = "Organic yeast nutrient derived from yeast hulls. Excellent for mead fermentation.",
                category = "Organic Nutrients",
                subcategory = "Yeast Hulls",
                defaultUnit = "grams",
                alternateUnits = "teaspoons,ounces",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.0,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Neutral",
                intensity = 1,
                contributes = "Yeast health, fermentation vigor, organic nutrition",
                isCommonlyAvailable = true,
                averageCostPerUnit = 8.0
            ),
            
            Ingredient(
                name = "Fermaid K",
                type = IngredientType.NUTRIENTS,
                description = "Complete yeast nutrient blend with vitamins, minerals, and amino acids.",
                category = "Complete Nutrients",
                subcategory = "Vitamin Blend",
                defaultUnit = "grams",
                alternateUnits = "teaspoons,ounces",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.0,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Neutral",
                intensity = 1,
                contributes = "Complete yeast nutrition, healthy fermentation",
                isCommonlyAvailable = true,
                averageCostPerUnit = 12.0
            ),
            
            Ingredient(
                name = "DAP (Diammonium Phosphate)",
                type = IngredientType.NUTRIENTS,
                description = "Inorganic nitrogen source for yeast nutrition. Use sparingly in mead.",
                category = "Nitrogen Source",
                subcategory = "Inorganic",
                defaultUnit = "grams",
                alternateUnits = "teaspoons",
                typicalUsageMin = 0.5,
                typicalUsageMax = 1.0,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "WINE",
                flavorProfile = "Neutral",
                intensity = 1,
                contributes = "Nitrogen for yeast growth",
                usageNotes = "Use sparingly - can create harsh flavors in mead",
                isCommonlyAvailable = true,
                averageCostPerUnit = 4.0
            ),
            
            Ingredient(
                name = "Go-Ferm Protect Evolution",
                type = IngredientType.NUTRIENTS,
                description = "Yeast rehydration nutrient that improves yeast viability and alcohol tolerance.",
                category = "Rehydration Nutrients",
                subcategory = "Yeast Protection",
                defaultUnit = "grams",
                alternateUnits = "teaspoons",
                typicalUsageMin = 1.25,
                typicalUsageMax = 1.25,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Neutral",
                intensity = 1,
                contributes = "Yeast health, improved alcohol tolerance",
                usageNotes = "Use during yeast rehydration only",
                isCommonlyAvailable = true,
                averageCostPerUnit = 15.0
            ),
            
            // ===== ACIDS AND pH ADJUSTMENT =====
            Ingredient(
                name = "Tartaric Acid",
                type = IngredientType.ACID,
                description = "Primary acid for pH adjustment in mead. Clean, wine-like acidity.",
                category = "Wine Acid",
                subcategory = "Primary Acid",
                defaultUnit = "grams",
                alternateUnits = "teaspoons,ounces",
                typicalUsageMin = 1.0,
                typicalUsageMax = 3.0,
                applicableBeverages = "MEAD,WINE,CIDER",
                preferredBeverages = "MEAD",
                flavorProfile = "Clean, tart, wine-like",
                intensity = 4,
                contributes = "Acidity, pH balance, preservation",
                isCommonlyAvailable = true,
                averageCostPerUnit = 3.0
            ),
            
            Ingredient(
                name = "Malic Acid",
                type = IngredientType.ACID,
                description = "Softer acid that adds apple-like tartness. Good for fruit meads.",
                category = "Fruit Acid",
                subcategory = "Secondary Acid",
                defaultUnit = "grams",
                alternateUnits = "teaspoons,ounces",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.5,
                applicableBeverages = "MEAD,WINE,CIDER",
                preferredBeverages = "MEAD",
                flavorProfile = "Apple-like, softer tartness",
                intensity = 3,
                contributes = "Balanced acidity, fruit character",
                isCommonlyAvailable = true,
                averageCostPerUnit = 3.5
            ),
            
            Ingredient(
                name = "Citric Acid",
                type = IngredientType.ACID,
                description = "Sharp, citrusy acid. Use sparingly as it can create metallic flavors.",
                category = "Citrus Acid",
                subcategory = "Accent Acid",
                defaultUnit = "grams",
                alternateUnits = "teaspoons",
                typicalUsageMin = 0.5,
                typicalUsageMax = 1.5,
                applicableBeverages = "MEAD,WINE,CIDER",
                preferredBeverages = "CIDER",
                flavorProfile = "Sharp, citrusy, bright",
                intensity = 5,
                contributes = "Bright acidity, citrus notes",
                usageNotes = "Use sparingly - can create metallic flavors",
                isCommonlyAvailable = true,
                averageCostPerUnit = 2.5
            ),
            
            // ===== TANNINS AND STRUCTURE =====
            Ingredient(
                name = "FT Rouge Tannin",
                type = IngredientType.TANNINS,
                description = "Grape tannin that adds structure and mouthfeel to mead. Improves aging potential.",
                category = "Grape Tannin",
                subcategory = "Structural",
                defaultUnit = "grams",
                alternateUnits = "teaspoons",
                typicalUsageMin = 0.5,
                typicalUsageMax = 2.0,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Astringent, structured, wine-like",
                intensity = 4,
                contributes = "Structure, mouthfeel, aging potential",
                isCommonlyAvailable = true,
                averageCostPerUnit = 12.0
            ),
            
            Ingredient(
                name = "Oak Tannin Powder",
                type = IngredientType.TANNINS,
                description = "Oak-derived tannins that add complexity and aging character.",
                category = "Oak Tannin",
                subcategory = "Flavor Tannin",
                defaultUnit = "grams",
                alternateUnits = "teaspoons",
                typicalUsageMin = 0.25,
                typicalUsageMax = 1.0,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Oak, vanilla, complexity",
                intensity = 3,
                contributes = "Oak character, complexity, aging notes",
                isCommonlyAvailable = true,
                averageCostPerUnit = 8.0
            ),
            
            // ===== WATER AND BASE =====
            Ingredient(
                name = "Spring Water",
                type = IngredientType.WATER,
                description = "Natural spring water with balanced minerals. Ideal for mead making.",
                category = "Natural Water",
                subcategory = "Spring",
                defaultUnit = "gallons",
                alternateUnits = "liters,quarts",
                typicalUsageMin = 4.0,
                typicalUsageMax = 6.0,
                applicableBeverages = "MEAD,WINE,BEER,CIDER,KOMBUCHA,SPECIALTY",
                preferredBeverages = "MEAD",
                flavorProfile = "Clean, mineral balance",
                intensity = 1,
                contributes = "Fermentation medium, mineral content",
                isCommonlyAvailable = true,
                averageCostPerUnit = 1.5
            ),
            
            // ===== CLARIFYING AGENTS =====
            Ingredient(
                name = "Bentonite",
                type = IngredientType.CLARIFIER,
                description = "Clay-based fining agent that removes proteins and aids in clearing mead.",
                category = "Clay Fining",
                subcategory = "Protein Removal",
                defaultUnit = "grams",
                alternateUnits = "teaspoons,tablespoons",
                typicalUsageMin = 2.0,
                typicalUsageMax = 5.0,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Neutral",
                intensity = 1,
                contributes = "Clarity, protein removal",
                isCommonlyAvailable = true,
                averageCostPerUnit = 3.0
            ),
            
            Ingredient(
                name = "Super Kleer KC",
                type = IngredientType.CLARIFIER,
                description = "Two-part fining system for crystal clear mead. Chitosan and kieselsol.",
                category = "Multi-Stage Fining",
                subcategory = "Complete System",
                defaultUnit = "packets",
                alternateUnits = "treatments",
                typicalUsageMin = 1.0,
                typicalUsageMax = 1.0,
                applicableBeverages = "MEAD,WINE",
                preferredBeverages = "MEAD",
                flavorProfile = "Neutral",
                intensity = 1,
                contributes = "Crystal clarity, haze removal",
                isCommonlyAvailable = true,
                averageCostPerUnit = 4.0
            ),
            
            // ===== SANITIZERS =====
            Ingredient(
                name = "Star San",
                type = IngredientType.SANITIZER,
                description = "No-rinse acid sanitizer. Industry standard for brewing equipment.",
                category = "Acid Sanitizer",
                subcategory = "No-Rinse",
                defaultUnit = "ounces",
                alternateUnits = "ml,tablespoons",
                typicalUsageMin = 1.0,
                typicalUsageMax = 2.0,
                applicableBeverages = "MEAD,WINE,BEER,CIDER,KOMBUCHA,SPECIALTY",
                preferredBeverages = "MEAD",
                flavorProfile = "Neutral (no-rinse)",
                intensity = 1,
                contributes = "Equipment sanitation",
                isCommonlyAvailable = true,
                averageCostPerUnit = 12.0
            ),
            
            // ===== SPICES FOR METHEGLINS =====
            Ingredient(
                name = "Ceylon Cinnamon",
                type = IngredientType.SPICES,
                description = "True cinnamon with sweet, delicate flavor. Perfect for metheglin.",
                category = "Sweet Spices",
                subcategory = "Bark Spices",
                defaultUnit = "grams",
                alternateUnits = "sticks,teaspoons",
                typicalUsageMin = 2.0,
                typicalUsageMax = 8.0,
                applicableBeverages = "MEAD,WINE,SPECIALTY",
                preferredBeverages = "MEAD",
                flavorProfile = "Sweet, warm, delicate cinnamon",
                intensity = 4,
                contributes = "Warm spice character, complexity",
                isCommonlyAvailable = true,
                averageCostPerUnit = 6.0
            ),
            
            Ingredient(
                name = "Madagascar Vanilla Beans",
                type = IngredientType.SPICES,
                description = "Premium vanilla beans with rich, creamy vanilla flavor. Excellent in mead.",
                category = "Sweet Spices",
                subcategory = "Vanilla",
                defaultUnit = "beans",
                alternateUnits = "grams",
                typicalUsageMin = 1.0,
                typicalUsageMax = 3.0,
                applicableBeverages = "MEAD,WINE,SPECIALTY",
                preferredBeverages = "MEAD",
                flavorProfile = "Rich, creamy, sweet vanilla",
                intensity = 4,
                contributes = "Vanilla character, sweetness perception",
                isCommonlyAvailable = true,
                averageCostPerUnit = 3.0
            ),
            
            Ingredient(
                name = "Whole Cloves",
                type = IngredientType.SPICES,
                description = "Aromatic spice with warm, sweet flavor. Use sparingly in metheglin.",
                category = "Warm Spices",
                subcategory = "Flower Buds",
                defaultUnit = "grams",
                alternateUnits = "pieces,teaspoons",
                typicalUsageMin = 0.5,
                typicalUsageMax = 2.0,
                applicableBeverages = "MEAD,WINE,SPECIALTY",
                preferredBeverages = "MEAD",
                flavorProfile = "Warm, sweet, aromatic, intense",
                intensity = 5,
                contributes = "Warm spice complexity",
                usageNotes = "Very potent - use sparingly",
                isCommonlyAvailable = true,
                averageCostPerUnit = 4.0
            )
        )
    }
}