package com.shg25.limimeshi.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shg25.limimeshi.core.database.dao.CampaignDao
import com.shg25.limimeshi.core.database.dao.ChainDao
import com.shg25.limimeshi.core.database.entity.CampaignEntity
import com.shg25.limimeshi.core.database.entity.ChainEntity

/**
 * Limimeshi アプリのRoom Database
 */
@Database(
    entities = [
        ChainEntity::class,
        CampaignEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LimimeshiDatabase : RoomDatabase() {
    abstract fun chainDao(): ChainDao
    abstract fun campaignDao(): CampaignDao
}
