package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CityFeed
import com.example.data.model.DataFeedStatus
import com.example.data.model.Restaurant
import com.example.ui.theme.GoldStar
import com.example.ui.theme.GradeAColor
import com.example.ui.theme.GradeBColor
import com.example.ui.theme.GradeCColor
import com.example.ui.theme.GradeCriticalColor
import com.example.ui.theme.ProGold
import com.example.util.UserLocationState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InteractiveMapCanvas(
    restaurants: List<Restaurant>,
    selectedRestaurant: Restaurant?,
    onSelectRestaurant: (Restaurant?) -> Unit,
    onOpenDetails: (Restaurant) -> Unit,
    onOpenPro: () -> Unit,
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
    var scale by remember { mutableFloatStateOf(1.2f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Reset pan & zoom whenever active city changes
    LaunchedEffect(currentCity) {
        scale = currentCity.defaultZoom
        offsetX = 0f
        offsetY = 0f
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    // Map theme colors
    val mapBgColor = if (isDark) Color(0xFF131A17) else Color(0xFFE8F2EC)
    val parkColor = if (isDark) Color(0xFF1B2B23) else Color(0xFFC7E6D5)
    val waterColor = if (isDark) Color(0xFF102533) else Color(0xFFB5E0F7)
    val roadColor = if (isDark) Color(0xFF232D28) else Color(0xFFFFFFFF)
    val roadBorderColor = if (isDark) Color(0xFF1A221E) else Color(0xFFD4E3DA)

    val centerLat = currentCity.centerLat
    val centerLng = currentCity.centerLng

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("interactive_map_canvas")
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.6f, 3.5f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    onSelectRestaurant(null)
                }
            }
    ) {
        // Map Canvas Drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val cx = canvasWidth / 2f + offsetX
            val cy = canvasHeight / 2f + offsetY

            // 1. Map Base Surface
            drawRect(color = mapBgColor)

            // 2. Water / Bay area
            val waterPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(canvasWidth * 1.5f, 0f)
                lineTo(canvasWidth * 1.5f, canvasHeight * 0.22f * scale + cy * 0.1f)
                cubicTo(
                    canvasWidth * 0.8f * scale + cx, canvasHeight * 0.28f * scale + cy,
                    canvasWidth * 0.4f * scale + cx, canvasHeight * 0.15f * scale + cy,
                    0f, canvasHeight * 0.25f * scale + cy * 0.1f
                )
                close()
            }
            drawPath(path = waterPath, color = waterColor)

            // 3. City Parks
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(cx - 300f * scale, cy - 80f * scale),
                size = Size(280f * scale, 120f * scale),
                cornerRadius = CornerRadius(24f, 24f)
            )

            drawRoundRect(
                color = parkColor,
                topLeft = Offset(cx + 80f * scale, cy + 180f * scale),
                size = Size(180f * scale, 140f * scale),
                cornerRadius = CornerRadius(20f, 20f)
            )

            // 4. City Road Grids
            val roadWidth = 20f * scale
            val mainRoadWidth = 32f * scale

            for (i in -4..5) {
                val rx = cx + (i * 120f * scale)
                drawLine(
                    color = roadBorderColor,
                    start = Offset(rx, 0f),
                    end = Offset(rx, canvasHeight),
                    strokeWidth = roadWidth + 4f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(rx, 0f),
                    end = Offset(rx, canvasHeight),
                    strokeWidth = roadWidth
                )
            }

            for (j in -4..5) {
                val ry = cy + (j * 110f * scale)
                val w = if (j == 0) mainRoadWidth else roadWidth
                drawLine(
                    color = roadBorderColor,
                    start = Offset(0f, ry),
                    end = Offset(canvasWidth, ry),
                    strokeWidth = w + 4f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(0f, ry),
                    end = Offset(canvasWidth, ry),
                    strokeWidth = w
                )
            }

            // 5. GPS User Location Pin & Direction Vector
            val userDx = ((locationState.longitude - centerLng) * 12000f * scale).toFloat()
            val userDy = ((centerLat - locationState.latitude) * 12000f * scale).toFloat()
            val userX = cx + userDx
            val userY = cy + userDy

            // Pulsing accuracy halo
            drawCircle(color = Color(0x333B82F6), radius = 34f * scale, center = Offset(userX, userY))
            drawCircle(color = Color(0xFF3B82F6), radius = 14f * scale, center = Offset(userX, userY))
            drawCircle(color = Color.White, radius = 6f * scale, center = Offset(userX, userY))

            // Directional heading arrow if moving or bearing available
            if (locationState.bearing != 0f || locationState.speedMph > 1f) {
                val rad = Math.toRadians((locationState.bearing - 90).toDouble())
                val tipX = userX + (28f * scale * cos(rad)).toFloat()
                val tipY = userY + (28f * scale * sin(rad)).toFloat()
                drawLine(
                    color = Color(0xFF2563EB),
                    start = Offset(userX, userY),
                    end = Offset(tipX, tipY),
                    strokeWidth = 5f * scale
                )
            }

            // 6. Restaurant Health Pins
            restaurants.forEach { restaurant ->
                val dx = ((restaurant.longitude - centerLng) * 12000f * scale).toFloat()
                val dy = ((centerLat - restaurant.latitude) * 12000f * scale).toFloat()

                val px = cx + dx
                val py = cy + dy

                val isSelected = selectedRestaurant?.id == restaurant.id

                val pinColor = when (restaurant.healthGrade.uppercase()) {
                    "A" -> GradeAColor
                    "B" -> GradeBColor
                    "C" -> GradeCColor
                    else -> GradeCriticalColor
                }

                val pinRadius = if (isSelected) 24f * scale else 18f * scale

                drawCircle(
                    color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.3f),
                    radius = pinRadius + 4f,
                    center = Offset(px, py)
                )

                drawCircle(
                    color = pinColor,
                    radius = pinRadius,
                    center = Offset(px, py)
                )

                drawCircle(
                    color = Color.White,
                    radius = pinRadius * 0.4f,
                    center = Offset(px, py)
                )
            }
        }

        // Overlay Interactive Pins Tap Detection Layer
        restaurants.forEach { restaurant ->
            val cx = (this.constraints.maxWidth / 2f) + offsetX
            val cy = (this.constraints.maxHeight / 2f) + offsetY

            val dx = ((restaurant.longitude - centerLng) * 12000f * scale).toFloat()
            val dy = ((centerLat - restaurant.latitude) * 12000f * scale).toFloat()

            val px = cx + dx
            val py = cy + dy

            val isSelected = selectedRestaurant?.id == restaurant.id

            Box(
                modifier = Modifier
                    .size(if (isSelected) 60.dp else 44.dp)
                    .align(Alignment.TopStart)
                    .padding(0.dp)
                    .graphicsOffset(px - if (isSelected) 30f else 22f, py - if (isSelected) 30f else 22f)
                    .clip(CircleShape)
                    .clickable {
                        onSelectRestaurant(restaurant)
                    }
            )
        }

        // Top Floating Municipal Live Feed & Driving Mode HUD
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = 126.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Driving HUD (if active)
            AnimatedVisibility(
                visible = isDrivingModeActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    contentColor = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "DRIVING HUD ACTIVE • AUTO FETCHING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = "Speed: ${Math.round(locationState.speedMph)} MPH • Nearest: ${locationState.nearestCityFeed.cityName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A)
                        ) {
                            Text(
                                text = "GPS ON",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22C55E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Live Feed Indicator Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (feedStatus.isLiveConnected) Color(0xFF22C55E) else Color(0xFFF59E0B))
                        )
                        Text(
                            text = if (feedStatus.isLoading) "Syncing ${currentCity.cityName} Feed..." else "${currentCity.cityName}: ${restaurants.size} Real Inspections",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "• ${currentCity.municipalitySource.take(18)}...",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onRefreshFeed,
                        modifier = Modifier.size(24.dp)
                    ) {
                        if (feedStatus.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Live Feed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Quick Nationwide City Switcher Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CityFeed.values()) { city ->
                    val isCurrent = city == currentCity
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        contentColor = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        shadowElevation = if (isCurrent) 4.dp else 2.dp,
                        modifier = Modifier.clickable { onSelectCity(city) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${city.cityName} (${city.stateCode})",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Map Control Floating Buttons (Driving Mode, Locate Me, Zoom +, Zoom -, Reset, Pro Radar)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 190.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Driving HUD Mode Toggle
            FloatingActionButton(
                onClick = onToggleDrivingMode,
                modifier = Modifier.size(42.dp),
                containerColor = if (isDrivingModeActive) Color(0xFF2563EB) else MaterialTheme.colorScheme.surface,
                contentColor = if (isDrivingModeActive) Color.White else MaterialTheme.colorScheme.onSurface
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Driving HUD Mode",
                    tint = if (isDrivingModeActive) Color.White else MaterialTheme.colorScheme.primary
                )
            }

            // Locate Me / Auto-Fetch GPS Jurisdiction
            FloatingActionButton(
                onClick = onLocateMe,
                modifier = Modifier.size(42.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.GpsFixed, contentDescription = "Auto Locate & Fetch")
            }

            FloatingActionButton(
                onClick = { scale = (scale * 1.25f).coerceAtMost(3.5f) },
                modifier = Modifier.size(42.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In")
            }

            FloatingActionButton(
                onClick = { scale = (scale / 1.25f).coerceAtLeast(0.6f) },
                modifier = Modifier.size(42.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            FloatingActionButton(
                onClick = {
                    scale = currentCity.defaultZoom
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier.size(42.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Center City")
            }

            FloatingActionButton(
                onClick = onOpenPro,
                modifier = Modifier.size(42.dp),
                containerColor = ProGold,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = "CleanBite Pro Pass")
            }
        }

        // Selected Restaurant Popup Card at Bottom of Map
        selectedRestaurant?.let { restaurant ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .clickable { onOpenDetails(restaurant) }
                    .testTag("map_selected_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(restaurant.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HealthGradeBadge(
                                grade = restaurant.healthGrade,
                                score = restaurant.healthScore,
                                showScore = true
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = GoldStar,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${restaurant.googleRating ?: restaurant.consumerRating}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (restaurant.isGoogleMatched) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "Google",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${restaurant.cuisine} • ${restaurant.priceRange} • ${restaurant.address}",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { onOpenDetails(restaurant) },
                        modifier = Modifier.testTag("view_details_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// Helper Modifier extension for canvas pin offsets
private fun Modifier.graphicsOffset(x: Float, y: Float): Modifier = this.then(
    Modifier.padding(
        start = x.coerceAtLeast(0f).dp,
        top = y.coerceAtLeast(0f).dp
    )
)
