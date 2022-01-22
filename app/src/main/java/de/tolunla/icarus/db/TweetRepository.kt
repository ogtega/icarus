package de.tolunla.icarus.db

import android.util.Log
import de.tolunla.icarus.db.dao.TweetDao
import de.tolunla.icarus.db.entity.Tweet
import de.tolunla.icarus.net.Twitter
import javax.inject.Inject

// TODO: Fix looping of requests
class TweetRepository @Inject constructor(
    private val twitter: Twitter,
    private val tweetDao: TweetDao
) {

    suspend fun getOlderTweets(from: Long?, count: Int = 20): List<Tweet> {
        Log.d(this::class.java.name, "getOlderTweets($from, $count)")
        val tweets = tweetDao.getOlder(from ?: getLatest(), count).toMutableList()

        Log.d(this::class.java.name, "Repo ${tweets.size}: ${tweets.map { it.id }}")

        if (tweets.size < count) {
            val fetched = twitter.getFeed(maxId = tweets.lastOrNull()?.id ?: getOldest(), count = count)
            insert(fetched)
            return tweets + fetched
        }

        return tweets
    }

    suspend fun getNewerTweets(count: Int = 20): List<Tweet> {
        Log.d(this::class.java.name, "getNewerTweets($count)")
        val fetched = twitter.getFeed(sinceId = null, count = count)
        insert(fetched)
        return fetched
    }

    private suspend fun getLatest(): Long {
        return tweetDao.getLatest()
    }

    private suspend fun getOldest(): Long {
        return tweetDao.getLatest()
    }

    suspend fun insert(tweets: List<Tweet>) {
        tweetDao.insertAll(tweets)
    }
}