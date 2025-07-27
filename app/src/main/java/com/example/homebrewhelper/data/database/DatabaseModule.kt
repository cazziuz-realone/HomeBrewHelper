package com.example.homebrewhelper.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideHomeBrewDatabase(
        @ApplicationContext context: Context
    ): HomeBrewDatabase {
        return Room.databaseBuilder(
            context,
            HomeBrewDatabase::class.java,
            HomeBrewDatabase.DATABASE_NAME
        )
        .addCallback(HomeBrewDatabase.createCallback())
        .fallbackToDestructiveMigration() // For development only
        .build()
    }
    
    @Provides
    fun provideRecipeDao(database: HomeBrewDatabase) = database.recipeDao()
    
    @Provides
    fun provideIngredientDao(database: HomeBrewDatabase) = database.ingredientDao()
    
    @Provides
    fun provideRecipeIngredientDao(database: HomeBrewDatabase) = database.recipeIngredientDao()
}