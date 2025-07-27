package com.example.homebrewhelper.ui.screens.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.model.IngredientType
import com.example.homebrewhelper.ui.components.BeverageTypeGrid
import com.example.homebrewhelper.viewmodel.RecipeBuilderViewModel
import com.example.homebrewhelper.viewmodel.RecipeFormState
import com.example.homebrewhelper.viewmodel.RecipeIngredientForm

/**
 * Screen for creating and editing recipes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBuilderScreen(
    onNavigateBack: () -> Unit,
    onRecipeSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
    recipeId: String? = null,
    viewModel: RecipeBuilderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recipeForm by viewModel.recipeForm.collectAsStateWithLifecycle()
    val recipeIngredients by viewModel.recipeIngredients.collectAsStateWithLifecycle()
    
    val isEditing = recipeId != null
    var showIngredientPicker by remember { mutableStateOf(false) }
    
    // Handle recipe saved
    LaunchedEffect(uiState.isSaved, uiState.savedRecipeId) {
        if (uiState.isSaved && uiState.savedRecipeId != null) {
            onRecipeSaved(uiState.savedRecipeId!!)
            viewModel.clearSavedState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Recipe" else "Create Recipe",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveRecipe() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Information Section
            item {
                RecipeBasicInfoSection(
                    recipeForm = recipeForm,
                    formErrors = uiState.formErrors,
                    onNameChange = viewModel::updateName,
                    onDescriptionChange = viewModel::updateDescription,
                    onBeverageTypeChange = viewModel::updateBeverageType,
                    onBatchSizeChange = viewModel::updateBatchSize,
                    onDifficultyChange = viewModel::updateDifficulty
                )
            }
            
            // Recipe Details Section
            item {
                RecipeDetailsSection(
                    recipeForm = recipeForm,
                    onAuthorChange = viewModel::updateAuthor,
                    onSourceChange = viewModel::updateSource,
                    onAbvChange = viewModel::updateEstimatedAbv
                )
            }
            
            // Ingredients Section
            item {
                RecipeIngredientsSection(
                    ingredients = recipeIngredients,
                    beverageType = recipeForm.beverageType,
                    onAddIngredient = { showIngredientPicker = true },
                    onUpdateIngredient = viewModel::updateIngredient,
                    onRemoveIngredient = viewModel::removeIngredient,
                    formErrors = uiState.formErrors
                )
            }
            
            // Instructions Section
            item {
                RecipeInstructionsSection(
                    recipeForm = recipeForm,
                    onBrewingInstructionsChange = viewModel::updateBrewingInstructions,
                    onFermentationNotesChange = viewModel::updateFermentationNotes,
                    onNotesChange = viewModel::updateNotes
                )
            }
            
            // Timing Section
            item {
                RecipeTimingSection(
                    recipeForm = recipeForm,
                    onTimingUpdate = viewModel::updateTiming
                )
            }
        }
    }
    
    // Ingredient Picker Modal
    if (showIngredientPicker) {
        IngredientPickerDialog(
            beverageType = recipeForm.beverageType,
            onIngredientSelected = { ingredientId, quantity, unit, step, additionTime ->
                viewModel.addIngredient(ingredientId, quantity, unit, step, additionTime)
                showIngredientPicker = false
            },
            onDismiss = { showIngredientPicker = false }
        )
    }
    
    // Beverage type change warning
    if (uiState.showBeverageTypeChangeWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.keepIngredientsAfterBeverageTypeChange() },
            title = { Text("Beverage Type Changed") },
            text = {
                Text("Changing the beverage type may make some ingredients incompatible. Would you like to clear the current ingredients?")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearIngredientsAfterBeverageTypeChange() }
                ) {
                    Text("Clear Ingredients")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.keepIngredientsAfterBeverageTypeChange() }
                ) {
                    Text("Keep Ingredients")
                }
            }
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show error snackbar
            viewModel.clearError()
        }
    }
}

@Composable
private fun RecipeBasicInfoSection(
    recipeForm: RecipeFormState,
    formErrors: Map<String, String>,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBeverageTypeChange: (BeverageType) -> Unit,
    onBatchSizeChange: (String) -> Unit,
    onDifficultyChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Basic Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Recipe name
            OutlinedTextField(
                value = recipeForm.name,
                onValueChange = onNameChange,
                label = { Text("Recipe Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = formErrors.containsKey("name"),
                supportingText = formErrors["name"]?.let { { Text(it) } },
                singleLine = true
            )
            
            // Description
            OutlinedTextField(
                value = recipeForm.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            
            // Beverage type
            Column {
                Text(
                    text = "Beverage Type *",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BeverageTypeGrid(
                    selectedType = recipeForm.beverageType,
                    onTypeSelected = { type ->
                        if (type != null) {
                            onBeverageTypeChange(type)
                        }
                    },
                    showClearAll = false
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Batch size
                OutlinedTextField(
                    value = recipeForm.batchSizeGallons.toString(),
                    onValueChange = onBatchSizeChange,
                    label = { Text("Batch Size (gal) *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = formErrors.containsKey("batchSize"),
                    supportingText = formErrors["batchSize"]?.let { { Text(it) } },
                    singleLine = true
                )
                
                // Difficulty
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Difficulty",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (1..5).forEach { level ->
                            FilterChip(
                                onClick = { onDifficultyChange(level) },
                                label = { Text(level.toString()) },
                                selected = recipeForm.difficulty == level,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeDetailsSection(
    recipeForm: RecipeFormState,
    onAuthorChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onAbvChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recipe Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = recipeForm.author,
                    onValueChange = onAuthorChange,
                    label = { Text("Author") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = recipeForm.source,
                    onValueChange = onSourceChange,
                    label = { Text("Source") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            OutlinedTextField(
                value = recipeForm.estimatedAbv?.toString() ?: "",
                onValueChange = onAbvChange,
                label = { Text("Estimated ABV (%)") },
                modifier = Modifier.fillMaxWidth(0.5f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
    }
}

@Composable
private fun RecipeIngredientsSection(
    ingredients: List<RecipeIngredientForm>,
    beverageType: BeverageType,
    onAddIngredient: () -> Unit,
    onUpdateIngredient: (Int, RecipeIngredientForm) -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    formErrors: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ingredients (${ingredients.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = onAddIngredient
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Ingredient")
                }
            }
            
            if (formErrors.containsKey("ingredients")) {
                Text(
                    text = formErrors["ingredients"]!!, 
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (ingredients.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalBar,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No ingredients added yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Add ${beverageType.displayName.lowercase()} ingredients to complete your recipe",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                ingredients.forEachIndexed { index, ingredient ->
                    IngredientFormItem(
                        ingredient = ingredient,
                        onUpdate = { updatedIngredient ->
                            onUpdateIngredient(index, updatedIngredient)
                        },
                        onRemove = { onRemoveIngredient(index) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientFormItem(
    ingredient: RecipeIngredientForm,
    onUpdate: (RecipeIngredientForm) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (ingredient.ingredientId.isNotBlank()) {
                        "Ingredient: ${ingredient.ingredientId}"
                    } else {
                        "New Ingredient"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove ingredient",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (ingredient.ingredientId.isNotBlank()) {
                Text(
                    text = "${ingredient.quantity} ${ingredient.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (ingredient.step.isNotBlank()) {
                    Text(
                        text = "Step: ${ingredient.step}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (ingredient.additionTime > 0) {
                    Text(
                        text = "Added at: ${ingredient.additionTime} minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeInstructionsSection(
    recipeForm: RecipeFormState,
    onBrewingInstructionsChange: (String) -> Unit,
    onFermentationNotesChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Instructions & Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = recipeForm.brewingInstructions,
                onValueChange = onBrewingInstructionsChange,
                label = { Text("Brewing Instructions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )
            
            OutlinedTextField(
                value = recipeForm.fermentationNotes,
                onValueChange = onFermentationNotesChange,
                label = { Text("Fermentation Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            
            OutlinedTextField(
                value = recipeForm.notes,
                onValueChange = onNotesChange,
                label = { Text("General Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun RecipeTimingSection(
    recipeForm: RecipeFormState,
    onTimingUpdate: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var brewingTime by remember { mutableStateOf(recipeForm.brewingTimeMinutes?.toString() ?: "") }
    var fermentationDays by remember { mutableStateOf(recipeForm.fermentationDays?.toString() ?: "") }
    var conditioningDays by remember { mutableStateOf(recipeForm.conditioningDays?.toString() ?: "") }
    
    LaunchedEffect(brewingTime, fermentationDays, conditioningDays) {
        onTimingUpdate(brewingTime, fermentationDays, conditioningDays)
    }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Timeline (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = brewingTime,
                    onValueChange = { brewingTime = it },
                    label = { Text("Brewing (min)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = fermentationDays,
                    onValueChange = { fermentationDays = it },
                    label = { Text("Fermentation (days)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = conditioningDays,
                    onValueChange = { conditioningDays = it },
                    label = { Text("Conditioning (days)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}