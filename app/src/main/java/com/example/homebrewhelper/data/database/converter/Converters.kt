package com.example.homebrewhelper.data.database.converter

import androidx.room.TypeConverter
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.model.IngredientType
import kotlinx.datetime.Instant

/**
 * Room type converters for complex data types
 * Handles serialization/deserialization of enums, dates, and simple collections
 */
class Converters {
    
    // BeverageType converters
    @TypeConverter
    fun fromBeverageType(beverageType: BeverageType): String {
        return beverageType.name
    }
    
    @TypeConverter
    fun toBeverageType(beverageType: String): BeverageType {
        return BeverageType.valueOf(beverageType)
    }
    
    // IngredientType converters
    @TypeConverter
    fun fromIngredientType(ingredientType: IngredientType): String {
        return ingredientType.name
    }
    
    @TypeConverter
    fun toIngredientType(ingredientType: String): IngredientType {
        return IngredientType.valueOf(ingredientType)
    }
    
    // Instant (kotlinx.datetime) converters
    @TypeConverter
    fun fromInstant(instant: Instant): Long {
        return instant.toEpochMilliseconds()
    }
    
    @TypeConverter
    fun toInstant(epochMillis: Long): Instant {
        return Instant.fromEpochMilliseconds(epochMillis)
    }
}