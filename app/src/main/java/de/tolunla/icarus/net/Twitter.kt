package de.tolunla.icarus.net

import android.util.Log
import de.tolunla.icarus.db.entity.Tweet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Twitter @Inject constructor(private val client: OkHttpClient) {

    private val jsonFormat = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun getFeed(sinceId: Long? = null, maxId: Long? = null, count: Int = 20): List<Tweet> {
        val url = "https://api.twitter.com/1.1/statuses/home_timeline.json".toHttpUrl()
            .newBuilder()
            .addQueryParameter("count", count.toString())

        sinceId?.let { url.addQueryParameter("since_id", it.toString()) }
        maxId?.let { url.addQueryParameter("max_id", (it - 1).toString()) }

        try {
            return Request.Builder().get(url.build())
                .let { jsonFormat.decodeFromString(it ?: "{}") }
        } catch (e: Exception) {
            Log.d(this::class.java.name, e.message ?: "")
        }

        return listOf()
    }

    suspend fun getUserProfile(pinned_tweets: Boolean = true): String? {
        val url = "https://api.twitter.com/2/users/me".toHttpUrl()
            .newBuilder()

        if (pinned_tweets) {
            url.addQueryParameter("expansions", "pinned_tweet_id")
        }

        return Request.Builder().get(url.build())
    }

    suspend fun postTweet(status: String): String? {
        val url = "https://api.twitter.com/1.1/statuses/update.json".toHttpUrl()
            .newBuilder()

        url.addQueryParameter("status", status)

        return Request.Builder().post(url.build(), FormBody.Builder().build())
    }

    private suspend fun Request.Builder.post(url: HttpUrl, reqBody: RequestBody): String? {
        val req = this.url(url).post(reqBody).build()

        return withContext(Dispatchers.IO) {
            val res = client.newCall(req).execute()
            if (res.code != 200) {
                res.body?.close()
                return@withContext null
            }

            val bodyText = res.body?.string()

            res.body?.close()

            bodyText
        }
    }

    private suspend fun Request.Builder.get(url: HttpUrl): String? {
        val req = this.url(url).build()

        return withContext(Dispatchers.IO) {
            val res = client.newCall(req).execute()
            if (res.code != 200) {
                res.body?.close()
                return@withContext null
            }

            val bodyText = res.body?.string()
            res.body?.close()

            bodyText
        }
    }
}