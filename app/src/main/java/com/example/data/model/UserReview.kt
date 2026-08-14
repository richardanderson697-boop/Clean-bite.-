package com.example.data.model

data class UserReview(
    val id: String,
    val restaurantId: String,
    val authorName: String,
    val rating: Float,
    val date: String,
    val comment: String,
    val sentimentTag: String = "Cleanliness Verified",
    val userCleanlinessRating: Int = 5 // 1 to 5 scale
)
