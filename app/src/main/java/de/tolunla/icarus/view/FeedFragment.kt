package de.tolunla.icarus.view

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.tolunla.icarus.net.Twitter
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var twitter: Twitter

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {

            twitter.getFeed()?.let {
                Log.d(tag, it)
            }
        }
    }
}