package de.tolunla.icarus.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import de.tolunla.icarus.databinding.FragmentMainBinding
import de.tolunla.icarus.net.Twitter
import de.tolunla.icarus.view.adapter.FeedAdapter
import de.tolunla.icarus.view.viewmodel.TweetViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var twitter: Twitter

    private val tweetViewModel: TweetViewModel by viewModels()
    private lateinit var feedAdapter: FeedAdapter

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)
        feedAdapter = FeedAdapter(mutableListOf())
        binding.feedList.adapter = feedAdapter

        lifecycleScope.launch {
            tweetViewModel.getNewerTweets(25).observe(viewLifecycleOwner, { updates ->
                lifecycleScope.launch(Dispatchers.Main) {
                    feedAdapter.tweets.addAll(updates)
                    feedAdapter.notifyItemRangeInserted(0, updates.size)
                }
            })
        }

        binding.feedList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var update = true

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                (recyclerView.layoutManager as LinearLayoutManager).let {
                    val visibleItems = it.childCount
                    val totalItems = it.itemCount

                    // Scrolling down
                    if (visibleItems + it.findFirstVisibleItemPosition() >= totalItems && dy > 0 && update) {
                        update = false
                        tweetViewModel.viewModelScope.launch(Dispatchers.Main) {
                            tweetViewModel.getOlderTweets(
                                feedAdapter.tweets.lastOrNull()?.id,
                                25
                            ).observe(viewLifecycleOwner, { updates ->
                                feedAdapter.tweets.addAll(feedAdapter.tweets.size, updates)
                                feedAdapter.notifyItemRangeInserted(
                                    feedAdapter.tweets.size,
                                    updates.size
                                )
                                update = true
                            })
                        }
                    }
                }
            }
        })

        return binding.root
    }
}