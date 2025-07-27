package com.example.homebrewhelper.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enumeration of supported beverage types in HomeBrewHelper
 * Each type has specific characteristics for ingredients, processes, and calculations
 */
enum class BeverageType(
    val displayName: String,
    val icon: ImageVector,
    val description: String,
    val primaryIngredients: List<String>,
    val typicalAbvMin: Double,
    val typicalAbvMax: Double
) {
    BEER(
        displayName = "Beer",
        icon = Icons.Default.LocalBar,
        description = "Fermented beverage made from malted cereal grains",
        primaryIngredients = listOf("Malt", "Hops", "Yeast", "Water"),
        typicalAbvMin = 3.0,
        typicalAbvMax = 12.0
    ),
    WINE(
        displayName = "Wine",
        icon = Icons.Default.WineBar,
        description = "Fermented beverage made from grapes or other fruits",
        primaryIngredients = listOf("Grapes", "Yeast", "Acid", "Sulfites"),
        typicalAbvMin = 8.0,
        typicalAbvMax = 16.0
    ),
    MEAD(
        displayName = "Mead",
        icon = Icons.Default.LocalBar,
        description = "Fermented beverage made from honey and water",
        primaryIngredients = listOf("Honey", "Water", "Yeast", "Nutrients"),
        typicalAbvMin = 8.0,
        typicalAbvMax = 18.0
    ),
    CIDER(
        displayName = "Cider",
        icon = Icons.Default.LocalBar,
        description = "Fermented beverage made from apple juice",
        primaryIngredients = listOf("Apple Juice", "Yeast", "Acid", "Tannins"),
        typicalAbvMin = 4.0,
        typicalAbvMax = 12.0
    ),
    KOMBUCHA(
        displayName = "Kombucha",
        icon = Icons.Default.LocalBar,
        description = "Fermented tea beverage with SCOBY culture",
        primaryIngredients = listOf("Tea", "Sugar", "SCOBY", "Starter Tea"),
        typicalAbvMin = 0.0,
        typicalAbvMax = 3.0
    ),
    SPECIALTY(
        displayName = "Specialty",
        icon = Icons.Default.LocalBar,
        description = "Other fermented beverages and experimental brews",
        primaryIngredients = listOf("Various"),
        typicalAbvMin = 0.0,
        typicalAbvMax = 20.0
    );

    /**
     * Get typical ABV range as a formatted string
     */
    fun getAbvRangeString(): String {
        return "${typicalAbvMin}% - ${typicalAbvMax}%"
    }

    /**
     * Check if ABV is within typical range for this beverage type
     */
    fun isAbvInRange(abv: Double): Boolean {
        return abv >= typicalAbvMin && abv <= typicalAbvMax
    }

    companion object {
        /**
         * Get beverage type by name, case-insensitive
         */
        fun fromString(name: String): BeverageType? {
            return values().find { it.name.equals(name, ignoreCase = true) }
        }

        /**
         * Get all beverage types suitable for beginners
         */
        fun getBeginnerFriendly(): List<BeverageType> {
            return listOf(BEER, CIDER, MEAD)
        }

        /**
         * Get all alcoholic beverage types
         */
        fun getAlcoholic(): List<BeverageType> {
            return values().filter { it.typicalAbvMin > 0.5 }
        }
    }
}