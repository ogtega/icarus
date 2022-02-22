package de.tolunla.icarus.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import de.tolunla.icarus.databinding.DialogComposeTweetBinding
import de.tolunla.icarus.db.dao.TweetDao
import de.tolunla.icarus.db.entity.Tweet
import de.tolunla.icarus.net.Twitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class ComposeTweetFragment : BottomSheetDialogFragment() {

    private val jsonFormat = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    lateinit var binding: DialogComposeTweetBinding
    var status = ""

    @Inject
    lateinit var twitter: Twitter

    @Inject
    lateinit var tweetDao: TweetDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogComposeTweetBinding.inflate(inflater)

        binding.composeEdittext.addTextChangedListener { text ->
            status = text.toString()
            binding.sendTweet.isEnabled = status.isNotBlank()
        }

        binding.closeCompose.setOnClickListener {
            dialog?.dismiss()
        }

        binding.sendTweet.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                twitter.postTweet(status)?.let { res ->
                    val tweet = jsonFormat.decodeFromString<Tweet>(res)
                    tweetDao.insertAll(listOf(tweet))
                    dialog?.dismiss()
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialog = dialog as BottomSheetDialog

        dialog.behavior.isFitToContents = false
        dialog.behavior.skipCollapsed = true
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.peekHeight = 0

        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
            it.updateLayoutParams {
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
    }
}