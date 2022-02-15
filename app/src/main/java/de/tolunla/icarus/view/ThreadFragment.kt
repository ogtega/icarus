package de.tolunla.icarus.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import dagger.hilt.android.AndroidEntryPoint
import de.tolunla.icarus.databinding.FragmentTweetThreadBinding
import de.tolunla.icarus.db.dao.TweetDao
import javax.inject.Inject

@AndroidEntryPoint
class ThreadFragment : Fragment() {
    @Inject
    lateinit var tweetDao: TweetDao

    @Inject
    lateinit var imageLoader: ImageLoader

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
                    val photos = mediaList.filter { media -> media.type == "photo" }

                    if (photos.isNotEmpty()) {
                        binding.imageGrid.visibility = View.VISIBLE
                    }

                    photos.forEachIndexed { index, tweetMedia ->
                        val imageView = ImageView(binding.root.context)

                        val request = ImageRequest.Builder(binding.root.context)
                            .data(tweetMedia.url)
                            .target(imageView)
                            .build()

                        imageLoader.enqueue(request)

                        val layoutParams = GridLayout.LayoutParams(
                            ViewGroup.LayoutParams(
                                binding.imageGrid.width.div(2),
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )

                        layoutParams.columnSpec =
                            GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                        imageView.layoutParams = layoutParams
                        imageView.adjustViewBounds = true
                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                        binding.imageGrid.addView(imageView, index)
                    }
                }
            }
        }
        return binding.root
    }
}