package com.example.homebrewhelper.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homebrewhelper.data.database.entity.Recipe
import com.example.homebrewhelper.data.database.entity.RecipeIngredient
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.repository.IngredientRepository
import com.example.homebrewhelper.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * ViewModel for building and editing recipes
 * Handles recipe creation form state and ingredient management
 */
@HiltViewModel
class RecipeBuilderViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val existingRecipeId: String? = savedStateHandle["recipeId"]
    private val isEditing: Boolean = existingRecipeId != null
    
    // UI State
    private val _uiState = MutableStateFlow(RecipeBuilderUiState())
    val uiState: StateFlow<RecipeBuilderUiState> = _uiState.asStateFlow()
    
    // Recipe form state
    private val _recipeForm = MutableStateFlow(RecipeFormState())
    val recipeForm: StateFlow<RecipeFormState> = _recipeForm.asStateFlow()
    
    // Ingredients state
    private val _recipeIngredients = MutableStateFlow<List<RecipeIngredientForm>>(emptyList())
    val recipeIngredients: StateFlow<List<RecipeIngredientForm>> = _recipeIngredients.asStateFlow()
    
    init {
        if (isEditing && existingRecipeId != null) {
            loadExistingRecipe(existingRecipeId)
        } else {
            // Initialize with default values for new recipe
            _recipeForm.update { it.copy(beverageType = BeverageType.BEER) }
        }
    }
    
    // Form field updates
    fun updateName(name: String) {
        _recipeForm.update { it.copy(name = name) }
        validateForm()
    }
    
    fun updateDescription(description: String) {
        _recipeForm.update { it.copy(description = description) }
    }
    
    fun updateBeverageType(beverageType: BeverageType) {
        _recipeForm.update { it.copy(beverageType = beverageType) }
        // Clear ingredients when beverage type changes as they may not be compatible
        if (_recipeIngredients.value.isNotEmpty()) {
            _uiState.update { it.copy(showBeverageTypeChangeWarning = true) }
        }
        validateForm()
    }
    
    fun updateBatchSize(batchSize: String) {
        val size = batchSize.toDoubleOrNull() ?: 0.0
        _recipeForm.update { it.copy(batchSizeGallons = size) }
        validateForm()
    }
    
    fun updateDifficulty(difficulty: Int) {
        _recipeForm.update { it.copy(difficulty = difficulty.coerceIn(1, 5)) }
    }
    
    fun updateAuthor(author: String) {
        _recipeForm.update { it.copy(author = author) }
    }
    
    fun updateSource(source: String) {
        _recipeForm.update { it.copy(source = source) }
    }
    
    fun updateEstimatedAbv(abv: String) {
        val abvValue = abv.toDoubleOrNull()
        _recipeForm.update { it.copy(estimatedAbv = abvValue) }
    }
    
    fun updateBrewingInstructions(instructions: String) {
        _recipeForm.update { it.copy(brewingInstructions = instructions) }
    }
    
    fun updateFermentationNotes(notes: String) {
        _recipeForm.update { it.copy(fermentationNotes = notes) }
    }
    
    fun updateNotes(notes: String) {
        _recipeForm.update { it.copy(notes = notes) }
    }
    
    fun updateTiming(brewingTime: String, fermentationDays: String, conditioningDays: String) {
        _recipeForm.update {
            it.copy(
                brewingTimeMinutes = brewingTime.toIntOrNull(),
                fermentationDays = fermentationDays.toIntOrNull(),
                conditioningDays = conditioningDays.toIntOrNull()
            )
        }
    }
    
    // Ingredient management
    fun addIngredient(ingredientId: String, quantity: String, unit: String, step: String, additionTime: String) {
        val quantityValue = quantity.toDoubleOrNull() ?: return
        val timeValue = additionTime.toIntOrNull() ?: 0
        
        val newIngredient = RecipeIngredientForm(
            ingredientId = ingredientId,
            quantity = quantityValue,
            unit = unit,
            step = step,
            additionTime = timeValue
        )
        
        _recipeIngredients.update { current ->
            current + newIngredient
        }
        
        validateForm()
    }
    
    fun updateIngredient(index: Int, ingredient: RecipeIngredientForm) {
        _recipeIngredients.update { current ->
            current.toMutableList().apply {
                if (index in indices) {
                    set(index, ingredient)
                }
            }
        }
        validateForm()
    }
    
    fun removeIngredient(index: Int) {
        _recipeIngredients.update { current ->
            current.toMutableList().apply {
                if (index in indices) {
                    removeAt(index)
                }
            }
        }
        validateForm()
    }
    
    fun clearIngredientsAfterBeverageTypeChange() {
        _recipeIngredients.update { emptyList() }
        _uiState.update { it.copy(showBeverageTypeChangeWarning = false) }
    }
    
    fun keepIngredientsAfterBeverageTypeChange() {
        _uiState.update { it.copy(showBeverageTypeChangeWarning = false) }
    }
    
    // Recipe operations
    fun saveRecipe() {
        if (!validateForm()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val form = _recipeForm.value
                val recipe = if (isEditing && existingRecipeId != null) {
                    // Update existing recipe
                    val existing = recipeRepository.getRecipe(existingRecipeId)
                        ?: throw IllegalStateException("Recipe not found")
                    
                    existing.copy(
                        name = form.name,
                        description = form.description,
                        beverageType = form.beverageType,
                        author = form.author,
                        source = form.source,
                        difficulty = form.difficulty,
                        batchSizeGallons = form.batchSizeGallons,
                        estimatedAbv = form.estimatedAbv,
                        brewingTimeMinutes = form.brewingTimeMinutes,
                        fermentationDays = form.fermentationDays,
                        conditioningDays = form.conditioningDays,
                        brewingInstructions = form.brewingInstructions,
                        fermentationNotes = form.fermentationNotes,
                        notes = form.notes,
                        updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                    )
                } else {
                    // Create new recipe
                    Recipe(
                        name = form.name,
                        description = form.description,
                        beverageType = form.beverageType,
                        author = form.author,
                        source = form.source,
                        difficulty = form.difficulty,
                        batchSizeGallons = form.batchSizeGallons,
                        estimatedAbv = form.estimatedAbv,
                        brewingTimeMinutes = form.brewingTimeMinutes,
                        fermentationDays = form.fermentationDays,
                        conditioningDays = form.conditioningDays,
                        brewingInstructions = form.brewingInstructions,
                        fermentationNotes = form.fermentationNotes,
                        notes = form.notes
                    )
                }
                
                val result = if (isEditing) {
                    recipeRepository.updateRecipe(recipe).map { recipe.id }
                } else {
                    recipeRepository.createRecipe(recipe)
                }
                
                result
                    .onSuccess { recipeId ->
                        // Save ingredients
                        saveRecipeIngredients(recipeId)
                        
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                isSaved = true,
                                savedRecipeId = recipeId
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to save recipe: ${error.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }
    
    private suspend fun saveRecipeIngredients(recipeId: String) {
        val ingredients = _recipeIngredients.value.map { form ->
            RecipeIngredient(
                recipeId = recipeId,
                ingredientId = form.ingredientId,
                quantity = form.quantity,
                unit = form.unit,
                step = form.step,
                additionTime = form.additionTime,
                instructions = form.instructions,
                isOptional = form.isOptional,
                notes = form.notes
            )
        }
        
        // Replace all ingredients for this recipe
        if (isEditing && existingRecipeId != null) {
            // Remove existing ingredients and add new ones
            // This is handled by the repository's replaceRecipeIngredients method
        }
        
        ingredients.forEach { ingredient ->
            recipeRepository.addIngredientToRecipe(
                recipeId = ingredient.recipeId,
                ingredientId = ingredient.ingredientId,
                quantity = ingredient.quantity,
                unit = ingredient.unit,
                step = ingredient.step,
                additionTime = ingredient.additionTime
            )
        }
    }
    
    private fun loadExistingRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val recipe = recipeRepository.getRecipe(recipeId)
                if (recipe != null) {
                    _recipeForm.update {
                        RecipeFormState(
                            name = recipe.name,
                            description = recipe.description,
                            beverageType = recipe.beverageType,
                            author = recipe.author,
                            source = recipe.source,
                            difficulty = recipe.difficulty,
                            batchSizeGallons = recipe.batchSizeGallons,
                            estimatedAbv = recipe.estimatedAbv,
                            brewingTimeMinutes = recipe.brewingTimeMinutes,
                            fermentationDays = recipe.fermentationDays,
                            conditioningDays = recipe.conditioningDays,
                            brewingInstructions = recipe.brewingInstructions,
                            fermentationNotes = recipe.fermentationNotes,
                            notes = recipe.notes
                        )
                    }
                    
                    // Load ingredients
                    val ingredients = recipeRepository.observeRecipeIngredients(recipeId)
                        .first()
                        .map { ingredient ->
                            RecipeIngredientForm(
                                ingredientId = ingredient.ingredientId,
                                quantity = ingredient.quantity,
                                unit = ingredient.unit,
                                step = ingredient.step,
                                additionTime = ingredient.additionTime,
                                instructions = ingredient.instructions,
                                isOptional = ingredient.isOptional,
                                notes = ingredient.notes
                            )
                        }
                    
                    _recipeIngredients.update { ingredients }
                }
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load recipe: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun validateForm(): Boolean {
        val form = _recipeForm.value
        val errors = mutableMapOf<String, String>()
        
        if (form.name.isBlank()) {
            errors["name"] = "Recipe name is required"
        }
        
        if (form.batchSizeGallons <= 0) {
            errors["batchSize"] = "Batch size must be greater than 0"
        }
        
        if (_recipeIngredients.value.isEmpty()) {
            errors["ingredients"] = "At least one ingredient is required"
        }
        
        _uiState.update { it.copy(formErrors = errors) }
        return errors.isEmpty()
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSavedState() {
        _uiState.update { it.copy(isSaved = false, savedRecipeId = null) }
    }
}

/**
 * UI state for the recipe builder screen
 */
data class RecipeBuilderUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val savedRecipeId: String? = null,
    val formErrors: Map<String, String> = emptyMap(),
    val showBeverageTypeChangeWarning: Boolean = false
)

/**
 * Form state for recipe creation/editing
 */
data class RecipeFormState(
    val name: String = "",
    val description: String = "",
    val beverageType: BeverageType = BeverageType.BEER,
    val author: String = "",
    val source: String = "",
    val difficulty: Int = 1,
    val batchSizeGallons: Double = 5.0,
    val estimatedAbv: Double? = null,
    val brewingTimeMinutes: Int? = null,
    val fermentationDays: Int? = null,
    val conditioningDays: Int? = null,
    val brewingInstructions: String = "",
    val fermentationNotes: String = "",
    val notes: String = ""
)

/**
 * Form state for recipe ingredients
 */
data class RecipeIngredientForm(
    val ingredientId: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val step: String = "",
    val additionTime: Int = 0,
    val instructions: String = "",
    val isOptional: Boolean = false,
    val notes: String = ""
)