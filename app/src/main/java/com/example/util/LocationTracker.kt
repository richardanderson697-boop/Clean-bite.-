package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.CityFeed
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class UserLocationState(
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val accuracy: Float = 10f,
    val speedMph: Float = 0f,
    val bearing: Float = 0f,
    val isTracking: Boolean = false,
    val hasPermission: Boolean = false,
    val nearestCityFeed: CityFeed = CityFeed.SAN_FRANCISCO,
    val distanceToNearestCityMiles: Double = 0.0
)

class LocationTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationState = MutableStateFlow(
        UserLocationState(
            hasPermission = hasLocationPermission()
        )
    )
    val locationState: StateFlow<UserLocationState> = _locationState.asStateFlow()

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    fun startContinuousTracking(): Flow<UserLocationState> = callbackFlow {
        if (!hasLocationPermission()) {
            _locationState.value = _locationState.value.copy(hasPermission = false, isTracking = false)
            trySend(_locationState.value)
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L
        ).apply {
            setMinUpdateIntervalMillis(1500L)
            setMinUpdateDistanceMeters(2f)
        }.build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val speedMph = (location.speed * 2.23694f).coerceAtLeast(0f)
                    val nearestCity = findNearestCityFeed(location.latitude, location.longitude)
                    val distanceMiles = calculateDistanceMiles(
                        location.latitude, location.longitude,
                        nearestCity.centerLat, nearestCity.centerLng
                    )

                    val newState = UserLocationState(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        speedMph = speedMph,
                        bearing = location.bearing,
                        isTracking = true,
                        hasPermission = true,
                        nearestCityFeed = nearestCity,
                        distanceToNearestCityMiles = distanceMiles
                    )
                    _locationState.value = newState
                    trySend(newState)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
            // Fetch last known immediately as well
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (lastLoc != null) {
                    val nearestCity = findNearestCityFeed(lastLoc.latitude, lastLoc.longitude)
                    val distanceMiles = calculateDistanceMiles(
                        lastLoc.latitude, lastLoc.longitude,
                        nearestCity.centerLat, nearestCity.centerLng
                    )
                    val state = UserLocationState(
                        latitude = lastLoc.latitude,
                        longitude = lastLoc.longitude,
                        accuracy = lastLoc.accuracy,
                        speedMph = (lastLoc.speed * 2.23694f).coerceAtLeast(0f),
                        bearing = lastLoc.bearing,
                        isTracking = true,
                        hasPermission = true,
                        nearestCityFeed = nearestCity,
                        distanceToNearestCityMiles = distanceMiles
                    )
                    _locationState.value = state
                    trySend(state)
                }
            }
        } catch (e: SecurityException) {
            Log.e("LocationTracker", "SecurityException requesting location: ${e.message}")
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
            _locationState.value = _locationState.value.copy(isTracking = false)
        }
    }

    // Identifies which municipal open data feed applies best to driver's current coordinates
    fun findNearestCityFeed(userLat: Double, userLng: Double): CityFeed {
        var closest = CityFeed.SAN_FRANCISCO
        var minDistance = Double.MAX_VALUE

        for (city in CityFeed.values()) {
            val dist = calculateDistanceMiles(userLat, userLng, city.centerLat, city.centerLng)
            if (dist < minDistance) {
                minDistance = dist
                closest = city
            }
        }
        return closest
    }

    companion object {
        fun calculateDistanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 3958.8 // Radius of earth in miles
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }
    }
}
