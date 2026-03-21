package com.shg25.limimeshi.feature.chainlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shg25.limimeshi.core.domain.GetCampaignStatusUseCase
import com.shg25.limimeshi.core.domain.GetChainListUseCase
import com.shg25.limimeshi.core.domain.ObserveFavoriteIdsUseCase
import com.shg25.limimeshi.core.domain.ObserveLoginStateUseCase
import com.shg25.limimeshi.core.domain.SyncChainDataUseCase
import com.shg25.limimeshi.core.domain.SyncFavoritesUseCase
import com.shg25.limimeshi.core.domain.ToggleFavoriteUseCase
import com.shg25.limimeshi.core.model.ChainSortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * チェーン店一覧画面のViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChainListViewModel @Inject constructor(
    private val getChainListUseCase: GetChainListUseCase,
    private val syncChainDataUseCase: SyncChainDataUseCase,
    private val observeLoginStateUseCase: ObserveLoginStateUseCase,
    private val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
    private val syncFavoritesUseCase: SyncFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    val getCampaignStatusUseCase: GetCampaignStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChainListUiState())
    val uiState: StateFlow<ChainListUiState> = _uiState.asStateFlow()

    private val _sortOrder = MutableStateFlow(ChainSortOrder.NEWEST)

    init {
        ensureCache()
        observeChainList()
        observeLoginState()
        observeFavorites()
    }

    /**
     * キャッシュが空の場合にFirestoreから同期
     */
    private fun ensureCache() {
        viewModelScope.launch {
            if (syncChainDataUseCase.isCacheEmpty()) {
                syncFromFirestore()
            }
        }
    }

    /**
     * チェーン店一覧をソート順に応じてリアルタイム購読（flatMapLatestで単一購読）
     */
    private fun observeChainList() {
        viewModelScope.launch {
            _sortOrder.flatMapLatest { sortOrder ->
                getChainListUseCase(sortOrder)
            }
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
        if (_sortOrder.value == sortOrder) return

        _uiState.update { it.copy(sortOrder = sortOrder) }
        _sortOrder.value = sortOrder
    }

    /**
     * エラーメッセージをクリア
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * ログイン状態を監視
     */
    private fun observeLoginState() {
        viewModelScope.launch {
            observeLoginStateUseCase().collect { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }

                // ログイン時にお気に入りを同期
                if (isLoggedIn) {
                    try {
                        syncFavoritesUseCase()
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to sync favorites")
                    }
                }
            }
        }
    }

    /**
     * お気に入り状態を監視
     */
    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoriteIdsUseCase().collect { favoriteIds ->
                _uiState.update { it.copy(favoriteChainIds = favoriteIds) }
            }
        }
    }

    /**
     * お気に入りをトグル
     */
    fun toggleFavorite(chainId: String) {
        val currentState = _uiState.value
        if (!currentState.isLoggedIn) return
        if (currentState.isLoadingFavorite(chainId)) return

        val isFavorite = currentState.isFavorite(chainId)
        val roomCount = currentState.chains
            .firstOrNull { it.chain.id == chainId }
            ?.chain?.favoriteCount ?: 0
        val originalCount = currentState.getFavoriteCount(chainId, roomCount)
        val optimisticCount = if (isFavorite) {
            (originalCount - 1).coerceAtLeast(0)
        } else {
            originalCount + 1
        }

        viewModelScope.launch {
            // ローディング状態を設定 + Optimistic UIでカウントを即時更新
            _uiState.update {
                it.copy(
                    loadingFavoriteChainIds = it.loadingFavoriteChainIds + chainId,
                    favoriteCountOverrides = it.favoriteCountOverrides + (chainId to optimisticCount)
                )
            }

            toggleFavoriteUseCase(chainId, isFavorite)
                .onSuccess {
                    Timber.d("Favorite toggled successfully: $chainId")
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to toggle favorite: $chainId")
                    _uiState.update {
                        it.copy(
                            errorMessage = "お気に入りの変更に失敗しました",
                            favoriteCountOverrides = it.favoriteCountOverrides - chainId
                        )
                    }
                }

            // ローディング状態を解除（オーバーライドは成功時は維持、失敗時のみ上で削除済み）
            _uiState.update {
                it.copy(loadingFavoriteChainIds = it.loadingFavoriteChainIds - chainId)
            }
        }
    }
}
