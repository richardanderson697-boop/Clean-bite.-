package com.example.data.model

enum class CityFeed(
    val cityName: String,
    val stateCode: String,
    val municipalitySource: String,
    val domain: String,
    val resourceId: String,
    val centerLat: Double,
    val centerLng: Double,
    val defaultZoom: Float = 1.2f,
    val isRealTimeLive: Boolean = true
) {
    SAN_FRANCISCO(
        cityName = "San Francisco",
        stateCode = "CA",
        municipalitySource = "San Francisco Dept. of Public Health (SFDPH)",
        domain = "data.sfgov.org",
        resourceId = "pyih-qa8i.json",
        centerLat = 37.7749,
        centerLng = -122.4194
    ),
    NEW_YORK(
        cityName = "New York City",
        stateCode = "NY",
        municipalitySource = "NYC Dept. of Health & Mental Hygiene (DOHMH)",
        domain = "data.cityofnewyork.us",
        resourceId = "43nn-pn8j.json",
        centerLat = 40.7128,
        centerLng = -74.0060
    ),
    CHICAGO(
        cityName = "Chicago",
        stateCode = "IL",
        municipalitySource = "Chicago Dept. of Public Health (CDPH)",
        domain = "data.cityofchicago.org",
        resourceId = "4ijn-t7nx.json",
        centerLat = 41.8781,
        centerLng = -87.6298
    ),
    AUSTIN(
        cityName = "Austin",
        stateCode = "TX",
        municipalitySource = "Austin Public Health (APH)",
        domain = "data.austintexas.gov",
        resourceId = "ecmv-9xxi.json",
        centerLat = 30.2672,
        centerLng = -97.7431
    ),
    SEATTLE(
        cityName = "Seattle & King County",
        stateCode = "WA",
        municipalitySource = "Seattle & King County Public Health",
        domain = "data.kingcounty.gov",
        resourceId = "f29t-992v.json",
        centerLat = 47.6062,
        centerLng = -122.3321
    )
}

data class DataFeedStatus(
    val activeCity: CityFeed = CityFeed.SAN_FRANCISCO,
    val isLoading: Boolean = false,
    val isLiveConnected: Boolean = true,
    val lastSyncTime: String = "Just now",
    val recordCount: Int = 0,
    val errorMessage: String? = null
)
