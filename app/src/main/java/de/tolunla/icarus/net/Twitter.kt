package de.tolunla.icarus.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Twitter @Inject constructor(private val client: OkHttpClient) {

    suspend fun getFeed(sinceId: Int? = null, maxId: Int? = null, count: Int = 200): String? {
        val url = "https://api.twitter.com/1.1/statuses/home_timeline.json".toHttpUrl()
            .newBuilder()
            .addQueryParameter("count", count.toString())

        sinceId?.let { url.addQueryParameter("since_id", it.toString()) }
        maxId?.let { url.addQueryParameter("max_id", (it - 1).toString()) }

        return Request.Builder().get(url.build())
    }

    suspend fun getUserProfile(pinned_tweets: Boolean = true): String? {
        val url = "https://api.twitter.com/2/users/me".toHttpUrl()
            .newBuilder()

        if (pinned_tweets) {
            url.addQueryParameter("expansions", "pinned_tweet_id")
        }

        return Request.Builder().get(url.build())
    }

    private suspend fun Request.Builder.post(url: HttpUrl, reqBody: RequestBody): String? {
        val req = this.url(url).post(reqBody).build()

        val body = withContext(Dispatchers.IO) {
            client.newCall(req).execute().body?.string()
        }

        return body
    }

    private suspend fun Request.Builder.get(url: HttpUrl): String? {
        val req = this.url(url).build()
        val body = withContext(Dispatchers.IO) {
            client.newCall(req).execute().body?.string()
        }

        return body
    }
}