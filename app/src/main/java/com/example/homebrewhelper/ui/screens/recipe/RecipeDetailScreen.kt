package com.example.homebrewhelper.ui.screens.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.homebrewhelper.data.database.entity.Recipe
import com.example.homebrewhelper.data.database.entity.RecipeIngredient
import com.example.homebrewhelper.viewmodel.RecipeDetailViewModel

/**
 * Screen for viewing detailed recipe information including ingredients and instructions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val variations by viewModel.variations.collectAsStateWithLifecycle()
    val calculatedCost by viewModel.calculatedCost.collectAsStateWithLifecycle()
    
    var showMenu by remember { mutableStateOf(false) }
    var showScaleDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    
    // Handle navigation
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            onNavigateBack()
            viewModel.clearNavigateBack()
        }
    }
    
    LaunchedEffect(uiState.navigationTarget) {
        uiState.navigationTarget?.let { targetRecipeId ->
            onNavigateToRecipe(targetRecipeId)
            viewModel.clearNavigationTarget()
        }
    }
    
    if (recipe == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Recipe not found",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = recipe!!.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = recipe!!.beverageType.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    IconButton(
                        onClick = { viewModel.toggleFavorite() }
                    ) {
                        Icon(
                            imageVector = if (recipe!!.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (recipe!!.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (recipe!!.isFavorite) MaterialTheme.colorScheme.error else LocalContentColor.current
                        )
                    }
                    
                    Box {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Recipe") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToEdit(recipe!!.id)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = {
                                    showMenu = false
                                    showDuplicateDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Scale Recipe") },
                                onClick = {
                                    showMenu = false
                                    showScaleDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Straighten, contentDescription = null)
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showMenu = false
                                    viewModel.deleteRecipe()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
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
            // Recipe overview card
            item {
                RecipeOverviewCard(
                    recipe = recipe!!,
                    calculatedCost = calculatedCost
                )
            }
            
            // Description
            if (recipe!!.description.isNotBlank()) {
                item {
                    RecipeSectionCard(
                        title = "Description",
                        icon = Icons.Default.Description
                    ) {
                        Text(
                            text = recipe!!.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Ingredients
            if (ingredients.isNotEmpty()) {
                item {
                    RecipeSectionCard(
                        title = "Ingredients (${ingredients.size})",
                        icon = Icons.Default.Inventory
                    ) {
                        // TODO: Group ingredients by step
                        val ingredientsByStep = ingredients.groupBy { it.step }
                        
                        ingredientsByStep.forEach { (step, stepIngredients) ->
                            if (ingredientsByStep.size > 1) {
                                Text(
                                    text = step.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            stepIngredients.forEach { ingredient ->
                                IngredientItem(
                                    ingredient = ingredient,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Brewing instructions
            if (recipe!!.brewingInstructions.isNotBlank()) {
                item {
                    RecipeSectionCard(
                        title = "Brewing Instructions",
                        icon = Icons.Default.Assignment
                    ) {
                        Text(
                            text = recipe!!.brewingInstructions,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Fermentation notes
            if (recipe!!.fermentationNotes.isNotBlank()) {
                item {
                    RecipeSectionCard(
                        title = "Fermentation Notes",
                        icon = Icons.Default.Science
                    ) {
                        Text(
                            text = recipe!!.fermentationNotes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // General notes
            if (recipe!!.notes.isNotBlank()) {
                item {
                    RecipeSectionCard(
                        title = "Notes",
                        icon = Icons.Default.Notes
                    ) {
                        Text(
                            text = recipe!!.notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Recipe variations
            if (variations.isNotEmpty()) {
                item {
                    RecipeSectionCard(
                        title = "Recipe Variations (${variations.size})",
                        icon = Icons.Default.Tune
                    ) {
                        variations.forEach { variation ->
                            if (variation.id != recipe!!.id) {
                                VariationItem(
                                    variation = variation,
                                    onClick = { onNavigateToRecipe(variation.id) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Scale recipe dialog
    if (showScaleDialog) {
        ScaleRecipeDialog(
            currentBatchSize = recipe!!.batchSizeGallons,
            onConfirm = { newSize ->
                viewModel.scaleRecipe(newSize)
                showScaleDialog = false
            },
            onDismiss = { showScaleDialog = false }
        )
    }
    
    // Duplicate recipe dialog
    if (showDuplicateDialog) {
        DuplicateRecipeDialog(
            originalName = recipe!!.name,
            onConfirm = { newName ->
                viewModel.duplicateRecipe(newName)
                showDuplicateDialog = false
            },
            onDismiss = { showDuplicateDialog = false }
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show error snackbar
            viewModel.clearError()
        }
    }
    
    // Success messages
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // TODO: Show success snackbar
            viewModel.clearSuccessMessage()
        }
    }
}

@Composable
private fun RecipeOverviewCard(
    recipe: Recipe,
    calculatedCost: Double?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RecipeStatItem(
                    value = "${recipe.batchSizeGallons}gal",
                    label = "Batch Size",
                    icon = Icons.Default.LocalDrink
                )
                
                RecipeStatItem(
                    value = recipe.getDifficultyString(),
                    label = "Difficulty",
                    icon = Icons.Default.TrendingUp
                )
                
                recipe.calculateAbv()?.let { abv ->
                    RecipeStatItem(
                        value = "${String.format("%.1f", abv)}%",
                        label = "ABV",
                        icon = Icons.Default.Percent
                    )
                }
                
                calculatedCost?.let { cost ->
                    RecipeStatItem(
                        value = "$${String.format("%.2f", cost)}",
                        label = "Est. Cost",
                        icon = Icons.Default.AttachMoney
                    )
                }
            }
            
            if (recipe.author.isNotBlank() || recipe.source.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (recipe.author.isNotBlank()) {
                        Text(
                            text = "By: ${recipe.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    if (recipe.source.isNotBlank()) {
                        Text(
                            text = "Source: ${recipe.source}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeStatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun RecipeSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            content()
        }
    }
}

@Composable
private fun IngredientItem(
    ingredient: RecipeIngredient,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${ingredient.quantity} ${ingredient.unit}", // TODO: Get ingredient name from ID
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            if (ingredient.additionTime > 0) {
                Text(
                    text = ingredient.getFormattedTiming(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (ingredient.notes.isNotBlank()) {
                Text(
                    text = ingredient.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
        
        if (ingredient.isOptional) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "Optional",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun VariationItem(
    variation: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = variation.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Version ${variation.version} • ${variation.batchSizeGallons}gal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScaleRecipeDialog(
    currentBatchSize: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var newBatchSize by remember { mutableStateOf(currentBatchSize.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scale Recipe") },
        text = {
            Column {
                Text("Current batch size: ${currentBatchSize}gal")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newBatchSize,
                    onValueChange = { newBatchSize = it },
                    label = { Text("New batch size (gallons)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    newBatchSize.toDoubleOrNull()?.let { size ->
                        if (size > 0) {
                            onConfirm(size)
                        }
                    }
                },
                enabled = newBatchSize.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text("Scale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DuplicateRecipeDialog(
    originalName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf("$originalName (Copy)") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Duplicate Recipe") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Recipe name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank()
            ) {
                Text("Duplicate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}