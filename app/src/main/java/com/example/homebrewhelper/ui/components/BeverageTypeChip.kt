package com.example.homebrewhelper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.homebrewhelper.data.model.BeverageType

/**
 * Chip component for beverage type selection and display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeverageTypeChip(
    beverageType: BeverageType,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = if (isSelected) {
        FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        FilterChipDefaults.filterChipColors()
    }
    
    FilterChip(
        onClick = { onClick?.invoke() },
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = beverageType.icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = beverageType.displayName,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        },
        selected = isSelected,
        colors = colors,
        modifier = modifier
    )
}

/**
 * Grid of beverage type chips for selection
 */
@Composable
fun BeverageTypeGrid(
    selectedType: BeverageType?,
    onTypeSelected: (BeverageType?) -> Unit,
    modifier: Modifier = Modifier,
    showClearAll: Boolean = true
) {
    Column(modifier = modifier) {
        if (showClearAll) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onTypeSelected(null) },
                    label = { Text("All Types") },
                    selected = selectedType == null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocalBar,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // First row: Beer, Wine, Mead
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BeverageTypeChip(
                beverageType = BeverageType.BEER,
                isSelected = selectedType == BeverageType.BEER,
                onClick = { onTypeSelected(BeverageType.BEER) },
                modifier = Modifier.weight(1f)
            )
            BeverageTypeChip(
                beverageType = BeverageType.WINE,
                isSelected = selectedType == BeverageType.WINE,
                onClick = { onTypeSelected(BeverageType.WINE) },
                modifier = Modifier.weight(1f)
            )
            BeverageTypeChip(
                beverageType = BeverageType.MEAD,
                isSelected = selectedType == BeverageType.MEAD,
                onClick = { onTypeSelected(BeverageType.MEAD) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Second row: Cider, Kombucha, Specialty
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BeverageTypeChip(
                beverageType = BeverageType.CIDER,
                isSelected = selectedType == BeverageType.CIDER,
                onClick = { onTypeSelected(BeverageType.CIDER) },
                modifier = Modifier.weight(1f)
            )
            BeverageTypeChip(
                beverageType = BeverageType.KOMBUCHA,
                isSelected = selectedType == BeverageType.KOMBUCHA,
                onClick = { onTypeSelected(BeverageType.KOMBUCHA) },
                modifier = Modifier.weight(1f)
            )
            BeverageTypeChip(
                beverageType = BeverageType.SPECIALTY,
                isSelected = selectedType == BeverageType.SPECIALTY,
                onClick = { onTypeSelected(BeverageType.SPECIALTY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}