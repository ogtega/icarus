package de.tolunla.icarus.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.tolunla.icarus.db.TweetRepository
import de.tolunla.icarus.db.entity.Tweet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TweetViewModel @Inject constructor(private val repo: TweetRepository) : ViewModel() {
    val tweets: MutableLiveData<List<Tweet>> = MutableLiveData()

    suspend fun loadTweets(
        order: TweetRepository.Order = TweetRepository.Order.BACKWARDS,
        count: Int = 20
    ) {
        tweets.value = repo.getTweets(null, order, count)
    }

    suspend fun loadTweetsFrom(
        id: Long,
        order: TweetRepository.Order = TweetRepository.Order.BACKWARDS,
        count: Int = 20
    ) {
        tweets.value = repo.getTweets(id, order, count)
    }

    suspend fun getLatest() = repo.getLatest()

    fun insert(tweet: Tweet) = viewModelScope.launch(Dispatchers.IO) {
        repo.insert(tweet)
    }
}