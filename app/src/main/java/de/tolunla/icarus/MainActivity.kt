package de.tolunla.icarus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.*
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClientConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    private val TAG: String = this::class.java.name

    private val service = ServiceBuilder(BuildConfig.TWITTER_API_KEY)
        .apiSecret(BuildConfig.TWITTER_API_SECRET)
        .callback("app://icarus.tolunla.de/")
        .httpClientConfig(OkHttpHttpClientConfig.defaultConfig())
        .build(TwitterApi.instance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            dataStoreManager.getTokenData().first().accessToken?.let { accessToken ->
                val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
                    val chainRequest = chain.request()
                    val request = chainRequest.newBuilder()
                    val method = chainRequest.method.uppercase()

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
                        "oauth_token" to accessToken.token,
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
                        accessToken.tokenSecret
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

                    Log.d(TAG, oauthBaseString)
                    Log.d(TAG, oauthHeaderString)

                    chain.proceed(request.build())
                }).build()

                val request = Request.Builder().url(
                    "https://api.twitter.com/1.1/statuses/home_timeline.json".toHttpUrl()
                        .newBuilder()
                        .build()
                ).build()

                val body = withContext(Dispatchers.IO) {
                    client.newCall(request).execute().body?.string()
                }

                Log.d(TAG, body ?: "")
            } ?: run {
                intent.data?.getQueryParameter(OAuthConstants.VERIFIER)?.also { verifier ->
                    val accessToken = withContext(Dispatchers.IO) {
                        service.getAccessToken(
                            dataStoreManager.getTokenData().first().requestToken,
                            verifier
                        )
                    }

                    dataStoreManager.setAccessToken(accessToken.rawResponse)
                } ?: run {
                    val authUrl = withContext(Dispatchers.IO) {
                        val requestToken = service.requestToken
                        dataStoreManager.setRequestToken(requestToken.rawResponse)
                        service.getAuthorizationUrl(requestToken)
                    }

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                    this@MainActivity.startActivity(intent)
                }
            }
        }
    }
}
