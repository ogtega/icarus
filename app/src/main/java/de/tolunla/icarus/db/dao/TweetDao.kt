package de.tolunla.icarus.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import de.tolunla.icarus.db.entity.Tweet

@Dao
interface TweetDao {

    @Query("SELECT * FROM tweet WHERE id < :id ORDER BY id DESC LIMIT :count")
    suspend fun getOlder(id: Long, count: Int): List<Tweet>

    @Query("SELECT * FROM tweet WHERE id > :id ORDER BY id ASC  LIMIT :count")
    suspend fun getNewer(id: Long, count: Int): List<Tweet>

    @Query("SELECT * FROM tweet WHERE id = :id")
    fun getTweet(id: Long): LiveData<Tweet>

    @Query("SELECT MAX(id) FROM tweet")
    suspend fun getLatest(): Long

    @Query("SELECT MIN(id) FROM tweet")
    suspend fun getOldest(): Long

    @Query("SELECT * FROM tweet ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, Tweet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tweets: List<Tweet>)

    @Delete
    suspend fun delete(tweet: Tweet)
}