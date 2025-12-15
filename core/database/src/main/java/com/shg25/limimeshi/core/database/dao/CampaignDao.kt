package com.shg25.limimeshi.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shg25.limimeshi.core.database.entity.CampaignEntity
import kotlinx.coroutines.flow.Flow

/**
 * キャンペーンのDAO
 */
@Dao
interface CampaignDao {
    @Query("""
        SELECT * FROM campaigns
        WHERE chainId = :chainId
        AND saleStartTime >= :oneYearAgoMillis
        ORDER BY saleStartTime DESC
    """)
    fun getByChainId(chainId: String, oneYearAgoMillis: Long): Flow<List<CampaignEntity>>

    @Query("""
        SELECT * FROM campaigns
        WHERE saleStartTime >= :oneYearAgoMillis
        ORDER BY saleStartTime DESC
    """)
    fun getAllRecent(oneYearAgoMillis: Long): Flow<List<CampaignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(campaigns: List<CampaignEntity>)

    @Query("DELETE FROM campaigns")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(campaigns: List<CampaignEntity>) {
        deleteAll()
        insertAll(campaigns)
    }
}
