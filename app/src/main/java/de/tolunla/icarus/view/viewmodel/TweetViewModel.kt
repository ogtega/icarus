package de.tolunla.icarus.view.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
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
    val ready: MutableLiveData<Boolean> = MutableLiveData(true)
    val tweetLivedata: MutableLiveData<List<Tweet>> = MutableLiveData()

    suspend fun getOlderTweets(from: Long?, count: Int = 20): MutableLiveData<List<Tweet>> {
        ready.value = false
        tweetLivedata.value = repo.getOlderTweets(from, count).toMutableList()
        return tweetLivedata
    }

    suspend fun getNewerTweets(count: Int = 20): MutableLiveData<List<Tweet>> {
        ready.value = false
        tweetLivedata.value = repo.getNewerTweets(count)
        return tweetLivedata
    }

    fun insert(tweet: Tweet) = viewModelScope.launch(Dispatchers.IO) {
        repo.insert(listOf(tweet))
    }
}