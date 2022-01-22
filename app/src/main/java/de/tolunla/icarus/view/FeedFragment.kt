package de.tolunla.icarus.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.tolunla.icarus.databinding.FragmentMainBinding
import de.tolunla.icarus.db.TweetRepository
import de.tolunla.icarus.db.entity.Tweet
import de.tolunla.icarus.net.Twitter
import de.tolunla.icarus.view.adapter.FeedAdapter
import de.tolunla.icarus.view.viewmodel.TweetViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var twitter: Twitter

    private val tweetViewModel: TweetViewModel by viewModels()

    private val tweets = mutableListOf<Tweet>()
    private lateinit var feedAdapter: FeedAdapter

    lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)
        feedAdapter = FeedAdapter(tweets, tweetViewModel)
        binding.feedList.adapter = feedAdapter

        tweetViewModel.tweets.observe(viewLifecycleOwner, { updates ->
            val pointer =
                if (updates.lastOrNull()?.id ?: 1 > tweets.lastOrNull()?.id ?: 0) 0 else tweets.lastIndex
            tweets.addAll(pointer, updates)
            feedAdapter.notifyItemRangeChanged(pointer, updates.size)
        })

        lifecycleScope.launch {
            tweetViewModel.loadTweets(count = 30)

            tweetViewModel.loadTweetsFrom(
                tweetViewModel.getLatest(),
                TweetRepository.Order.FORWARD,
                30
            )
        }

        return binding.root
    }
}