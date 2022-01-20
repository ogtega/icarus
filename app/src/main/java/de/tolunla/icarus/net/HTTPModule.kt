package de.tolunla.icarus.net

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HTTPModule {

    @Provides
    @Singleton
    fun provideCache(ctx: Application): Cache {
        val cacheSize = 10 * 1024 * 1024L
        return Cache(ctx.cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    fun provideClient(cache: Cache, authInterceptor: AuthorizationInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(authInterceptor)
            .build()
    }
}
