package com.example.data.model

data class FilterOptions(
    val searchQuery: String = "",
    val selectedCuisine: String? = null,
    val selectedPrice: String? = null, // "$", "$$", "$$$", "$$$$"
    val selectedGrade: String? = null, // "A", "B", "C", "Critical"
    val onlyImprovingTrend: Boolean = false,
    val zeroCriticalViolations: Boolean = false,
    val inspectedInLast30Days: Boolean = false,
    val sortBy: SortOption = SortOption.HEALTH_SCORE_DESC
)

enum class SortOption(val displayName: String) {
    HEALTH_SCORE_DESC("Highest Health Score"),
    CONSUMER_RATING_DESC("Top Consumer Rating"),
    NEAREST("Nearest Distance"),
    MOST_RECENT_INSPECTION("Latest Inspection")
}
