package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.ProSubscriptionSheet
import com.example.ui.screens.ListScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.RestaurantDetailScreen
import com.example.ui.screens.SavedFavoritesScreen
import com.example.ui.screens.SettingsProScreen
import com.example.ui.screens.TrendsAlertsScreen
import com.example.ui.theme.CleanBiteTheme
import com.example.ui.theme.ProGold
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CleanBiteTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
                val billingState by viewModel.billingState.collectAsStateWithLifecycle()
                val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
                val activeSpeakingId by viewModel.activeSpeakingId.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }
                val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val proSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                LaunchedEffect(uiState.snackbarMessage) {
                    uiState.snackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.dismissSnackbar()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        if (uiState.activeRestaurantDetails == null) {
                            NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                                NavigationBarItem(
                                    selected = uiState.activeTab == AppTab.MAP,
                                    onClick = { viewModel.selectTab(AppTab.MAP) },
                                    icon = { Icon(imageVector = Icons.Default.Map, contentDescription = "Map") },
                                    label = { Text("Map", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("tab_map")
                                )

                                NavigationBarItem(
                                    selected = uiState.activeTab == AppTab.LIST,
                                    onClick = { viewModel.selectTab(AppTab.LIST) },
                                    icon = { Icon(imageVector = Icons.Default.Explore, contentDescription = "Explore") },
                                    label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("tab_explore")
                                )

                                NavigationBarItem(
                                    selected = uiState.activeTab == AppTab.TRENDS,
                                    onClick = { viewModel.selectTab(AppTab.TRENDS) },
                                    icon = { Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "News") },
                                    label = { Text("Alerts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("tab_alerts")
                                )

                                NavigationBarItem(
                                    selected = uiState.activeTab == AppTab.FAVORITES,
                                    onClick = { viewModel.selectTab(AppTab.FAVORITES) },
                                    icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Bookmarks") },
                                    label = { Text("Saved", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("tab_saved")
                                )

                                NavigationBarItem(
                                    selected = uiState.activeTab == AppTab.PRO,
                                    onClick = { viewModel.selectTab(AppTab.PRO) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = "Pro Pass",
                                            tint = if (billingState.isProActive) ProGold else androidx.compose.ui.graphics.Color.Unspecified
                                        )
                                    },
                                    label = { Text("Pro Pass", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("tab_pro")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Tab Content
                        when (uiState.activeTab) {
                            AppTab.MAP -> MapScreen(
                                restaurants = uiState.filteredRestaurants,
                                selectedRestaurant = uiState.selectedMapRestaurant,
                                filterOptions = uiState.filterOptions,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onOpenFilterSheet = { viewModel.toggleFilterSheet(true) },
                                onSelectRestaurant = { viewModel.selectMapRestaurant(it) },
                                onOpenDetails = { viewModel.openRestaurantDetails(it) },
                                onOpenPro = { viewModel.toggleProSheet(true) },
                                onUpdateFilter = { viewModel.updateFilter(it) },
                                currentCity = uiState.currentCity,
                                feedStatus = uiState.feedStatus,
                                onSelectCity = { viewModel.selectCityFeed(it) },
                                onRefreshFeed = { viewModel.refreshCurrentFeed() },
                                locationState = uiState.locationState,
                                isDrivingModeActive = uiState.isDrivingModeActive,
                                onToggleDrivingMode = { viewModel.toggleDrivingMode() },
                                onLocateMe = { viewModel.locateUserAndAutoFetch() }
                            )

                            AppTab.LIST -> ListScreen(
                                restaurants = uiState.filteredRestaurants,
                                favoriteIds = favoriteIds,
                                filterOptions = uiState.filterOptions,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onOpenFilterSheet = { viewModel.toggleFilterSheet(true) },
                                onResetFilter = { viewModel.resetFilter() },
                                onRestaurantClick = { viewModel.openRestaurantDetails(it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                onSpeakClick = { viewModel.speakListing(it) },
                                activeSpeakingId = activeSpeakingId,
                                currentCity = uiState.currentCity,
                                feedStatus = uiState.feedStatus,
                                onSelectCity = { viewModel.selectCityFeed(it) },
                                onRefreshFeed = { viewModel.refreshCurrentFeed() }
                            )

                            AppTab.TRENDS -> TrendsAlertsScreen(
                                restaurants = uiState.filteredRestaurants,
                                onRestaurantClick = { viewModel.openRestaurantDetails(it) }
                            )

                            AppTab.FAVORITES -> {
                                val favList = uiState.filteredRestaurants.filter { favoriteIds.contains(it.id) }
                                SavedFavoritesScreen(
                                    favoriteRestaurants = favList,
                                    onRestaurantClick = { viewModel.openRestaurantDetails(it) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                    onSpeakClick = { viewModel.speakListing(it) },
                                    activeSpeakingId = activeSpeakingId
                                )
                            }

                            AppTab.PRO -> SettingsProScreen(
                                billingState = billingState,
                                onOpenProSheet = { viewModel.toggleProSheet(true) },
                                onCancelSub = { viewModel.cancelSubscription() },
                                onRestorePurchase = { viewModel.restorePurchases() }
                            )
                        }

                        // Full Screen Detail Modal Overlay
                        AnimatedVisibility(
                            visible = uiState.activeRestaurantDetails != null,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            uiState.activeRestaurantDetails?.let { restaurant ->
                                RestaurantDetailScreen(
                                    restaurant = restaurant,
                                    inspectionReports = uiState.inspectionReports,
                                    reviews = uiState.reviews,
                                    isFavorite = favoriteIds.contains(restaurant.id),
                                    onBack = {
                                        viewModel.stopSpeech()
                                        viewModel.closeRestaurantDetails()
                                    },
                                    onFavoriteToggle = { viewModel.toggleFavorite(restaurant.id) },
                                    onAddReview = { rating, comment, tag ->
                                        viewModel.postUserReview(restaurant.id, rating, comment, tag)
                                    },
                                    onSpeakClick = { viewModel.speakListing(restaurant, uiState.inspectionReports) },
                                    isSpeaking = (activeSpeakingId == restaurant.id)
                                )
                            }
                        }

                        // Filter Bottom Sheet Modal
                        if (uiState.isFilterSheetOpen) {
                            FilterBottomSheet(
                                sheetState = filterSheetState,
                                currentFilter = uiState.filterOptions,
                                onApplyFilter = { viewModel.updateFilter(it) },
                                onResetFilter = { viewModel.resetFilter() },
                                onDismiss = { viewModel.toggleFilterSheet(false) }
                            )
                        }

                        // Google Play Pro Subscription Sheet Modal
                        if (uiState.isProSheetOpen) {
                            ProSubscriptionSheet(
                                sheetState = proSheetState,
                                billingState = billingState,
                                onPurchaseTier = { viewModel.purchaseProTier(it) },
                                onRestorePurchase = { viewModel.restorePurchases() },
                                onCancelSub = { viewModel.cancelSubscription() },
                                onDismiss = { viewModel.toggleProSheet(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}
