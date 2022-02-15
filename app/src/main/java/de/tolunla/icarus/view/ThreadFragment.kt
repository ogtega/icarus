package de.tolunla.icarus.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import dagger.hilt.android.AndroidEntryPoint
import de.tolunla.icarus.databinding.FragmentTweetThreadBinding
import de.tolunla.icarus.db.dao.TweetDao
import de.tolunla.icarus.net.Twitter
import javax.inject.Inject

@AndroidEntryPoint
class ThreadFragment : Fragment() {
    @Inject
    lateinit var twitter: Twitter

    @Inject
    lateinit var tweetDao: TweetDao

    private lateinit var binding: FragmentTweetThreadBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTweetThreadBinding.inflate(inflater)
        arguments?.getLong("tweet")?.let {
            tweetDao.getTweet(it).observe(viewLifecycleOwner) { tweet ->
                binding.name.text = tweet.user.name
                binding.username.text = "@${tweet.user.username}"
                binding.body.text = tweet.text

                binding.profileImg.load(tweet.user.profileImage.replace("normal", "bigger")) {
                    transformations(CircleCropTransformation())
                }

                tweet.entities?.media?.let { mediaList ->
                    mediaList.forEachIndexed { index, tweetMedia ->
                        val last = index + 1 == mediaList.size
                        val imageView = ImageView(context)
                        binding.imageGrid.addView(imageView, index)
                        imageView.load(tweetMedia.url)
                        val layoutParams = GridLayout.LayoutParams(
                            ViewGroup.LayoutParams(
                                binding.imageGrid.width.div(if (last) 1 else 2),
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )

                        layoutParams.columnSpec =
                            GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                        imageView.layoutParams = layoutParams
                        imageView.adjustViewBounds = true
                        imageView.scaleType = ImageView.ScaleType.FIT_XY
                        Log.d(this::class.java.name, imageView.width.toString())
                    }
                }
            }
        }
        return binding.root
    }
}