package com.shg25.limimeshi.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shg25.limimeshi.core.model.Chain
import java.time.Instant

/**
 * チェーン店のローカルキャッシュ用Entity
 */
@Entity(tableName = "chains")
data class ChainEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val furigana: String,
    val favoriteCount: Int,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toModel(): Chain = Chain(
        id = id,
        name = name,
        furigana = furigana,
        favoriteCount = favoriteCount,
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )

    companion object {
        fun fromModel(chain: Chain): ChainEntity = ChainEntity(
            id = chain.id,
            name = chain.name,
            furigana = chain.furigana,
            favoriteCount = chain.favoriteCount,
            createdAt = chain.createdAt.toEpochMilli(),
            updatedAt = chain.updatedAt.toEpochMilli()
        )
    }
}
