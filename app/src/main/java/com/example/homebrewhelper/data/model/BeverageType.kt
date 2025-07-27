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
    val typicalAbv: DoubleRange
) {
    BEER(
        displayName = "Beer",
        icon = Icons.Default.LocalBar,
        description = "Fermented beverage made from malted cereal grains",
        primaryIngredients = listOf("Malt", "Hops", "Yeast", "Water"),
        typicalAbv = 3.0..12.0
    ),
    WINE(
        displayName = "Wine",
        icon = Icons.Default.WineBar,
        description = "Fermented beverage made from grapes or other fruits",
        primaryIngredients = listOf("Grapes", "Yeast", "Acid", "Sulfites"),
        typicalAbv = 8.0..16.0
    ),
    MEAD(
        displayName = "Mead",
        icon = Icons.Default.LocalBar,
        description = "Fermented beverage made from honey and water",
        primaryIngredients = listOf("Honey", "Water", "Yeast", "Nutrients"),
        typicalAbv = 8.0..18.0
    ),
    CIDER(
        displayName = "Cider",
        icon = Icons.Default.LocalBar,
        description = "Fermented beverage made from apple juice",
        primaryIngredients = listOf("Apple Juice", "Yeast", "Acid", "Tannins"),
        typicalAbv = 4.0..12.0
    ),
    KOMBUCHA(
        displayName = "Kombucha",
        icon = Icons.Default.LocalBar,
        description = "Fermented tea beverage with SCOBY culture",
        primaryIngredients = listOf("Tea", "Sugar", "SCOBY", "Starter Tea"),
        typicalAbv = 0.0..3.0
    ),
    SPECIALTY(
        displayName = "Specialty",
        icon = Icons.Default.LocalBar,
        description = "Other fermented beverages and experimental brews",
        primaryIngredients = listOf("Various"),
        typicalAbv = 0.0..20.0
    );

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
            return values().filter { it.typicalAbv.start > 0.5 }
        }
    }
}