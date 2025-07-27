package com.example.homebrewhelper.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.model.IngredientType
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Room entity representing an ingredient in the database
 * Can be used across multiple recipes and beverage types
 */
@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Basic ingredient information
    val name: String,
    val type: IngredientType,
    val description: String = "",
    
    // Classification
    val category: String = "", // Specific category within type (e.g., "Pale Ale Malt" for grain)
    val subcategory: String = "", // Even more specific (e.g., "2-Row" for malt)
    val brand: String = "",
    val supplier: String = "",
    
    // Composition data
    val alphaAcid: Double? = null, // For hops
    val lovibond: Double? = null, // Color rating for grains
    val potential: Double? = null, // Extract potential for fermentables
    val moisture: Double? = null, // Moisture content percentage
    val protein: Double? = null, // Protein content percentage
    
    // Usage guidelines
    val defaultUnit: String,
    val alternateUnits: String = "", // JSON array of alternate units
    val typicalUsageMin: Double? = null,
    val typicalUsageMax: Double? = null,
    val usageNotes: String = "",
    
    // Beverage compatibility
    val applicableBeverages: String, // JSON array of BeverageType
    val preferredBeverages: String = "", // JSON array of most suitable beverages
    
    // Flavor profile
    val flavorProfile: String = "", // JSON object with flavor descriptors
    val intensity: Int = 3, // 1-5 scale for flavor intensity
    val contributes: String = "", // What this ingredient contributes (flavor, color, body, etc.)
    
    // Substitutions
    val substitutes: String = "", // JSON array of ingredient IDs that can substitute
    val substitutionRatio: Double = 1.0, // Default 1:1 substitution ratio
    
    // Processing information
    val requiresProcessing: Boolean = false, // Needs mashing, crushing, etc.
    val processingNotes: String = "",
    val storageNotes: String = "",
    val shelfLifeDays: Int? = null,
    
    // Cost and availability
    val averageCostPerUnit: Double? = null,
    val isCommonlyAvailable: Boolean = true,
    val seasonality: String = "", // When ingredient is typically available
    
    // Quality indicators
    val qualityGrade: String = "", // Premium, standard, basic, etc.
    val origin: String = "", // Geographic origin
    val harvestYear: Int? = null, // For hops, grapes, etc.
    
    // System fields
    val isCustom: Boolean = false, // User-created vs system ingredient
    val createdAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val updatedAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val isDeleted: Boolean = false
) {
    /**
     * Check if ingredient is suitable for a specific beverage type
     */
    fun isSuitableFor(beverageType: BeverageType): Boolean {
        // Parse applicableBeverages JSON and check
        return type.applicableBeverages.contains(beverageType)
    }
    
    /**
     * Get formatted usage range string
     */
    fun getUsageRange(): String? {
        return if (typicalUsageMin != null && typicalUsageMax != null) {
            "$typicalUsageMin - $typicalUsageMax $defaultUnit"
        } else null
    }
    
    /**
     * Get intensity description
     */
    fun getIntensityDescription(): String {
        return when (intensity) {
            1 -> "Very Mild"
            2 -> "Mild"
            3 -> "Moderate"
            4 -> "Strong"
            5 -> "Very Strong"
            else -> "Unknown"
        }
    }
    
    /**
     * Check if ingredient is fresh based on shelf life
     */
    fun isFresh(): Boolean? {
        return shelfLifeDays?.let { shelfLife ->
            val daysSinceCreated = (System.currentTimeMillis() - createdAt.toEpochMilliseconds()) / (24 * 60 * 60 * 1000)
            daysSinceCreated < shelfLife
        }
    }
    
    /**
     * Get cost per typical usage amount
     */
    fun getCostPerUsage(): Double? {
        return if (averageCostPerUnit != null && typicalUsageMin != null) {
            averageCostPerUnit * typicalUsageMin
        } else null
    }
}