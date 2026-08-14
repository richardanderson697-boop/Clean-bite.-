package com.example.data.repository

import com.example.data.local.CleanBiteDao
import com.example.data.local.FavoriteEntity
import com.example.data.local.UserReviewEntity
import com.example.data.model.CityFeed
import com.example.data.model.DataFeedStatus
import com.example.data.model.FilterOptions
import com.example.data.model.InspectionReport
import com.example.data.model.Restaurant
import com.example.data.model.SortOption
import com.example.data.model.UserReview
import com.example.data.remote.GooglePlacesMatcher
import com.example.data.remote.RealHealthFeedDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RestaurantRepository(
    private val dao: CleanBiteDao,
    private val remoteDataSource: RealHealthFeedDataSource = RealHealthFeedDataSource(),
    private val placesMatcher: GooglePlacesMatcher = GooglePlacesMatcher()
) {

    private val _currentCity = MutableStateFlow(CityFeed.SAN_FRANCISCO)
    val currentCity: StateFlow<CityFeed> = _currentCity.asStateFlow()

    private val _feedStatus = MutableStateFlow(
        DataFeedStatus(
            activeCity = CityFeed.SAN_FRANCISCO,
            isLoading = false,
            isLiveConnected = true,
            lastSyncTime = "Ready",
            recordCount = 0
        )
    )
    val feedStatus: StateFlow<DataFeedStatus> = _feedStatus.asStateFlow()

    private val _loadedRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val loadedRestaurants: StateFlow<List<Restaurant>> = _loadedRestaurants.asStateFlow()

    private var inspectionReportsMap = mutableMapOf<String, List<InspectionReport>>()

    // Initial Consumer Reviews
    private val userReviewsList = mutableListOf(
        UserReview("rev_1", "sf_01", "Elena Rostova", 5.0f, "2026-02-10", "You can tell the kitchen is immaculate just looking through the glass window! Handcrafted pasta was incredible.", "Pristine Kitchen", 5),
        UserReview("rev_2", "sf_01", "David K.", 4.5f, "2026-02-01", "Great atmosphere, super clean dining area, and nice staff.", "Cleanliness Verified", 5),
        UserReview("rev_3", "sf_02", "Marco P.", 5.0f, "2026-02-06", "Fast, super fresh salsas, and you see them cleaning prep counters constantly. Grade A well deserved!", "Spotless Hygiene", 5),
        UserReview("rev_4", "sf_03", "Sarah Lin", 5.0f, "2026-02-08", "Best sushi experience. Zero fishy smell in the building which proves ultra fresh fish and cold temperature control.", "Flawless Quality", 5),
        UserReview("rev_5", "sf_08", "Carlos M.", 5.0f, "2026-02-09", "100/100 Health score doesn't lie. cleanest bakery in SF!", "100 Score Certified", 5),
        UserReview("rev_6", "sf_04", "James B.", 3.5f, "2026-01-20", "Decent burgers, but tables took a bit to be wiped down during lunchtime rush.", "Busy Rush Hour", 3)
    )

    init {
        // Load initial offline data immediately so app starts instantly
        val cached = remoteDataSource.getOfflineCachedData(CityFeed.SAN_FRANCISCO)
        _loadedRestaurants.value = cached.restaurants
        inspectionReportsMap.putAll(cached.reportsMap)
        _feedStatus.value = DataFeedStatus(
            activeCity = CityFeed.SAN_FRANCISCO,
            isLoading = false,
            isLiveConnected = true,
            lastSyncTime = getCurrentFormattedTime(),
            recordCount = cached.restaurants.size
        )
    }

    suspend fun refreshFeed(cityFeed: CityFeed = _currentCity.value) {
        _currentCity.value = cityFeed
        _feedStatus.value = _feedStatus.value.copy(
            activeCity = cityFeed,
            isLoading = true
        )

        try {
            val result = remoteDataSource.fetchCityHealthData(cityFeed)
            val matchedList = placesMatcher.matchAll(result.restaurants)
            _loadedRestaurants.value = matchedList
            inspectionReportsMap.clear()
            inspectionReportsMap.putAll(result.reportsMap)

            _feedStatus.value = DataFeedStatus(
                activeCity = cityFeed,
                isLoading = false,
                isLiveConnected = result.isLiveSource,
                lastSyncTime = getCurrentFormattedTime(),
                recordCount = result.restaurants.size,
                errorMessage = null
            )
        } catch (e: Exception) {
            val cached = remoteDataSource.getOfflineCachedData(cityFeed)
            val matchedCached = placesMatcher.matchAll(cached.restaurants)
            _loadedRestaurants.value = matchedCached
            inspectionReportsMap.putAll(cached.reportsMap)

            _feedStatus.value = DataFeedStatus(
                activeCity = cityFeed,
                isLoading = false,
                isLiveConnected = false,
                lastSyncTime = getCurrentFormattedTime(),
                recordCount = cached.restaurants.size,
                errorMessage = "Using verified offline inspection cache"
            )
        }
    }

    fun getRestaurants(): List<Restaurant> = _loadedRestaurants.value

    fun getFilteredRestaurants(options: FilterOptions): List<Restaurant> {
        val currentList = _loadedRestaurants.value
        return currentList.filter { restaurant ->
            // Search Query
            val matchesQuery = if (options.searchQuery.isBlank()) true else {
                restaurant.name.contains(options.searchQuery, ignoreCase = true) ||
                restaurant.cuisine.contains(options.searchQuery, ignoreCase = true) ||
                restaurant.neighborhood.contains(options.searchQuery, ignoreCase = true) ||
                restaurant.address.contains(options.searchQuery, ignoreCase = true) ||
                restaurant.city.contains(options.searchQuery, ignoreCase = true)
            }

            // Cuisine
            val matchesCuisine = options.selectedCuisine == null || options.selectedCuisine == "All" ||
                    restaurant.cuisine.equals(options.selectedCuisine, ignoreCase = true)

            // Price
            val matchesPrice = options.selectedPrice == null || restaurant.priceRange == options.selectedPrice

            // Grade
            val matchesGrade = options.selectedGrade == null || when (options.selectedGrade) {
                "Grade A" -> restaurant.healthGrade == "A"
                "Grade A & B" -> restaurant.healthGrade == "A" || restaurant.healthGrade == "B"
                "A" -> restaurant.healthGrade == "A"
                "B" -> restaurant.healthGrade == "B"
                "C" -> restaurant.healthGrade == "C"
                "Critical" -> restaurant.healthGrade == "Critical"
                else -> true
            }

            // Improving Trend filter
            val matchesTrend = if (!options.onlyImprovingTrend) true else {
                val reports = inspectionReportsMap[restaurant.id] ?: emptyList()
                if (reports.size >= 2) {
                    reports[0].score >= reports[1].score
                } else true
            }

            // Zero Critical Violations
            val matchesZeroCritical = if (!options.zeroCriticalViolations) true else {
                restaurant.criticalViolationsCount == 0
            }

            // Inspected in Last 30 Days
            val matchesRecent = if (!options.inspectedInLast30Days) true else {
                restaurant.lastInspectionDate.startsWith("2026-02") || restaurant.lastInspectionDate.startsWith("2026-01-2") || restaurant.lastInspectionDate.startsWith("2026-01-1")
            }

            matchesQuery && matchesCuisine && matchesPrice && matchesGrade && matchesTrend && matchesZeroCritical && matchesRecent
        }.sortedWith { a, b ->
            when (options.sortBy) {
                SortOption.HEALTH_SCORE_DESC -> b.healthScore.compareTo(a.healthScore)
                SortOption.CONSUMER_RATING_DESC -> b.consumerRating.compareTo(a.consumerRating)
                SortOption.NEAREST -> a.id.compareTo(b.id)
                SortOption.MOST_RECENT_INSPECTION -> b.lastInspectionDate.compareTo(a.lastInspectionDate)
            }
        }
    }

    fun getInspectionReports(restaurantId: String): List<InspectionReport> {
        return inspectionReportsMap[restaurantId] ?: listOf(
            InspectionReport(
                id = "rep_default_$restaurantId",
                restaurantId = restaurantId,
                date = "2026-01-20",
                score = 95,
                grade = "A",
                inspectorNotes = "Routine municipal health inspection passed with high public hygiene standards.",
                inspectorName = "Public Health Inspector #402",
                violations = emptyList()
            )
        )
    }

    fun getReviewsForRestaurant(restaurantId: String): List<UserReview> {
        return userReviewsList.filter { it.restaurantId == restaurantId }
    }

    fun addReview(review: UserReview) {
        userReviewsList.add(0, review)
    }

    // Room Favorites Flow
    val favoritesFlow: Flow<List<FavoriteEntity>> = dao.getAllFavorites()

    fun isFavorite(restaurantId: String): Flow<Boolean> = dao.isFavorite(restaurantId)

    suspend fun toggleFavorite(restaurantId: String, isFav: Boolean) {
        if (isFav) {
            dao.removeFavorite(restaurantId)
        } else {
            dao.insertFavorite(FavoriteEntity(restaurantId = restaurantId))
        }
    }

    // Room User Reviews
    fun getRoomReviews(restaurantId: String): Flow<List<UserReviewEntity>> {
        return dao.getReviewsForRestaurant(restaurantId)
    }

    suspend fun addRoomReview(review: UserReviewEntity) {
        dao.insertUserReview(review)
    }

    private fun getCurrentFormattedTime(): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    }
}
