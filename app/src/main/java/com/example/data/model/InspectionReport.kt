package com.example.data.model

data class InspectionReport(
    val id: String,
    val restaurantId: String,
    val date: String,
    val score: Int,
    val grade: String,
    val inspectorNotes: String,
    val inspectorName: String = "Public Health Inspector #402",
    val violations: List<ViolationItem> = emptyList()
)

data class ViolationItem(
    val code: String,
    val description: String,
    val isCritical: Boolean,
    val isCorrectedOnSite: Boolean = true
)
