package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.billing.BillingState
import com.example.data.billing.PlayBillingManager
import com.example.data.billing.ProTier
import com.example.data.local.CleanBiteDatabase
import com.example.data.local.UserReviewEntity
import com.example.data.model.CityFeed
import com.example.data.model.DataFeedStatus
import com.example.data.model.FilterOptions
import com.example.data.model.InspectionReport
import com.example.data.model.Restaurant
import com.example.data.model.UserReview
import com.example.data.repository.RestaurantRepository
import com.example.util.LocationTracker
import com.example.util.UserLocationState
import com.example.util.VoiceSynthesisManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    MAP("Map"),
    LIST("Explore"),
    TRENDS("Health News"),
    FAVORITES("Bookmarks"),
    PRO("Pro Pass")
}

data class MainUiState(
    val activeTab: AppTab = AppTab.MAP,
    val filterOptions: FilterOptions = FilterOptions(),
    val filteredRestaurants: List<Restaurant> = emptyList(),
    val selectedMapRestaurant: Restaurant? = null,
    val activeRestaurantDetails: Restaurant? = null,
    val inspectionReports: List<InspectionReport> = emptyList(),
    val reviews: List<UserReview> = emptyList(),
    val isFilterSheetOpen: Boolean = false,
    val isProSheetOpen: Boolean = false,
    val snackbarMessage: String? = null,
    val feedStatus: DataFeedStatus = DataFeedStatus(),
    val currentCity: CityFeed = CityFeed.SAN_FRANCISCO,
    val locationState: UserLocationState = UserLocationState(),
    val isDrivingModeActive: Boolean = false,
    val isGpsFollowEnabled: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CleanBiteDatabase.getDatabase(application)
    private val repository = RestaurantRepository(db.cleanBiteDao())
    private val billingManager = PlayBillingManager()
    private val voiceManager = VoiceSynthesisManager(application)
    private val locationTracker = LocationTracker(application)

    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
    val activeSpeakingId: StateFlow<String?> = voiceManager.activeSpeakingId
    val locationState: StateFlow<UserLocationState> = locationTracker.locationState

    private val _activeTab = MutableStateFlow(AppTab.MAP)
    private val _filterOptions = MutableStateFlow(FilterOptions())
    private val _selectedMapRestaurant = MutableStateFlow<Restaurant?>(null)
    private val _activeRestaurantDetails = MutableStateFlow<Restaurant?>(null)
    private val _isFilterSheetOpen = MutableStateFlow(false)
    private val _isProSheetOpen = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _isDrivingModeActive = MutableStateFlow(false)
    private val _isGpsFollowEnabled = MutableStateFlow(false)

    // Room Favorites
    val favoriteIds: StateFlow<Set<String>> = repository.favoritesFlow
        .map { list -> list.map { it.restaurantId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Billing State
    val billingState: StateFlow<BillingState> = billingManager.billingState

    val feedStatus: StateFlow<DataFeedStatus> = repository.feedStatus
    val currentCity: StateFlow<CityFeed> = repository.currentCity

    val uiState: StateFlow<MainUiState> = combine(
        combine(_activeTab, _filterOptions, _selectedMapRestaurant, _activeRestaurantDetails) { tab, filter, mapRest, detailsRest ->
            Quadruple(tab, filter, mapRest, detailsRest)
        },
        combine(_isFilterSheetOpen, _isProSheetOpen, _snackbarMessage, repository.loadedRestaurants) { isFilterOpen, isProOpen, snackbar, loaded ->
            Quadruple(isFilterOpen, isProOpen, snackbar, loaded)
        },
        combine(repository.feedStatus, repository.currentCity, locationTracker.locationState) { status, city, loc ->
            Triple(status, city, loc)
        },
        combine(_isDrivingModeActive, _isGpsFollowEnabled) { driving, follow ->
            driving to follow
        }
    ) { (tab, filter, mapRest, detailsRest), (isFilterOpen, isProOpen, snackbar, _), (status, city, loc), (driving, follow) ->
        val filtered = repository.getFilteredRestaurants(filter)
        val reports = detailsRest?.let { repository.getInspectionReports(it.id) } ?: emptyList()
        val reviews = detailsRest?.let { repository.getReviewsForRestaurant(it.id) } ?: emptyList()

        MainUiState(
            activeTab = tab,
            filterOptions = filter,
            filteredRestaurants = filtered,
            selectedMapRestaurant = mapRest,
            activeRestaurantDetails = detailsRest,
            inspectionReports = reports,
            reviews = reviews,
            isFilterSheetOpen = isFilterOpen,
            isProSheetOpen = isProOpen,
            snackbarMessage = snackbar,
            feedStatus = status,
            currentCity = city,
            locationState = loc,
            isDrivingModeActive = driving,
            isGpsFollowEnabled = follow
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MainUiState(filteredRestaurants = repository.getRestaurants())
    )

    init {
        // Asynchronously fetch live data feed on launch
        viewModelScope.launch {
            repository.refreshFeed(CityFeed.SAN_FRANCISCO)
        }

        // Start location tracking flow
        viewModelScope.launch {
            locationTracker.startContinuousTracking().collect { loc ->
                // Auto-jurisdiction detection when driving/travelling
                if (_isDrivingModeActive.value && loc.nearestCityFeed != repository.currentCity.value) {
                    repository.refreshFeed(loc.nearestCityFeed)
                    _snackbarMessage.value = "📍 Approaching ${loc.nearestCityFeed.cityName} jurisdiction. Switching to ${loc.nearestCityFeed.municipalitySource} inspections."
                }
            }
        }
    }

    fun toggleDrivingMode() {
        val nextState = !_isDrivingModeActive.value
        _isDrivingModeActive.value = nextState
        _isGpsFollowEnabled.value = nextState
        _snackbarMessage.value = if (nextState) {
            "🚗 Driving HUD Activated: Auto-fetching nearby health inspections along your route."
        } else {
            "Driving Mode Paused."
        }
    }

    fun toggleGpsFollow() {
        _isGpsFollowEnabled.value = !_isGpsFollowEnabled.value
        if (_isGpsFollowEnabled.value) {
            val userCity = locationState.value.nearestCityFeed
            if (userCity != repository.currentCity.value) {
                selectCityFeed(userCity)
            }
        }
    }

    fun locateUserAndAutoFetch() {
        val currentLoc = locationState.value
        val nearestCity = currentLoc.nearestCityFeed
        _isGpsFollowEnabled.value = true
        selectCityFeed(nearestCity)
        _snackbarMessage.value = "📍 Centered at GPS location (${nearestCity.cityName} Municipal Feed)"
    }

    fun selectCityFeed(cityFeed: CityFeed) {
        viewModelScope.launch {
            _selectedMapRestaurant.value = null
            repository.refreshFeed(cityFeed)
            _snackbarMessage.value = "Connected to ${cityFeed.cityName} Health Feed (${cityFeed.municipalitySource})"
        }
    }

    fun refreshCurrentFeed() {
        viewModelScope.launch {
            repository.refreshFeed(repository.currentCity.value)
            _snackbarMessage.value = "Live health inspections refreshed successfully!"
        }
    }

    fun selectTab(tab: AppTab) {
        _activeTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _filterOptions.value = _filterOptions.value.copy(searchQuery = query)
    }

    fun updateFilter(options: FilterOptions) {
        _filterOptions.value = options
    }

    fun resetFilter() {
        _filterOptions.value = FilterOptions()
    }

    fun selectMapRestaurant(restaurant: Restaurant?) {
        _selectedMapRestaurant.value = restaurant
    }

    fun openRestaurantDetails(restaurant: Restaurant) {
        _activeRestaurantDetails.value = restaurant
    }

    fun closeRestaurantDetails() {
        _activeRestaurantDetails.value = null
    }

    fun toggleFavorite(restaurantId: String) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(restaurantId)
            repository.toggleFavorite(restaurantId, isFav)
            _snackbarMessage.value = if (isFav) "Removed from Bookmarks" else "Saved to CleanBite Bookmarks"
        }
    }

    fun toggleFilterSheet(open: Boolean) {
        _isFilterSheetOpen.value = open
    }

    fun toggleProSheet(open: Boolean) {
        _isProSheetOpen.value = open
    }

    fun postUserReview(restaurantId: String, rating: Float, comment: String, tag: String) {
        val newReview = UserReview(
            id = "rev_" + System.currentTimeMillis(),
            restaurantId = restaurantId,
            authorName = "You (Verified Diner)",
            rating = rating,
            date = "2026-08-14",
            comment = comment,
            sentimentTag = tag,
            userCleanlinessRating = 5
        )
        repository.addReview(newReview)

        // Save to Room
        viewModelScope.launch {
            repository.addRoomReview(
                UserReviewEntity(
                    id = newReview.id,
                    restaurantId = restaurantId,
                    authorName = newReview.authorName,
                    rating = rating,
                    date = newReview.date,
                    comment = comment,
                    sentimentTag = tag,
                    userCleanlinessRating = 5
                )
            )
            _snackbarMessage.value = "Review published successfully!"
        }
    }

    fun purchaseProTier(tier: ProTier) {
        billingManager.launchBillingFlow(tier) { success ->
            if (success) {
                _snackbarMessage.value = "🎉 Welcome to CleanBite Pro (${tier.title})!"
                _isProSheetOpen.value = false
            }
        }
    }

    fun restorePurchases() {
        billingManager.restorePurchases { success, msg ->
            _snackbarMessage.value = msg
            if (success) _isProSheetOpen.value = false
        }
    }

    fun cancelSubscription() {
        billingManager.cancelSubscription()
        _snackbarMessage.value = "CleanBite Pro subscription cancelled."
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    fun speakListing(restaurant: Restaurant, reports: List<InspectionReport> = emptyList()) {
        voiceManager.speakListing(restaurant, reports)
    }

    fun stopSpeech() {
        voiceManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}

// Simple helper class for combining 4 items
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
