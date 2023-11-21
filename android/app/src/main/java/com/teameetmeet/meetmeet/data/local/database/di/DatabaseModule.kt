package com.teameetmeet.meetmeet.data.local.database.di

import android.content.Context
import androidx.room.Room
import com.teameetmeet.meetmeet.data.local.database.AppDatabase
import com.teameetmeet.meetmeet.data.local.database.dao.EventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDao(database: AppDatabase): EventDao {
        return database.eventEntityDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room
            .databaseBuilder(context, AppDatabase::class.java, "meetmeet-local.db")
            .build()
    }
}