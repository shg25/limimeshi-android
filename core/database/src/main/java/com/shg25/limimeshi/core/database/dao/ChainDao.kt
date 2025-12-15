package com.shg25.limimeshi.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shg25.limimeshi.core.database.entity.ChainEntity
import kotlinx.coroutines.flow.Flow

/**
 * チェーン店のDAO
 */
@Dao
interface ChainDao {
    @Query("SELECT * FROM chains ORDER BY furigana ASC")
    fun getAllByFurigana(): Flow<List<ChainEntity>>

    @Query("SELECT * FROM chains WHERE id = :id")
    suspend fun getById(id: String): ChainEntity?

    @Query("SELECT * FROM chains WHERE id IN (:ids)")
    fun getByIds(ids: List<String>): Flow<List<ChainEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chains: List<ChainEntity>)

    @Query("DELETE FROM chains")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(chains: List<ChainEntity>) {
        deleteAll()
        insertAll(chains)
    }
}
