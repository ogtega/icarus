package de.tolunla.icarus.db.dao

import androidx.room.*
import de.tolunla.icarus.db.entity.Tweet

@Dao
interface TweetDao {

    @Query("SELECT * FROM tweet WHERE id < :id ORDER BY id DESC LIMIT :count")
    fun getOlder(id: Long, count: Int): List<Tweet>

    @Query("SELECT * FROM tweet WHERE id > :id ORDER BY id ASC LIMIT :count")
    fun getNewer(id: Long, count: Int): List<Tweet>

    @Query("SELECT MAX(id) FROM tweet")
    fun getLatest(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tweets: List<Tweet>)

    @Delete
    fun delete(tweet: Tweet)
}