package de.tolunla.icarus.net

import android.net.Uri
import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import de.tolunla.icarus.BuildConfig
import de.tolunla.icarus.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthorizationInterceptor @Inject constructor(private val dataStoreManager: DataStoreManager) :
    Interceptor {

    private val service = ServiceBuilder(BuildConfig.TWITTER_API_KEY)
        .apiSecret(BuildConfig.TWITTER_API_SECRET)
        .callback("app://icarus.tolunla.de/")
        .build(TwitterApi.instance())

    override fun intercept(chain: Interceptor.Chain): Response {
        val chainRequest = chain.request()
        val request = chainRequest.newBuilder()
        val method = chainRequest.method.uppercase()

        val accessToken = runBlocking { dataStoreManager.getTokenData().first().accessToken }

        val url = chainRequest.url.newBuilder().apply {
            chainRequest.url.queryParameterNames.map {
                removeAllEncodedQueryParameters(it)
            }
        }.build().toString()

        val queryParams = (0 until chainRequest.url.querySize).map {
            chainRequest.url.queryParameterName(it) to Uri.encode(
                chainRequest.url.queryParameterValue(
                    it
                )
            )
        }.toMap()

        val oauthAuthHeaders = sortedMapOf<String, String>(
            "oauth_consumer_key" to service.apiKey,
            "oauth_nonce" to service.api.timestampService.nonce,
            "oauth_signature_method" to service.api.signatureService.signatureMethod,
            "oauth_timestamp" to service.api.timestampService.timestampInSeconds,
            "oauth_token" to (accessToken?.token ?: ""),
            "oauth_version" to "1.0"
        )

        val oauthParamString =
            (queryParams + oauthAuthHeaders).toSortedMap()
                .map { "${it.key}=${it.value}" }.joinToString("&")
        val oauthBaseString =
            "$method&${Uri.encode(url)}&${Uri.encode(oauthParamString)}"
        val oauthSignature = service.api.signatureService.getSignature(
            oauthBaseString,
            service.apiSecret,
            (accessToken?.tokenSecret ?: "")
        )

        oauthAuthHeaders["oauth_signature"] = oauthSignature

        val oauthHeaderString = "OAuth ${
            oauthAuthHeaders.map { "${Uri.encode(it.key)}=\"${Uri.encode(it.value)}\"" }
                .joinToString(", ")
        }"

        request.addHeader(
            "Authorization",
            oauthHeaderString
        )

        return chain.proceed(request.build())
    }
}