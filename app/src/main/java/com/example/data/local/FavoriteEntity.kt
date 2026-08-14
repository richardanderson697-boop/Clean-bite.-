package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val restaurantId: String,
    val savedAtTimestamp: Long = System.currentTimeMillis(),
    val customNote: String = ""
)
