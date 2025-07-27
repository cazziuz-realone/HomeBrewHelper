package com.example.homebrewhelper.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity representing the many-to-many relationship between recipes and ingredients
 * Includes usage-specific information like quantity, timing, and processing notes
 */
@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["ingredientId"]),
        Index(value = ["recipeId", "step", "additionTime"])
    ]
)
data class RecipeIngredient(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Foreign keys
    val recipeId: String,
    val ingredientId: String,
    
    // Quantity information
    val quantity: Double,
    val unit: String,
    val quantityNotes: String = "", // "heaping", "packed", "level", etc.
    
    // Process timing
    val step: String, // "mash", "boil", "fermentation", "bottling", etc.
    val additionTime: Int = 0, // Minutes from start of step (0 = beginning)
    val duration: Int? = null, // How long to keep ingredient in process (minutes)
    
    // Processing instructions
    val processingMethod: String = "", // "crushed", "whole", "extract", etc.
    val temperature: Double? = null, // Temperature for addition (Fahrenheit)
    val instructions: String = "",
    
    // Contribution calculations
    val gravityContribution: Double? = null, // Points contribution to OG
    val colorContribution: Double? = null, // SRM contribution
    val bitternessContribution: Double? = null, // IBU contribution for hops
    val alcoholContribution: Double? = null, // Expected alcohol contribution
    
    // Usage metadata
    val isOptional: Boolean = false,
    val isSubstitution: Boolean = false, // If this replaces another ingredient
    val originalIngredientId: String? = null, // ID of ingredient being substituted
    val substitutionRatio: Double = 1.0, // Ratio used for substitution
    val substitutionNotes: String = "",
    
    // Timing groups for complex schedules
    val timingGroup: String = "", // Group related additions ("hop burst", "nutrient schedule")
    val priority: Int = 0, // Order within timing group
    
    // Quality assurance
    val qualityRequirements: String = "", // Special quality notes for this usage
    val criticalTiming: Boolean = false, // If timing is critical for this addition
    
    // Cost tracking
    val estimatedCost: Double? = null,
    val actualCost: Double? = null,
    
    // Notes and observations
    val notes: String = "",
    val lastUsedNotes: String = "" // Notes from last time this combination was used
) {
    /**
     * Get formatted addition timing
     */
    fun getFormattedTiming(): String {
        return when {
            additionTime == 0 -> "At start of $step"
            duration != null -> "$additionTime min into $step for $duration min"
            else -> "$additionTime min into $step"
        }
    }
    
    /**
     * Check if this is a late addition (last 25% of step)
     */
    fun isLateAddition(stepDuration: Int): Boolean {
        return additionTime > (stepDuration * 0.75)
    }
    
    /**
     * Get priority description
     */
    fun getPriorityDescription(): String {
        return when (priority) {
            0 -> "Standard"
            in 1..3 -> "High Priority"
            in -3..-1 -> "Low Priority"
            else -> "Custom Priority: $priority"
        }
    }
    
    /**
     * Calculate cost efficiency (cost per contribution)
     */
    fun getCostEfficiency(): Double? {
        val cost = estimatedCost ?: actualCost
        val contribution = gravityContribution ?: bitternessContribution ?: alcoholContribution
        
        return if (cost != null && contribution != null && contribution > 0) {
            cost / contribution
        } else null
    }
    
    /**
     * Check if ingredient addition is time-sensitive
     */
    fun isTimeSensitive(): Boolean {
        return criticalTiming || step in listOf("boil", "fermentation", "carbonation")
    }
    
    /**
     * Get step category for grouping
     */
    fun getStepCategory(): String {
        return when (step.lowercase()) {
            "mash", "sparge" -> "Mashing"
            "boil", "whirlpool" -> "Boiling"
            "fermentation", "primary", "secondary" -> "Fermentation"
            "conditioning", "aging", "bulk aging" -> "Aging"
            "bottling", "kegging", "packaging" -> "Packaging"
            "crushing", "pressing" -> "Processing"
            else -> "Other"
        }
    }
}