package com.shg25.limimeshi.core.database.di

import android.content.Context
import androidx.room.Room
import com.shg25.limimeshi.core.database.LimimeshiDatabase
import com.shg25.limimeshi.core.database.dao.CampaignDao
import com.shg25.limimeshi.core.database.dao.ChainDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLimimeshiDatabase(
        @ApplicationContext context: Context
    ): LimimeshiDatabase = Room.databaseBuilder(
        context,
        LimimeshiDatabase::class.java,
        "limimeshi.db"
    ).build()

    @Provides
    fun provideChainDao(database: LimimeshiDatabase): ChainDao = database.chainDao()

    @Provides
    fun provideCampaignDao(database: LimimeshiDatabase): CampaignDao = database.campaignDao()
}
