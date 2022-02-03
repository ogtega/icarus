package de.tolunla.icarus.view

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import de.tolunla.icarus.databinding.FragmentMainBinding
import de.tolunla.icarus.databinding.NewTweetChipBinding
import de.tolunla.icarus.db.dao.TweetDao
import de.tolunla.icarus.net.TweetRemoteMediator
import de.tolunla.icarus.net.Twitter
import de.tolunla.icarus.view.adapter.FeedAdapter
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalPagingApi
class FeedFragment : Fragment() {

    @Inject
    lateinit var twitter: Twitter

    @Inject
    lateinit var tweetDao: TweetDao
    private lateinit var binding: FragmentMainBinding
    private lateinit var newTweetBinding: NewTweetChipBinding

    private val feedAdapter = FeedAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var tweetCount = 0
        binding = FragmentMainBinding.inflate(inflater)
        newTweetBinding = NewTweetChipBinding.inflate(inflater)

        val pager = Pager(
            config = PagingConfig(pageSize = 100, prefetchDistance = 0),
            remoteMediator = TweetRemoteMediator(tweetDao = tweetDao, twitter)
        ) {
            tweetDao.pagingSource()
        }

        val newPopupWindow = PopupWindow(
            newTweetBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        pager.flow.asLiveData().observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                Log.i(this::class.java.name, "Submitted: ${feedAdapter.itemCount}")
                tweetCount = feedAdapter.itemCount
                feedAdapter.submitData(it)
            }
        }

        feedAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner) { loadStates ->
            if (loadStates.mediator?.refresh is LoadState.NotLoading) {
                binding.swipeRefresh.isRefreshing = false
            }

            // Reached before the pager submits the paging data to the adapter
            if (loadStates.mediator?.prepend is LoadState.NotLoading && tweetCount != 0) {
                if (tweetCount < feedAdapter.itemCount) {
                    newPopupWindow.showAtLocation(
                        binding.root,
                        Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                        0,
                        binding.swipeRefresh.let { it.progressCircleDiameter + it.progressViewEndOffset })
                }
            }
        }

        binding.feedList.adapter = feedAdapter

        binding.feedList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                (recyclerView.layoutManager as LinearLayoutManager).let {
                    if (it.findFirstCompletelyVisibleItemPosition() == 0) {
                        newPopupWindow.dismiss()
                    }
                }
            }
        })

        newTweetBinding.root.setOnClickListener {
            binding.feedList.scrollToPosition(0)
        }

        binding.swipeRefresh.setOnRefreshListener {
            feedAdapter.refresh()
        }

        return binding.root
    }
}