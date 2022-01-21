package de.tolunla.icarus.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuthConstants
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClientConfig
import dagger.hilt.android.AndroidEntryPoint
import de.tolunla.icarus.BuildConfig
import de.tolunla.icarus.DataStoreManager
import de.tolunla.icarus.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var client: OkHttpClient

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    lateinit var binding: ActivityMainBinding

    private val TAG: String = this::class.java.name

    private val service = ServiceBuilder(BuildConfig.TWITTER_API_KEY)
        .apiSecret(BuildConfig.TWITTER_API_SECRET)
        .callback("app://icarus.tolunla.de/")
        .httpClientConfig(OkHttpHttpClientConfig.defaultConfig())
        .build(TwitterApi.instance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            if (authenticate()) {
                withContext(Dispatchers.IO) {
                    val request = Request.Builder().url(
                        "https://api.twitter.com/1.1/statuses/home_timeline.json".toHttpUrl()
                            .newBuilder()
                            .build()
                    ).build()

                    val body = withContext(Dispatchers.IO) {
                        client.newCall(request).execute().body?.string()
                    }

                    Log.d(TAG, body ?: "")
                }
            }
        }
    }

    private suspend fun authenticate(): Boolean {
        // May block UI thread, oh well...
        val tokenData = dataStoreManager.getTokenData().first()
        val verifier = intent.data?.getQueryParameter(OAuthConstants.VERIFIER)

        if (tokenData.requestToken != null && verifier != null) {
            val accessToken = withContext(Dispatchers.IO) {
                service.getAccessToken(
                    dataStoreManager.getTokenData().first().requestToken,
                    verifier
                )
            }

            dataStoreManager.setAccessToken(accessToken.rawResponse)
        } else if (tokenData.accessToken == null) {
            val authUrl = withContext(Dispatchers.IO) {
                val requestToken = service.requestToken
                dataStoreManager.setRequestToken(requestToken.rawResponse)
                service.getAuthorizationUrl(requestToken)
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
            startActivity(intent)
            return false
        }

        return tokenData.requestToken != null
    }
}
