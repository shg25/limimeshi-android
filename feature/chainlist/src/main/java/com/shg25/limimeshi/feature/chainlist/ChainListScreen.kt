package com.shg25.limimeshi.feature.chainlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shg25.limimeshi.core.domain.GetCampaignStatusUseCase
import com.shg25.limimeshi.core.ui.component.FavoriteButton
import com.shg25.limimeshi.core.ui.component.XPostEmbed
import com.shg25.limimeshi.core.model.Campaign
import com.shg25.limimeshi.core.model.CampaignStatus
import com.shg25.limimeshi.core.model.ChainSortOrder
import com.shg25.limimeshi.core.model.ChainWithCampaigns
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * チェーン店一覧画面（ViewModelを使用）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainListScreen(
    viewModel: ChainListViewModel = hiltViewModel(),
    getCampaignStatusUseCase: GetCampaignStatusUseCase = GetCampaignStatusUseCase()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    ChainListContent(
        uiState = uiState,
        getCampaignStatusUseCase = getCampaignStatusUseCase,
        onSortOrderChange = viewModel::changeSortOrder,
        onRefresh = viewModel::refresh,
        onToggleFavorite = viewModel::toggleFavorite,
        snackbarHostState = snackbarHostState
    )
}

/**
 * チェーン店一覧画面のコンテンツ（テスト可能）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChainListContent(
    uiState: ChainListUiState,
    getCampaignStatusUseCase: GetCampaignStatusUseCase,
    onSortOrderChange: (ChainSortOrder) -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("チェーン店一覧") },
                actions = {
                    SortSelector(
                        currentSortOrder = uiState.sortOrder,
                        onSortOrderChange = onSortOrderChange
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.isEmpty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "データなし",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    ChainList(
                        chains = uiState.chains,
                        getCampaignStatusUseCase = getCampaignStatusUseCase,
                        isLoggedIn = uiState.isLoggedIn,
                        favoriteChainIds = uiState.favoriteChainIds,
                        loadingFavoriteChainIds = uiState.loadingFavoriteChainIds,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }
    }
}

@Composable
internal fun SortSelector(
    currentSortOrder: ChainSortOrder,
    onSortOrderChange: (ChainSortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = {
                Text(
                    when (currentSortOrder) {
                        ChainSortOrder.NEWEST -> "新着順"
                        ChainSortOrder.FURIGANA -> "ふりがな順"
                    }
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("新着順") },
                onClick = {
                    onSortOrderChange(ChainSortOrder.NEWEST)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("ふりがな順") },
                onClick = {
                    onSortOrderChange(ChainSortOrder.FURIGANA)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun ChainList(
    chains: List<ChainWithCampaigns>,
    getCampaignStatusUseCase: GetCampaignStatusUseCase,
    isLoggedIn: Boolean,
    favoriteChainIds: Set<String>,
    loadingFavoriteChainIds: Set<String>,
    onToggleFavorite: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(chains, key = { it.chain.id }) { chainWithCampaigns ->
            val chainId = chainWithCampaigns.chain.id
            ChainCard(
                chainWithCampaigns = chainWithCampaigns,
                getCampaignStatusUseCase = getCampaignStatusUseCase,
                isLoggedIn = isLoggedIn,
                isFavorite = favoriteChainIds.contains(chainId),
                isLoadingFavorite = loadingFavoriteChainIds.contains(chainId),
                onToggleFavorite = { onToggleFavorite(chainId) }
            )
        }
    }
}

@Composable
private fun ChainCard(
    chainWithCampaigns: ChainWithCampaigns,
    getCampaignStatusUseCase: GetCampaignStatusUseCase,
    isLoggedIn: Boolean,
    isFavorite: Boolean,
    isLoadingFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // ヘッダー：チェーン店名、お気に入りボタン、お気に入り数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chainWithCampaigns.chain.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                FavoriteButton(
                    isFavorite = isFavorite,
                    isLoading = isLoadingFavorite,
                    enabled = isLoggedIn,
                    onClick = onToggleFavorite
                )
                Text(
                    text = "♥ ${chainWithCampaigns.chain.favoriteCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // キャンペーン一覧
            if (chainWithCampaigns.campaigns.isEmpty()) {
                Text(
                    text = "現在キャンペーンはありません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                chainWithCampaigns.campaigns.forEach { campaign ->
                    CampaignItem(
                        campaign = campaign,
                        status = getCampaignStatusUseCase(campaign)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CampaignItem(
    campaign: Campaign,
    status: CampaignStatus
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
            .withZone(ZoneId.systemDefault())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // キャンペーン名とステータス
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campaign.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 販売開始日時
            Text(
                text = "開始: ${dateFormatter.format(campaign.saleStartTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 販売終了日時（設定されている場合のみ）
            campaign.saleEndTime?.let { endTime ->
                Text(
                    text = "終了: ${dateFormatter.format(endTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 説明
            if (campaign.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = campaign.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // X Post埋め込み
            if (!campaign.xPostUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                XPostEmbed(
                    xPostUrl = campaign.xPostUrl,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: CampaignStatus) {
    val (text, color) = when (status) {
        is CampaignStatus.Upcoming -> "予定" to MaterialTheme.colorScheme.tertiary
        is CampaignStatus.Active -> "開始から${status.daysElapsed}日経過" to MaterialTheme.colorScheme.primary
        is CampaignStatus.ActiveLongTerm -> "開始から${status.monthsElapsed}ヶ月以上経過" to MaterialTheme.colorScheme.secondary
        is CampaignStatus.Ended -> "終了" to MaterialTheme.colorScheme.outline
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
