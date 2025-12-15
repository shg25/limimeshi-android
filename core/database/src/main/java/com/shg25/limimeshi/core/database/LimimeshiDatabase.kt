package com.shg25.limimeshi.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shg25.limimeshi.core.database.dao.CampaignDao
import com.shg25.limimeshi.core.database.dao.ChainDao
import com.shg25.limimeshi.core.database.dao.FavoriteDao
import com.shg25.limimeshi.core.database.entity.CampaignEntity
import com.shg25.limimeshi.core.database.entity.ChainEntity
import com.shg25.limimeshi.core.database.entity.FavoriteEntity

/**
 * Limimeshi アプリのRoom Database
 */
@Database(
    entities = [
        ChainEntity::class,
        CampaignEntity::class,
        FavoriteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LimimeshiDatabase : RoomDatabase() {
    abstract fun chainDao(): ChainDao
    abstract fun campaignDao(): CampaignDao
    abstract fun favoriteDao(): FavoriteDao
}
