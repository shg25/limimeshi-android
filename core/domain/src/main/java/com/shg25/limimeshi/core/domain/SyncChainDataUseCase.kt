package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.ChainRepository
import javax.inject.Inject

/**
 * Firestoreからチェーン店データを同期するUseCase
 */
class SyncChainDataUseCase @Inject constructor(
    private val chainRepository: ChainRepository
) {
    /**
     * Firestoreからデータを同期
     */
    suspend operator fun invoke() {
        chainRepository.syncFromFirestore()
    }

    /**
     * キャッシュが空かどうかを確認
     */
    suspend fun isCacheEmpty(): Boolean {
        return chainRepository.isCacheEmpty()
    }
}
