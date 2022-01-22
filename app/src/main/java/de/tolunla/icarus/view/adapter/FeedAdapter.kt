package de.tolunla.icarus.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.tolunla.icarus.databinding.TweetListItemBinding
import de.tolunla.icarus.db.entity.Tweet

class FeedAdapter(val tweets: MutableList<Tweet>) :
    RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

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
        val tweet = tweets[position]

        binding.name.text = tweet.user.name
        binding.username.text = tweet.user.username
        binding.body.text = tweet.text
    }

    override fun getItemCount() = tweets.size

    inner class ViewHolder(val binding: TweetListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}