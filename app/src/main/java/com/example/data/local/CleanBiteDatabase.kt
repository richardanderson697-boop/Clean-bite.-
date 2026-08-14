package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class, UserReviewEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CleanBiteDatabase : RoomDatabase() {
    abstract fun cleanBiteDao(): CleanBiteDao

    companion object {
        @Volatile
        private var INSTANCE: CleanBiteDatabase? = null

        fun getDatabase(context: Context): CleanBiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CleanBiteDatabase::class.java,
                    "cleanbite_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
