package com.example.homebrewhelper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homebrewhelper.data.database.entity.Recipe
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.repository.RecipeRepository
import com.example.homebrewhelper.data.repository.InitializationRepository
import com.example.homebrewhelper.data.repository.IngredientRepository
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
    private val recipeRepository: RecipeRepository,
    private val initializationRepository: InitializationRepository,
    private val ingredientRepository: IngredientRepository
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
    
    // Ingredient statistics for debugging
    private val _ingredientStats = MutableStateFlow<IngredientRepository.IngredientStats?>(null)
    val ingredientStats: StateFlow<IngredientRepository.IngredientStats?> = _ingredientStats.asStateFlow()
    
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
        android.util.Log.e("HomeBrewHelper", "Error in recipes flow", error)
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
            android.util.Log.e("HomeBrewHelper", "Error in recent recipes flow", error)
            _uiState.update { it.copy(error = error.message) }
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        android.util.Log.d("HomeBrewHelper", "RecipeListViewModel initialized")
        // Initialize app with mead ingredients and load stats
        initializeApp()
        loadRecipeStats()
        loadIngredientStats()
    }
    
    private fun initializeApp() {
        android.util.Log.d("HomeBrewHelper", "Starting app initialization...")
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    error = null,
                    initializationMessage = "Loading mead brewing ingredients..."
                ) 
            }
            
            try {
                val result = initializationRepository.initializeDefaultData()
                
                result.onSuccess {
                    android.util.Log.d("HomeBrewHelper", "Successfully initialized app data")
                    
                    // Load updated ingredient stats
                    loadIngredientStats()
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null,
                            initializationMessage = null,
                            successMessage = "Mead brewing ingredients loaded successfully!"
                        ) 
                    }
                }.onFailure { error ->
                    android.util.Log.e("HomeBrewHelper", "Failed to initialize app data", error)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load brewing ingredients. Try refreshing the ingredients.",
                            initializationMessage = null
                        ) 
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeBrewHelper", "Exception during initialization", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error loading brewing ingredients: ${e.message}",
                        initializationMessage = null
                    ) 
                }
            }
        }
    }
    
    private fun loadIngredientStats() {
        viewModelScope.launch {
            try {
                val stats = ingredientRepository.getIngredientStats()
                _ingredientStats.value = stats
                android.util.Log.d("HomeBrewHelper", "Loaded ingredient stats: ${stats.totalIngredients} total ingredients")
                
                // Update UI state with ingredient info
                _uiState.update { currentState ->
                    currentState.copy(
                        ingredientCount = stats.totalIngredients,
                        hasIngredients = stats.totalIngredients > 0
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeBrewHelper", "Failed to load ingredient stats", e)
            }
        }
    }
    
    // Manual refresh function for testing/troubleshooting
    fun forceInitialization() {
        android.util.Log.d("HomeBrewHelper", "Force initialization requested by user")
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    error = null,
                    initializationMessage = "Refreshing mead brewing ingredients..."
                ) 
            }
            
            try {
                val result = initializationRepository.forceInitialization()
                
                result.onSuccess {
                    android.util.Log.d("HomeBrewHelper", "Force initialization successful")
                    
                    // Reload ingredient stats
                    loadIngredientStats()
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null,
                            initializationMessage = null,
                            successMessage = "Mead brewing ingredients refreshed successfully!"
                        ) 
                    }
                }.onFailure { error ->
                    android.util.Log.e("HomeBrewHelper", "Force initialization failed", error)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to refresh ingredients: ${error.message}",
                            initializationMessage = null
                        ) 
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeBrewHelper", "Exception during force initialization", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error refreshing ingredients: ${e.message}",
                        initializationMessage = null
                    ) 
                }
            }
        }
    }
    
    // Check ingredient status
    fun checkIngredientStatus() {
        android.util.Log.d("HomeBrewHelper", "Checking ingredient status...")
        loadIngredientStats()
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
                android.util.Log.e("HomeBrewHelper", "Failed to load recipe stats", e)
                _uiState.update { it.copy(error = "Failed to load statistics: ${e.message}") }
            }
        }
    }
    
    fun refreshStats() {
        loadRecipeStats()
        loadIngredientStats()
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
    val recipeStats: RecipeRepository.RecipeStats? = null,
    val initializationMessage: String? = null, // Message during ingredient loading
    val ingredientCount: Int = 0,
    val hasIngredients: Boolean = false
)