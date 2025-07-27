package com.example.homebrewhelper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homebrewhelper.data.database.entity.Recipe
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the recipe list screen
 * Handles filtering, searching, and recipe operations
 */
@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()
    
    // Search and filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedBeverageType = MutableStateFlow<BeverageType?>(null)
    val selectedBeverageType: StateFlow<BeverageType?> = _selectedBeverageType.asStateFlow()
    
    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()
    
    // Combined recipes flow based on filters
    val recipes: StateFlow<List<Recipe>> = combine(
        searchQuery,
        selectedBeverageType,
        showFavoritesOnly
    ) { query, beverageType, favoritesOnly ->
        Triple(query, beverageType, favoritesOnly)
    }.flatMapLatest { (query, beverageType, favoritesOnly) ->
        when {
            favoritesOnly -> recipeRepository.getFavoriteRecipes()
            query.isNotBlank() && beverageType != null -> {
                recipeRepository.searchRecipes(query)
                    .map { recipes -> recipes.filter { it.beverageType == beverageType } }
            }
            query.isNotBlank() -> recipeRepository.searchRecipes(query)
            beverageType != null -> recipeRepository.getRecipesByType(beverageType)
            else -> recipeRepository.observeAllRecipes()
        }
    }.catch { error ->
        _uiState.update { it.copy(error = error.message) }
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Recent recipes for quick access
    val recentRecipes: StateFlow<List<Recipe>> = recipeRepository.getRecentRecipes(5)
        .catch { error ->
            _uiState.update { it.copy(error = error.message) }
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        loadRecipeStats()
    }
    
    // Actions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectBeverageType(beverageType: BeverageType?) {
        _selectedBeverageType.value = beverageType
    }
    
    fun toggleFavoritesFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }
    
    fun toggleFavorite(recipeId: String) {
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
    
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            recipeRepository.deleteRecipe(recipeId)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = null,
                            successMessage = "Recipe deleted successfully"
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
    
    fun duplicateRecipe(recipeId: String, newName: String) {
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
    
    fun scaleRecipe(recipeId: String, newBatchSize: Double) {
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
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    fun clearNavigationTarget() {
        _uiState.update { it.copy(navigationTarget = null) }
    }
    
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedBeverageType.value = null
        _showFavoritesOnly.value = false
    }
    
    private fun loadRecipeStats() {
        viewModelScope.launch {
            try {
                val stats = recipeRepository.getRecipeStats()
                _uiState.update { it.copy(recipeStats = stats) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load statistics: ${e.message}") }
            }
        }
    }
    
    fun refreshStats() {
        loadRecipeStats()
    }
}

/**
 * UI state for the recipe list screen
 */
data class RecipeListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val navigationTarget: String? = null, // Recipe ID to navigate to
    val recipeStats: RecipeRepository.RecipeStats? = null
)