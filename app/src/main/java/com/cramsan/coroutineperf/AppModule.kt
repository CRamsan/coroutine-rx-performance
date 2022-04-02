package com.cramsan.coroutineperf

import android.content.Context
import androidx.room.Room
import com.cramsan.coroutineperf.api.ReqbinService
import com.cramsan.coroutineperf.storage.AppDatabase
import com.cramsan.coroutineperf.storage.ResponseDao
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .cache(null)
        .build()

    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://reqbin.com/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    @Provides
    fun provideApiService(
        retrofit: Retrofit,
    ): ReqbinService = retrofit.create(ReqbinService::class.java)

    @Provides
    fun provideDatabase(
        @ApplicationContext appContext: Context,
    ): AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "database-name"
    ).build()

    @Provides
    fun provideDao(
        appDatabase: AppDatabase,
    ): ResponseDao = appDatabase.dao()

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    fun provideIoScheduler(): Scheduler = Schedulers.io()
}