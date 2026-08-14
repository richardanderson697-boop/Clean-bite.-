package com.example.data.model

data class Restaurant(
    val id: String,
    val name: String,
    val address: String,
    val neighborhood: String,
    val cuisine: String,
    val priceRange: String, // "$", "$$", "$$$", "$$$$"
    val latitude: Double,
    val longitude: Double,
    val healthGrade: String, // "A", "B", "C", "Critical"
    val healthScore: Int, // 0 - 100
    val lastInspectionDate: String,
    val criticalViolationsCount: Int,
    val nonCriticalViolationsCount: Int,
    val consumerRating: Float, // e.g. 4.7f
    val reviewCount: Int,
    val imageUrl: String,
    val isPromoted: Boolean = false,
    val phone: String = "(555) 019-2831",
    val hours: String = "11:00 AM - 10:00 PM",
    val description: String = "Popular local dining establishment subject to regular municipal public health department inspections.",
    val city: String = "San Francisco",
    val municipalitySource: String = "Department of Public Health",
    val violationCodes: List<String> = emptyList(),
    val googlePlaceId: String? = null,
    val googleRating: Float? = null,
    val googleUserRatingsTotal: Int? = null,
    val googleMapsUrl: String? = null,
    val isGoogleMatched: Boolean = true,
    val isOpenNow: Boolean = true
)

enum class HealthGrade(val label: String, val minScore: Int) {
    GRADE_A("Grade A", 90),
    GRADE_B("Grade B", 80),
    GRADE_C("Grade C", 70),
    CRITICAL("Needs Action", 0)
}
