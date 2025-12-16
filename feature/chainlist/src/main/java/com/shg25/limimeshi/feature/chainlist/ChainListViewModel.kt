package com.shg25.limimeshi.feature.chainlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shg25.limimeshi.core.data.repository.FavoritesRepository
import com.shg25.limimeshi.core.domain.GetChainListUseCase
import com.shg25.limimeshi.core.domain.SyncChainDataUseCase
import com.shg25.limimeshi.core.domain.ToggleFavoriteUseCase
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
    private val syncChainDataUseCase: SyncChainDataUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChainListUiState())
    val uiState: StateFlow<ChainListUiState> = _uiState.asStateFlow()

    init {
        loadChainList()
        observeLoginState()
        observeFavorites()
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

    /**
     * ログイン状態を監視
     */
    private fun observeLoginState() {
        viewModelScope.launch {
            favoritesRepository.isLoggedIn.collect { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }

                // ログイン時にお気に入りを同期
                if (isLoggedIn) {
                    try {
                        favoritesRepository.syncFromFirestore()
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
            favoritesRepository.favoriteChainIds.collect { favoriteIds ->
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

        viewModelScope.launch {
            // ローディング状態を設定
            _uiState.update {
                it.copy(loadingFavoriteChainIds = it.loadingFavoriteChainIds + chainId)
            }

            toggleFavoriteUseCase(chainId, isFavorite)
                .onSuccess {
                    Timber.d("Favorite toggled successfully: $chainId")
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to toggle favorite: $chainId")
                    _uiState.update {
                        it.copy(errorMessage = "お気に入りの変更に失敗しました")
                    }
                }

            // ローディング状態を解除
            _uiState.update {
                it.copy(loadingFavoriteChainIds = it.loadingFavoriteChainIds - chainId)
            }
        }
    }
}
