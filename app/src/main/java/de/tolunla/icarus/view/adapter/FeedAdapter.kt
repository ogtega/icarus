package de.tolunla.icarus.view.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import de.tolunla.icarus.R
import de.tolunla.icarus.databinding.TweetListItemBinding
import de.tolunla.icarus.db.entity.Tweet
import java.text.SimpleDateFormat
import java.util.*

class FeedAdapter :
    PagingDataAdapter<Tweet, FeedAdapter.ViewHolder>(TweetComparator) {

    private lateinit var inflater: LayoutInflater
    private val dateFormat = SimpleDateFormat("E MMM dd hh:mm:ss Z yyyy", Locale.getDefault())
    private val monthDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val yearDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

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
            binding.name.text = it.user.name
            binding.username.text = "@${it.user.username}"
            binding.body.text = it.text

            binding.root.setOnClickListener { view ->
                val bundle = bundleOf("tweet" to tweet.id)
                view.findNavController()
                    .navigate(R.id.action_home_feed_dst_to_tweet_thread_dst, bundle)
            }

            binding.profileImg.load(it.user.profileImage.replace("normal", "bigger")) {
                transformations(CircleCropTransformation())
            }

            binding.media.visibility = View.GONE

            it.entities?.also { tweetEntities ->
                val mediaList = tweetEntities.media

                if (mediaList.isNotEmpty()) {
                    val media = mediaList[0]
                    if (media.type == "photo") {
                        binding.media.load(media.url) {
                            transformations(RoundedCornersTransformation(0.1f))
                        }

                        binding.media.visibility = View.VISIBLE
                    }
                }
            }

            dateFormat.parse(it.createdAt)?.let { date ->
                val elapsed = (System.currentTimeMillis() - date.time)
                binding.age.text = when {
                    elapsed < DateUtils.SECOND_IN_MILLIS * 6 -> "Just now"
                    elapsed < DateUtils.MINUTE_IN_MILLIS -> "${elapsed / DateUtils.SECOND_IN_MILLIS}m"
                    elapsed < DateUtils.HOUR_IN_MILLIS -> "${elapsed / DateUtils.MINUTE_IN_MILLIS}m"
                    elapsed < DateUtils.DAY_IN_MILLIS -> "${elapsed / DateUtils.HOUR_IN_MILLIS}h"
                    elapsed < DateUtils.DAY_IN_MILLIS * 365.25 -> monthDateFormat.format(date)
                    else -> yearDateFormat.format(date)
                }
            }
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