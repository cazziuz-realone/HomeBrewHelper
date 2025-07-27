package com.example.homebrewhelper.data.model

/**
 * Types of ingredients used in brewing, with beverage-specific categories
 */
enum class IngredientType(
    val displayName: String,
    val applicableBeverages: List<BeverageType>,
    val isRequired: Boolean = false,
    val defaultUnit: String,
    val alternateUnits: List<String> = emptyList()
) {
    // Universal ingredients
    WATER(
        displayName = "Water",
        applicableBeverages = BeverageType.values().toList(),
        isRequired = true,
        defaultUnit = "gallons",
        alternateUnits = listOf("liters", "quarts")
    ),
    YEAST(
        displayName = "Yeast",
        applicableBeverages = listOf(BeverageType.BEER, BeverageType.WINE, BeverageType.MEAD, BeverageType.CIDER),
        isRequired = true,
        defaultUnit = "packets",
        alternateUnits = listOf("grams", "ounces")
    ),
    
    // Beer-specific ingredients
    GRAIN(
        displayName = "Grain/Malt",
        applicableBeverages = listOf(BeverageType.BEER),
        isRequired = true,
        defaultUnit = "pounds",
        alternateUnits = listOf("kilograms", "ounces")
    ),
    HOPS(
        displayName = "Hops",
        applicableBeverages = listOf(BeverageType.BEER),
        defaultUnit = "ounces",
        alternateUnits = listOf("grams")
    ),
    
    // Wine-specific ingredients
    GRAPES(
        displayName = "Grapes/Fruit",
        applicableBeverages = listOf(BeverageType.WINE),
        isRequired = true,
        defaultUnit = "pounds",
        alternateUnits = listOf("kilograms")
    ),
    ACID(
        displayName = "Acid",
        applicableBeverages = listOf(BeverageType.WINE, BeverageType.CIDER, BeverageType.MEAD),
        defaultUnit = "teaspoons",
        alternateUnits = listOf("grams", "ounces")
    ),
    SULFITES(
        displayName = "Sulfites",
        applicableBeverages = listOf(BeverageType.WINE),
        defaultUnit = "teaspoons",
        alternateUnits = listOf("grams")
    ),
    
    // Mead-specific ingredients
    HONEY(
        displayName = "Honey",
        applicableBeverages = listOf(BeverageType.MEAD),
        isRequired = true,
        defaultUnit = "pounds",
        alternateUnits = listOf("kilograms", "ounces")
    ),
    NUTRIENTS(
        displayName = "Yeast Nutrients",
        applicableBeverages = listOf(BeverageType.MEAD, BeverageType.WINE),
        defaultUnit = "teaspoons",
        alternateUnits = listOf("grams")
    ),
    
    // Cider-specific ingredients
    APPLE_JUICE(
        displayName = "Apple Juice/Cider",
        applicableBeverages = listOf(BeverageType.CIDER),
        isRequired = true,
        defaultUnit = "gallons",
        alternateUnits = listOf("liters")
    ),
    TANNINS(
        displayName = "Tannins",
        applicableBeverages = listOf(BeverageType.CIDER, BeverageType.WINE),
        defaultUnit = "teaspoons",
        alternateUnits = listOf("grams")
    ),
    
    // Kombucha-specific ingredients
    TEA(
        displayName = "Tea",
        applicableBeverages = listOf(BeverageType.KOMBUCHA),
        isRequired = true,
        defaultUnit = "tea bags",
        alternateUnits = listOf("tablespoons", "grams")
    ),
    SUGAR(
        displayName = "Sugar",
        applicableBeverages = listOf(BeverageType.KOMBUCHA, BeverageType.WINE, BeverageType.CIDER),
        defaultUnit = "cups",
        alternateUnits = listOf("pounds", "grams")
    ),
    SCOBY(
        displayName = "SCOBY",
        applicableBeverages = listOf(BeverageType.KOMBUCHA),
        isRequired = true,
        defaultUnit = "pieces"
    ),
    STARTER_TEA(
        displayName = "Starter Tea",
        applicableBeverages = listOf(BeverageType.KOMBUCHA),
        isRequired = true,
        defaultUnit = "cups",
        alternateUnits = listOf("ounces")
    ),
    
    // Common additives
    SPICES(
        displayName = "Spices/Flavorings",
        applicableBeverages = BeverageType.values().toList(),
        defaultUnit = "teaspoons",
        alternateUnits = listOf("tablespoons", "ounces", "grams")
    ),
    CLARIFIER(
        displayName = "Clarifier/Fining Agent",
        applicableBeverages = listOf(BeverageType.BEER, BeverageType.WINE, BeverageType.MEAD, BeverageType.CIDER),
        defaultUnit = "teaspoons",
        alternateUnits = listOf("grams", "ounces")
    ),
    SANITIZER(
        displayName = "Sanitizer",
        applicableBeverages = BeverageType.values().toList(),
        defaultUnit = "ounces",
        alternateUnits = listOf("tablespoons")
    ),
    OTHER(
        displayName = "Other",
        applicableBeverages = BeverageType.values().toList(),
        defaultUnit = "units",
        alternateUnits = listOf("grams", "ounces", "pounds", "teaspoons", "tablespoons")
    );

    companion object {
        /**
         * Get ingredient types applicable to a specific beverage
         */
        fun getForBeverage(beverageType: BeverageType): List<IngredientType> {
            return values().filter { it.applicableBeverages.contains(beverageType) }
        }

        /**
         * Get required ingredients for a beverage type
         */
        fun getRequiredForBeverage(beverageType: BeverageType): List<IngredientType> {
            return getForBeverage(beverageType).filter { it.isRequired }
        }

        /**
         * Get optional ingredients for a beverage type
         */
        fun getOptionalForBeverage(beverageType: BeverageType): List<IngredientType> {
            return getForBeverage(beverageType).filter { !it.isRequired }
        }
    }
}