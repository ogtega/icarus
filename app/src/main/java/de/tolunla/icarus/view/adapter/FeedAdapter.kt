package de.tolunla.icarus.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.tolunla.icarus.databinding.TweetListItemBinding
import de.tolunla.icarus.db.entity.Tweet

class FeedAdapter :
    PagingDataAdapter<Tweet, FeedAdapter.ViewHolder>(TweetComparator) {

    private lateinit var inflater: LayoutInflater

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        inflater = LayoutInflater.from(recyclerView.context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TweetListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val tweet = getItem(position)

        tweet?.let {
            binding.name.text = tweet.user.name
            binding.username.text = tweet.user.username
            binding.body.text = tweet.text
            binding.age.text = tweet.createdAt
        }
    }

    inner class ViewHolder(val binding: TweetListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    object TweetComparator : DiffUtil.ItemCallback<Tweet>() {
        override fun areItemsTheSame(oldItem: Tweet, newItem: Tweet): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tweet, newItem: Tweet): Boolean {
            return oldItem == newItem
        }
    }
}