package de.tolunla.icarus.view.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
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

            binding.profileImg.load(it.user.profileImage) {
                transformations(CircleCropTransformation())
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