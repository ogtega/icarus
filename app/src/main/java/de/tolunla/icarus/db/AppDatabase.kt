package de.tolunla.icarus.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.tolunla.icarus.db.dao.TweetDao
import de.tolunla.icarus.db.entity.Tweet

@Database(entities = [Tweet::class], version = 1)
@TypeConverters(EntityConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tweetDao(): TweetDao
}