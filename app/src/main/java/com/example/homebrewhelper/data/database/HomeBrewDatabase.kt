package com.example.homebrewhelper.data.database

import androidx.room.*
import com.example.homebrewhelper.data.database.converter.Converters
import com.example.homebrewhelper.data.database.dao.IngredientDao
import com.example.homebrewhelper.data.database.dao.RecipeDao
import com.example.homebrewhelper.data.database.dao.RecipeIngredientDao
import com.example.homebrewhelper.data.database.entity.Ingredient
import com.example.homebrewhelper.data.database.entity.Recipe
import com.example.homebrewhelper.data.database.entity.RecipeIngredient

/**
 * Room database for HomeBrewHelper application
 * Central database containing all brewing-related data
 */
@Database(
    entities = [
        Recipe::class,
        Ingredient::class,
        RecipeIngredient::class
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(Converters::class)
abstract class HomeBrewDatabase : RoomDatabase() {
    
    // DAOs
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    
    companion object {
        const val DATABASE_NAME = "homebrew_database"
        const val DATABASE_VERSION = 1
        
        /**
         * Pre-populate callback to insert default ingredients
         */
        fun createCallback(): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Database will be populated via repository on first launch
                }
            }
        }
    }
}