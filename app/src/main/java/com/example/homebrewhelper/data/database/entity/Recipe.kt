package com.example.homebrewhelper.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.homebrewhelper.data.model.BeverageType
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Room entity representing a brewing recipe
 * Supports all beverage types with flexible schema
 */
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Basic recipe information
    val name: String,
    val description: String = "",
    val beverageType: BeverageType,
    
    // Recipe metadata
    val author: String = "",
    val source: String = "", // Book, website, personal, etc.
    val difficulty: Int = 1, // 1-5 scale
    val tags: String = "", // JSON array of tags as string
    
    // Batch information
    val batchSizeGallons: Double,
    val originalGravity: Double? = null,
    val finalGravity: Double? = null,
    val estimatedAbv: Double? = null,
    val estimatedIbu: Double? = null, // For beer
    val estimatedSrm: Double? = null, // Color for beer
    
    // Timing information
    val brewingTimeMinutes: Int? = null,
    val fermentationDays: Int? = null,
    val conditioningDays: Int? = null,
    val totalTimelineDays: Int? = null,
    
    // Process notes
    val brewingInstructions: String = "",
    val fermentationNotes: String = "",
    val notes: String = "",
    
    // Version control
    val version: Int = 1,
    val parentRecipeId: String? = null, // For recipe variations
    
    // Beverage-specific JSON data
    val beerSpecific: String? = null, // JSON for hop schedule, mash schedule, etc.
    val wineSpecific: String? = null, // JSON for grape varieties, MLF info, etc.
    val meadSpecific: String? = null, // JSON for honey types, nutrient schedule, etc.
    val ciderSpecific: String? = null, // JSON for apple varieties, acid blend, etc.
    val kombuchaSpecific: String? = null, // JSON for tea types, SCOBY info, etc.
    
    // System fields
    val createdAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val updatedAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val isDeleted: Boolean = false,
    val isFavorite: Boolean = false
) {
    /**
     * Calculate estimated ABV from gravity readings
     */
    fun calculateAbv(): Double? {
        return if (originalGravity != null && finalGravity != null) {
            (originalGravity - finalGravity) * 131.25
        } else {
            estimatedAbv
        }
    }
    
    /**
     * Get formatted difficulty string
     */
    fun getDifficultyString(): String {
        return when (difficulty) {
            1 -> "Beginner"
            2 -> "Easy"
            3 -> "Intermediate"
            4 -> "Advanced"
            5 -> "Expert"
            else -> "Unknown"
        }
    }
    
    /**
     * Check if recipe is complete (has required fields)
     */
    fun isComplete(): Boolean {
        return name.isNotBlank() && batchSizeGallons > 0
    }
    
    /**
     * Get estimated timeline for completion
     */
    fun getEstimatedCompletionDays(): Int {
        return totalTimelineDays ?: run {
            val brewing = (brewingTimeMinutes ?: 0) / (24 * 60) // Convert to days
            val fermentation = fermentationDays ?: when (beverageType) {
                BeverageType.BEER -> 14
                BeverageType.WINE -> 60
                BeverageType.MEAD -> 90
                BeverageType.CIDER -> 30
                BeverageType.KOMBUCHA -> 7
                BeverageType.SPECIALTY -> 30
            }
            val conditioning = conditioningDays ?: when (beverageType) {
                BeverageType.BEER -> 14
                BeverageType.WINE -> 30
                BeverageType.MEAD -> 60
                BeverageType.CIDER -> 14
                BeverageType.KOMBUCHA -> 3
                BeverageType.SPECIALTY -> 14
            }
            brewing + fermentation + conditioning
        }
    }
}