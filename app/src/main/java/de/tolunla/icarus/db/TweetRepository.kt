package de.tolunla.icarus.db

import de.tolunla.icarus.db.dao.TweetDao
import de.tolunla.icarus.db.entity.Tweet
import de.tolunla.icarus.net.Twitter
import javax.inject.Inject

// TODO: Fix looping of requests
class TweetRepository @Inject constructor(
    private val twitter: Twitter,
    private val tweetDao: TweetDao
) {
    enum class Order {
        FORWARD, BACKWARDS
    }

    suspend fun getTweets(
        id: Long? = tweetDao.getLatest(),
        order: Order = Order.BACKWARDS,
        count: Int = 20
    ): List<Tweet> {
        val res = mutableListOf<Tweet>()

        if (id == null) {
            res += twitter.getFeed(count = count)
            tweetDao.insertAll(res)
            return res
        }

        res += when (order) {
            Order.BACKWARDS -> tweetDao.getOlder(id, count)
            Order.FORWARD -> tweetDao.getNewer(id, count)
        }

        if (res.size < count) {
            val updates = when (order) {
                Order.BACKWARDS -> twitter.getFeed(
                    maxId = res.lastOrNull()?.id ?: id,
                    count = count
                )
                Order.FORWARD -> twitter.getFeed(
                    sinceId = res.firstOrNull()?.id ?: id,
                    count = count
                )
            }

            tweetDao.insertAll(updates)

            return if (order == Order.FORWARD) updates + res else res + updates
        }

        return res
    }

    suspend fun getLatest(): Long {
        return tweetDao.getLatest()
    }

    suspend fun insert(tweet: Tweet) {
        tweetDao.insertAll(listOf(tweet))
    }
}