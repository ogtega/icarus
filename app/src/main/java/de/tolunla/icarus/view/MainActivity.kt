package de.tolunla.icarus.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuthConstants
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClientConfig
import dagger.hilt.android.AndroidEntryPoint
import de.tolunla.icarus.BuildConfig
import de.tolunla.icarus.DataStoreManager
import de.tolunla.icarus.R
import de.tolunla.icarus.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var client: OkHttpClient

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph

    private val service = ServiceBuilder(BuildConfig.TWITTER_API_KEY)
        .apiSecret(BuildConfig.TWITTER_API_SECRET)
        .callback("app://icarus.tolunla.de/")
        .httpClientConfig(OkHttpHttpClientConfig.defaultConfig())
        .build(TwitterApi.instance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // setContentView to a splash screen

        navController = binding.fragmentContainerView.getFragment<NavHostFragment>().navController
        navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            this.onOptionsItemSelected(item)
        }

        lifecycleScope.launch {
            if (authenticate()) {
                setupNavigation()
                setContentView(binding.root)
            } else {
                finish()
            }
        }
    }

    private fun setupNavigation() {
        navController.graph = navGraph
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.feed_dst) binding.floatingActionButton.show() else binding.floatingActionButton.hide()
        }

        setSupportActionBar(binding.toolbar)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) ||
                super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
}
