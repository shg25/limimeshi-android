package com.shg25.limimeshi.feature.chainlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shg25.limimeshi.core.domain.GetChainListUseCase
import com.shg25.limimeshi.core.domain.SyncChainDataUseCase
import com.shg25.limimeshi.core.model.ChainSortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * チェーン店一覧画面のViewModel
 */
@HiltViewModel
class ChainListViewModel @Inject constructor(
    private val getChainListUseCase: GetChainListUseCase,
    private val syncChainDataUseCase: SyncChainDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChainListUiState())
    val uiState: StateFlow<ChainListUiState> = _uiState.asStateFlow()

    init {
        loadChainList()
    }

    /**
     * チェーン店一覧を読み込み
     */
    private fun loadChainList() {
        viewModelScope.launch {
            // キャッシュが空の場合は先にFirestoreから同期
            if (syncChainDataUseCase.isCacheEmpty()) {
                syncFromFirestore()
            }

            // Roomからデータを取得（リアルタイム更新）
            getChainListUseCase(_uiState.value.sortOrder)
                .catch { e ->
                    Timber.e(e, "Failed to load chain list")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "データの読み込みに失敗しました"
                        )
                    }
                }
                .collect { chains ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chains = chains,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    /**
     * Firestoreからデータを同期
     */
    private suspend fun syncFromFirestore() {
        try {
            syncChainDataUseCase()
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync from Firestore")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "サーバーとの同期に失敗しました"
                )
            }
        }
    }

    /**
     * Pull-to-refreshで最新データを取得
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            try {
                syncChainDataUseCase()
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh")
                _uiState.update {
                    it.copy(errorMessage = "更新に失敗しました")
                }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    /**
     * ソート順を変更
     */
    fun changeSortOrder(sortOrder: ChainSortOrder) {
        if (_uiState.value.sortOrder == sortOrder) return

        _uiState.update { it.copy(sortOrder = sortOrder) }

        // 新しいソート順でデータを再取得
        viewModelScope.launch {
            getChainListUseCase(sortOrder)
                .catch { e ->
                    Timber.e(e, "Failed to change sort order")
                }
                .collect { chains ->
                    _uiState.update {
                        it.copy(chains = chains)
                    }
                }
        }
    }

    /**
     * エラーメッセージをクリア
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
