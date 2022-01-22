package de.tolunla.icarus.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tolunla.icarus.databinding.TweetListItemBinding
import de.tolunla.icarus.db.TweetRepository
import de.tolunla.icarus.db.entity.Tweet
import de.tolunla.icarus.view.viewmodel.TweetViewModel
import kotlinx.coroutines.launch

class FeedAdapter(val tweets: List<Tweet>, val tweetViewModel: TweetViewModel) :
    RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    private lateinit var inflater: LayoutInflater

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        inflater = LayoutInflater.from(recyclerView.context)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                Log.d(this@FeedAdapter::class.java.name, "Scrolling")

                (recyclerView.layoutManager as LinearLayoutManager).let {
                    val visibleItems = it.childCount
                    val totalItems = it.itemCount

                    // Scrolling down
                    if (visibleItems + it.findFirstVisibleItemPosition() >= totalItems && dy > 0) {
                        tweetViewModel.viewModelScope.launch {
                            Log.d(this@FeedAdapter::class.java.name, "Loading")
                            tweetViewModel.loadTweetsFrom(tweets.last().id, TweetRepository.Order.BACKWARDS, 20)
                        }
                    }
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TweetListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val tweet = tweets[position]

        binding.name.text = tweet.user.name
        binding.username.text = tweet.user.username
        binding.body.text = tweet.text
    }

    override fun getItemCount() = tweets.size

    inner class ViewHolder(val binding: TweetListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}