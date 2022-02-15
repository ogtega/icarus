package de.tolunla.icarus.net

import android.app.Application
import coil.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import javax.inject.Singleton
import kotlin.math.pow

@Module
@InstallIn(SingletonComponent::class)
object HTTPModule {

    @Provides
    fun provideCache(ctx: Application): Cache {
        val cacheSize = 10 * 2.0.pow(20.0)
        return Cache(ctx.cacheDir, cacheSize.toLong())
    }

    @Provides
    @Singleton
    fun provideClient(cache: Cache, authInterceptor: AuthorizationInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideImageLoader(ctx: Application, client: OkHttpClient): ImageLoader {
        return ImageLoader.Builder(ctx).okHttpClient(client).build()
    }
}
