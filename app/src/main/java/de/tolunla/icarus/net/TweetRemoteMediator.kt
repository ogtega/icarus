package de.tolunla.icarus.net

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import de.tolunla.icarus.db.dao.TweetDao
import de.tolunla.icarus.db.entity.Tweet
import javax.inject.Inject

@ExperimentalPagingApi
class TweetRemoteMediator @Inject constructor(
    val tweetDao: TweetDao,
    private val twitter: Twitter
) :
    RemoteMediator<Int, Tweet>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, Tweet>): MediatorResult {
        return try {
            val loadId = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> state.firstItemOrNull()?.id
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = false)
                    lastItem.id
                }
            }

            val response = when (loadType) {
                LoadType.REFRESH -> twitter.getFeed(count = state.config.pageSize)
                LoadType.PREPEND -> twitter.getFeed(sinceId = loadId, count = state.config.pageSize)
                LoadType.APPEND -> twitter.getFeed(maxId = loadId, count = state.config.pageSize)
            }

            tweetDao.insertAll(response)

            Log.d(this::class.java.name, "Fetched ${response.size} tweets")

            MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }
}