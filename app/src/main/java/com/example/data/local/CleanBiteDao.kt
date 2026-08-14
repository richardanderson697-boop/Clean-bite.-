package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CleanBiteDao {
    @Query("SELECT * FROM favorites ORDER BY savedAtTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE restaurantId = :restaurantId)")
    fun isFavorite(restaurantId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE restaurantId = :restaurantId")
    suspend fun removeFavorite(restaurantId: String)

    @Query("SELECT * FROM user_reviews WHERE restaurantId = :restaurantId ORDER BY date DESC")
    fun getReviewsForRestaurant(restaurantId: String): Flow<List<UserReviewEntity>>

    @Query("SELECT * FROM user_reviews ORDER BY date DESC")
    fun getAllUserReviews(): Flow<List<UserReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserReview(review: UserReviewEntity)
}
