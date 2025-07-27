package com.example.homebrewhelper.ui.screens.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homebrewhelper.data.database.entity.Ingredient
import com.example.homebrewhelper.data.model.BeverageType
import com.example.homebrewhelper.data.model.IngredientType
import com.example.homebrewhelper.data.repository.IngredientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dialog for selecting ingredients from the database
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientPickerDialog(
    beverageType: BeverageType,
    onIngredientSelected: (String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IngredientPickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableIngredients by viewModel.availableIngredients.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedIngredientType by viewModel.selectedIngredientType.collectAsStateWithLifecycle()
    
    var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var step by remember { mutableStateOf("Fermentation") }
    var additionTime by remember { mutableStateOf("0") }
    
    LaunchedEffect(beverageType) {
        viewModel.loadIngredientsForBeverage(beverageType)
    }
    
    LaunchedEffect(selectedIngredient) {
        selectedIngredient?.let { ingredient ->
            if (unit.isBlank()) {
                unit = ingredient.defaultUnit
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Ingredient",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Divider()
                
                // Content
                if (selectedIngredient == null) {
                    // Ingredient selection phase
                    IngredientSelectionContent(
                        beverageType = beverageType,
                        availableIngredients = availableIngredients,
                        searchQuery = searchQuery,
                        selectedIngredientType = selectedIngredientType,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onIngredientTypeSelected = viewModel::selectIngredientType,
                        onIngredientSelected = { ingredient ->
                            selectedIngredient = ingredient
                        },
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Ingredient details and quantity input phase
                    IngredientDetailsContent(
                        ingredient = selectedIngredient!!,
                        quantity = quantity,
                        unit = unit,
                        step = step,
                        additionTime = additionTime,
                        onQuantityChange = { quantity = it },
                        onUnitChange = { unit = it },
                        onStepChange = { step = it },
                        onAdditionTimeChange = { additionTime = it },
                        onBack = { selectedIngredient = null },
                        onConfirm = {
                            if (quantity.isNotBlank() && unit.isNotBlank()) {
                                onIngredientSelected(
                                    selectedIngredient!!.id,
                                    quantity,
                                    unit,
                                    step,
                                    additionTime
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientSelectionContent(
    beverageType: BeverageType,
    availableIngredients: List<Ingredient>,
    searchQuery: String,
    selectedIngredientType: IngredientType?,
    onSearchQueryChange: (String) -> Unit,
    onIngredientTypeSelected: (IngredientType?) -> Unit,
    onIngredientSelected: (Ingredient) -> Unit,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Search and filters
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search ingredients...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Ingredient type filter
            Text(
                text = "Filter by type:",
                style = MaterialTheme.typography.labelMedium
            )
            
            LazyColumn(
                modifier = Modifier.height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val applicableTypes = IngredientType.getForBeverage(beverageType)
                items(applicableTypes) { type ->
                    FilterChip(
                        onClick = {
                            onIngredientTypeSelected(
                                if (selectedIngredientType == type) null else type
                            )
                        },
                        label = { Text(type.displayName) },
                        selected = selectedIngredientType == type
                    )
                }
            }
        }
        
        Divider()
        
        // Ingredients list
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Error loading ingredients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            availableIngredients.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No ingredients found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try adjusting your search or filter",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableIngredients) { ingredient ->
                        IngredientCard(
                            ingredient = ingredient,
                            onClick = { onIngredientSelected(ingredient) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientCard(
    ingredient: Ingredient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = ingredient.type.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (ingredient.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ingredient.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            
            if (ingredient.category.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Category: ${ingredient.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Usage range if available
            ingredient.getUsageRange()?.let { range ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Typical usage: $range",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IngredientDetailsContent(
    ingredient: Ingredient,
    quantity: String,
    unit: String,
    step: String,
    additionTime: String,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onStepChange: (String) -> Unit,
    onAdditionTimeChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Back button and ingredient info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ingredient.type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ingredient details
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ingredient Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (ingredient.description.isNotBlank()) {
                            Text(
                                text = ingredient.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (ingredient.category.isNotBlank()) {
                            Text(
                                text = "Category: ${ingredient.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        ingredient.getUsageRange()?.let { range ->
                            Text(
                                text = "Typical usage: $range",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Quantity and unit input
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Quantity & Unit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = onQuantityChange,
                                label = { Text("Quantity *") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = unit,
                                onValueChange = onUnitChange,
                                label = { Text("Unit *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        
                        // Common units for this ingredient type
                        val commonUnits = ingredient.alternateUnits.split(",") + ingredient.defaultUnit
                        if (commonUnits.size > 1) {
                            Text(
                                text = "Common units:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            LazyColumn(
                                modifier = Modifier.height(60.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(commonUnits.filter { it.isNotBlank() }) { commonUnit ->
                                    FilterChip(
                                        onClick = { onUnitChange(commonUnit.trim()) },
                                        label = { Text(commonUnit.trim()) },
                                        selected = unit == commonUnit.trim()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Step and timing
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Brewing Step & Timing",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = step,
                            onValueChange = onStepChange,
                            label = { Text("Brewing Step") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = additionTime,
                            onValueChange = onAdditionTimeChange,
                            label = { Text("Addition Time (minutes)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onConfirm,
                enabled = quantity.isNotBlank() && unit.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Ingredient")
            }
        }
    }
}

@HiltViewModel
class IngredientPickerViewModel @Inject constructor(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(IngredientPickerUiState())
    val uiState: StateFlow<IngredientPickerUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedIngredientType = MutableStateFlow<IngredientType?>(null)
    val selectedIngredientType: StateFlow<IngredientType?> = _selectedIngredientType.asStateFlow()
    
    private val _allIngredients = MutableStateFlow<List<Ingredient>>(emptyList())
    
    val availableIngredients: StateFlow<List<Ingredient>> = combine(
        _allIngredients,
        searchQuery,
        selectedIngredientType
    ) { ingredients, query, type ->
        ingredients
            .filter { ingredient ->
                // Filter by type if selected
                type == null || ingredient.type == type
            }
            .filter { ingredient ->
                // Filter by search query
                query.isBlank() || 
                ingredient.name.contains(query, ignoreCase = true) ||
                ingredient.description.contains(query, ignoreCase = true) ||
                ingredient.category.contains(query, ignoreCase = true)
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun loadIngredientsForBeverage(beverageType: BeverageType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                ingredientRepository.getIngredientsForBeverage(beverageType)
                    .collect { ingredients ->
                        _allIngredients.value = ingredients
                        _uiState.update { it.copy(isLoading = false) }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load ingredients: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectIngredientType(type: IngredientType?) {
        _selectedIngredientType.value = type
    }
}

data class IngredientPickerUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)