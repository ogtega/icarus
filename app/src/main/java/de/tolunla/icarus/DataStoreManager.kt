package de.tolunla.icarus

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.github.scribejava.core.model.OAuth1AccessToken
import com.github.scribejava.core.model.OAuth1RequestToken
import com.github.scribejava.core.model.OAuthConstants
import com.github.scribejava.core.model.Token
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(
    name = "shared_preferences",
)

data class TokenData(val accessToken: OAuth1AccessToken?, val requestToken: OAuth1RequestToken?)

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext appContext: Context) {

    private val dataStore = appContext.dataStore

    private object TokenKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REQUEST_TOKEN = stringPreferencesKey("request_token")
    }

    suspend fun setRequestToken(rawResponse: String) {
        dataStore.edit { preferences ->
            preferences[TokenKeys.REQUEST_TOKEN] = rawResponse
        }
    }

    suspend fun setAccessToken(rawResponse: String) {
        dataStore.edit { preferences ->
            preferences[TokenKeys.ACCESS_TOKEN] = rawResponse
        }
    }

    suspend fun fetchInitialPreferences() = mapTokenData(dataStore.data.first().toPreferences())

    fun getTokenData(): Flow<TokenData> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { prefs ->
        mapTokenData(prefs)
    }

    private fun mapTokenData(prefs: Preferences): TokenData {
        val accessToken = prefs[TokenKeys.ACCESS_TOKEN]?.let {
            val token = object : Token(it) {}
            OAuth1AccessToken(
                token.getParameter(OAuthConstants.TOKEN),
                token.getParameter(OAuthConstants.TOKEN_SECRET),
                token.rawResponse
            )
        }

        val requestToken = prefs[TokenKeys.REQUEST_TOKEN]?.let {
            val token = object : Token(it) {}
            OAuth1RequestToken(
                token.getParameter(OAuthConstants.TOKEN),
                token.getParameter(OAuthConstants.TOKEN_SECRET),
                token.rawResponse
            )
        }

        return TokenData(accessToken, requestToken)
    }
}
