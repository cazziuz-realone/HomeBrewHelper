package com.example.homebrewhelper.data.database.converter

import androidx.room.TypeConverter
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.model.IngredientType
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room type converters for complex data types
 * Handles serialization/deserialization of enums, dates, and JSON objects
 */
class Converters {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
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
    
    // List<String> converters for tags, alternate units, etc.
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<String>>(value)
            } catch (e: Exception) {
                // Fallback for malformed JSON - split by comma
                value.split(",").map { it.trim() }
            }
        }
    }
    
    // Map<String, Any> converters for complex JSON objects
    @TypeConverter
    fun fromMap(value: Map<String, Any>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toMap(value: String): Map<String, Any> {
        return if (value.isEmpty()) {
            emptyMap()
        } else {
            try {
                json.decodeFromString<Map<String, Any>>(value)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
    
    // Double range converters (for things like ABV ranges)
    @TypeConverter
    fun fromDoubleRange(range: DoubleRange): String {
        return "${range.start},${range.endInclusive}"
    }
    
    @TypeConverter
    fun toDoubleRange(value: String): DoubleRange {
        return try {
            val parts = value.split(",")
            if (parts.size == 2) {
                parts[0].toDouble()..parts[1].toDouble()
            } else {
                0.0..0.0
            }
        } catch (e: Exception) {
            0.0..0.0
        }
    }
}