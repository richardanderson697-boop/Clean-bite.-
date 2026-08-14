package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_reviews")
data class UserReviewEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val authorName: String,
    val rating: Float,
    val date: String,
    val comment: String,
    val sentimentTag: String,
    val userCleanlinessRating: Int
)
