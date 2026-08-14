package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FilterOptions
import com.example.data.model.SortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    currentFilter: FilterOptions,
    onApplyFilter: (FilterOptions) -> Unit,
    onResetFilter: () -> Unit,
    onDismiss: () -> Unit
) {
    var filterState by remember(currentFilter) { mutableStateOf(currentFilter) }

    val cuisines = listOf("All", "Italian", "Mexican", "Japanese", "American", "Thai", "Vegan", "Seafood", "Bakery", "Coffee")
    val prices = listOf("$", "$$", "$$$", "$$$$")
    val grades = listOf("Grade A", "Grade A & B", "C", "Critical")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & Sort Options",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Health Department Grade Filter
            Text(
                text = "Health Inspection Grade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grades.forEach { grade ->
                    val isSelected = filterState.selectedGrade == grade
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            filterState = filterState.copy(
                                selectedGrade = if (isSelected) null else grade
                            )
                        },
                        label = { Text(grade) },
                        leadingIcon = if (isSelected) {
                            { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
                        } else null,
                        modifier = Modifier.testTag("chip_grade_$grade")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Cuisine Filter
            Text(
                text = "Cuisine Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cuisines.forEach { cuisine ->
                    val isSelected = filterState.selectedCuisine == cuisine || (cuisine == "All" && filterState.selectedCuisine == null)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            filterState = filterState.copy(
                                selectedCuisine = if (cuisine == "All") null else cuisine
                            )
                        },
                        label = { Text(cuisine) },
                        leadingIcon = if (isSelected) {
                            { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
                        } else null,
                        modifier = Modifier.testTag("chip_cuisine_$cuisine")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Price Filter
            Text(
                text = "Price Tier",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                prices.forEach { price ->
                    val isSelected = filterState.selectedPrice == price
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            filterState = filterState.copy(
                                selectedPrice = if (isSelected) null else price
                            )
                        },
                        label = { Text(price) },
                        modifier = Modifier.testTag("chip_price_$price")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Inspection History Trends Switches
            Text(
                text = "Inspection History Trends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Only Improving Health Scores", fontWeight = FontWeight.Medium)
                    Text("Filter spots with stable or rising health scores", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = filterState.onlyImprovingTrend,
                    onCheckedChange = { filterState = filterState.copy(onlyImprovingTrend = it) },
                    modifier = Modifier.testTag("switch_improving_trend")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Zero Critical Violations", fontWeight = FontWeight.Medium)
                    Text("Exclude restaurants with recent critical citations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = filterState.zeroCriticalViolations,
                    onCheckedChange = { filterState = filterState.copy(zeroCriticalViolations = it) },
                    modifier = Modifier.testTag("switch_zero_critical")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Sort Options
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOption.entries.forEach { option ->
                    val isSelected = filterState.sortBy == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterState = filterState.copy(sortBy = option) },
                        label = { Text(option.displayName) },
                        modifier = Modifier.testTag("chip_sort_${option.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onResetFilter()
                        filterState = FilterOptions()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_filters_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                    Text("Reset All", modifier = Modifier.padding(start = 6.dp))
                }

                Button(
                    onClick = {
                        onApplyFilter(filterState)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("apply_filters_button")
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}
