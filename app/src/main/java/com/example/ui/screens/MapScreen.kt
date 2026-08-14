package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityFeed
import com.example.data.model.DataFeedStatus
import com.example.data.model.FilterOptions
import com.example.data.model.Restaurant
import com.example.ui.components.InteractiveMapCanvas
import com.example.util.UserLocationState

@Composable
fun MapScreen(
    restaurants: List<Restaurant>,
    selectedRestaurant: Restaurant?,
    filterOptions: FilterOptions,
    onSearchQueryChange: (String) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onSelectRestaurant: (Restaurant?) -> Unit,
    onOpenDetails: (Restaurant) -> Unit,
    onOpenPro: () -> Unit,
    onUpdateFilter: (FilterOptions) -> Unit,
    modifier: Modifier = Modifier,
    currentCity: CityFeed = CityFeed.SAN_FRANCISCO,
    feedStatus: DataFeedStatus = DataFeedStatus(),
    onSelectCity: (CityFeed) -> Unit = {},
    onRefreshFeed: () -> Unit = {},
    locationState: UserLocationState = UserLocationState(),
    isDrivingModeActive: Boolean = false,
    onToggleDrivingMode: () -> Unit = {},
    onLocateMe: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Interactive Map Base Layer with live municipal rendering & pin placement
        InteractiveMapCanvas(
            restaurants = restaurants,
            selectedRestaurant = selectedRestaurant,
            onSelectRestaurant = onSelectRestaurant,
            onOpenDetails = onOpenDetails,
            onOpenPro = onOpenPro,
            currentCity = currentCity,
            feedStatus = feedStatus,
            onSelectCity = onSelectCity,
            onRefreshFeed = onRefreshFeed,
            locationState = locationState,
            isDrivingModeActive = isDrivingModeActive,
            onToggleDrivingMode = onToggleDrivingMode,
            onLocateMe = onLocateMe,
            modifier = Modifier.fillMaxSize()
        )

        // Floating Search & Quick Filter Bar at Top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bar_container")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = filterOptions.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search restaurant name or cuisine...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_input")
                    )

                    if (filterOptions.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenFilterSheet,
                        modifier = Modifier.testTag("open_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Cuisine & Health Filter Chips Bar
            val cuisines = listOf("All", "Italian", "Mexican", "Japanese", "American", "Thai", "Bakery", "Cafe", "Seafood", "Vegan")

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Search result count badge if searching
                if (filterOptions.searchQuery.isNotBlank()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🔍 ${restaurants.size} matches",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Cuisines quick filters
                items(cuisines) { cuisine ->
                    val isSelected = if (cuisine == "All") {
                        filterOptions.selectedCuisine == null || filterOptions.selectedCuisine == "All"
                    } else {
                        filterOptions.selectedCuisine.equals(cuisine, ignoreCase = true)
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newCuisine = if (cuisine == "All" || isSelected) null else cuisine
                            onUpdateFilter(filterOptions.copy(selectedCuisine = newCuisine))
                        },
                        label = { Text(cuisine, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                // Health Grade A chip
                item {
                    val isGradeA = filterOptions.selectedGrade == "Grade A"
                    FilterChip(
                        selected = isGradeA,
                        onClick = {
                            onUpdateFilter(
                                filterOptions.copy(selectedGrade = if (isGradeA) null else "Grade A")
                            )
                        },
                        label = { Text("Grade A Only", fontSize = 11.sp) }
                    )
                }

                // 0 Critical Violations chip
                item {
                    val isZeroCrit = filterOptions.zeroCriticalViolations
                    FilterChip(
                        selected = isZeroCrit,
                        onClick = {
                            onUpdateFilter(
                                filterOptions.copy(zeroCriticalViolations = !isZeroCrit)
                            )
                        },
                        label = { Text("0 Critical", fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}
