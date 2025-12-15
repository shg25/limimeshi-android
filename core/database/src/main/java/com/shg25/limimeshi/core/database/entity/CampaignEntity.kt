package com.shg25.limimeshi.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shg25.limimeshi.core.model.Campaign
import java.time.Instant

/**
 * キャンペーンのローカルキャッシュ用Entity
 */
@Entity(
    tableName = "campaigns",
    foreignKeys = [
        ForeignKey(
            entity = ChainEntity::class,
            parentColumns = ["id"],
            childColumns = ["chainId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chainId"]),
        Index(value = ["saleStartTime"])
    ]
)
data class CampaignEntity(
    @PrimaryKey
    val id: String,
    val chainId: String,
    val name: String,
    val description: String,
    val saleStartTime: Long,
    val saleEndTime: Long?,
    val xPostUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toModel(): Campaign = Campaign(
        id = id,
        chainId = chainId,
        name = name,
        description = description,
        saleStartTime = Instant.ofEpochMilli(saleStartTime),
        saleEndTime = saleEndTime?.let { Instant.ofEpochMilli(it) },
        xPostUrl = xPostUrl,
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )

    companion object {
        fun fromModel(campaign: Campaign): CampaignEntity = CampaignEntity(
            id = campaign.id,
            chainId = campaign.chainId,
            name = campaign.name,
            description = campaign.description,
            saleStartTime = campaign.saleStartTime.toEpochMilli(),
            saleEndTime = campaign.saleEndTime?.toEpochMilli(),
            xPostUrl = campaign.xPostUrl,
            createdAt = campaign.createdAt.toEpochMilli(),
            updatedAt = campaign.updatedAt.toEpochMilli()
        )
    }
}
