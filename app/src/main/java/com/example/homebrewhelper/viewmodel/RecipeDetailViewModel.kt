package com.example.homebrewhelper.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homebrewhelper.data.database.entity.Recipe
import com.example.homebrewhelper.data.database.entity.RecipeIngredient
import com.example.homebrewhelper.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for viewing and managing recipe details
 * Handles recipe display, ingredient management, and calculations
 */
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])
    
    // UI State
    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()
    
    // Recipe data
    val recipe: StateFlow<Recipe?> = recipeRepository.observeRecipe(recipeId)
        .catch { error ->
            _uiState.update { it.copy(error = error.message) }
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Recipe ingredients
    val ingredients: StateFlow<List<RecipeIngredient>> = 
        recipeRepository.observeRecipeIngredients(recipeId)
            .catch { error ->
                _uiState.update { it.copy(error = error.message) }
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    // Recipe variations
    val variations: StateFlow<List<Recipe>> = recipe
        .filterNotNull()
        .flatMapLatest { currentRecipe ->
            val parentId = currentRecipe.parentRecipeId ?: currentRecipe.id
            recipeRepository.getRecipeVariations(parentId)
        }
        .catch { error ->
            _uiState.update { it.copy(error = error.message) }
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Calculated values
    val calculatedCost: StateFlow<Double?> = ingredients
        .map { ingredientList ->
            ingredientList.mapNotNull { it.estimatedCost }.sum().takeIf { it > 0 }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    init {
        // Load initial data if needed
        loadRecipeDetails()
    }
    
    // Actions
    fun toggleFavorite() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            recipeRepository.toggleFavorite(recipeId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Failed to update favorite: ${error.message}"
                        ) 
                    }
                }
        }
    }
    
    fun deleteRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            recipeRepository.deleteRecipe(recipeId)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = null,
                            shouldNavigateBack = true
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Failed to delete recipe: ${error.message}"
                        ) 
                    }
                }
        }
    }
    
    fun duplicateRecipe(newName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            recipeRepository.duplicateRecipe(recipeId, newName)
                .onSuccess { newRecipeId ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = null,
                            successMessage = "Recipe duplicated successfully",
                            navigationTarget = newRecipeId
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Failed to duplicate recipe: ${error.message}"
                        ) 
                    }
                }
        }
    }
    
    fun scaleRecipe(newBatchSize: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            recipeRepository.scaleRecipe(recipeId, newBatchSize)
                .onSuccess { scaledRecipeId ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = null,
                            successMessage = "Recipe scaled successfully",
                            navigationTarget = scaledRecipeId
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Failed to scale recipe: ${error.message}"
                        ) 
                    }
                }
        }
    }
    
    fun createVariation(changes: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val parentId = recipe.value?.parentRecipeId ?: recipeId
            recipeRepository.createRecipeVariation(parentId, changes)
                .onSuccess { variationId ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = null,
                            successMessage = "Recipe variation created",
                            navigationTarget = variationId
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Failed to create variation: ${error.message}"
                        ) 
                    }
                }
        }
    }
    
    fun updateActualCost(actualCost: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            recipeRepository.updateActualRecipeCost(recipeId, actualCost)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = null,
                            successMessage = "Cost updated successfully"
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Failed to update cost: ${error.message}"
                        ) 
                    }
                }
        }
    }
    
    fun removeIngredient(ingredientId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            recipeRepository.removeIngredientFromRecipe(ingredientId)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = null,
                            successMessage = "Ingredient removed"
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Failed to remove ingredient: ${error.message}"
                        ) 
                    }
                }
        }
    }
    
    fun showIngredientsByStep(step: String) {
        _uiState.update { it.copy(selectedStep = step) }
    }
    
    fun clearStepFilter() {
        _uiState.update { it.copy(selectedStep = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    fun clearNavigationTarget() {
        _uiState.update { it.copy(navigationTarget = null) }
    }
    
    fun clearNavigateBack() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }
    
    private fun loadRecipeDetails() {
        viewModelScope.launch {
            try {
                val cost = recipeRepository.calculateRecipeCost(recipeId)
                _uiState.update { it.copy(totalCost = cost) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to calculate cost: ${e.message}") }
            }
        }
    }
    
    // Helper functions for UI
    fun getIngredientsByStep(step: String): List<RecipeIngredient> {
        return ingredients.value.filter { it.step == step }
    }
    
    fun getAllSteps(): List<String> {
        return ingredients.value.map { it.step }.distinct().sorted()
    }
    
    fun getFilteredIngredients(): List<RecipeIngredient> {
        val selectedStep = uiState.value.selectedStep
        return if (selectedStep != null) {
            ingredients.value.filter { it.step == selectedStep }
        } else {
            ingredients.value
        }
    }
}

/**
 * UI state for the recipe detail screen
 */
data class RecipeDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val navigationTarget: String? = null,
    val shouldNavigateBack: Boolean = false,
    val selectedStep: String? = null,
    val totalCost: Double? = null
)