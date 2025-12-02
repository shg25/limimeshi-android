# Quickstart: チェーン店一覧（Chain List）

**Feature**: 002-chain-list
**Date**: 2025-11-28
**Related**: [spec.md](./spec.md), [research.md](./research.md), [contracts/firestore-schema.md](./contracts/firestore-schema.md)

## Prerequisites

- Android Studio Hedgehog (2023.1.1) 以上
- JDK 17 以上
- Firebase Project作成済み（`limimeshi-dev` or `limimeshi-prod`）
- Firestore有効化済み
- Firebase Authentication有効化済み（Googleログイン）
- limimeshi-android リポジトリがセットアップ済み

---

## Setup

### 1. プロジェクト作成

Android Studioで新規プロジェクト作成:
- **Template**: Empty Activity（Compose）
- **Name**: Limimeshi
- **Package name**: com.shg25.limimeshi
- **Language**: Kotlin
- **Minimum SDK**: API 26 (Android 8.0)
- **Build configuration language**: Kotlin DSL

### 2. 依存関係のインストール

`app/build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.shg25.limimeshi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shg25.limimeshi"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

`build.gradle.kts`（プロジェクトルート）:
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}
```

### 3. Firebase設定

1. Firebase Consoleで「Androidアプリを追加」
2. パッケージ名: `com.shg25.limimeshi`
3. `google-services.json` をダウンロードして `app/` に配置
4. `.gitignore` に `google-services.json` を追加

### 4. Firestoreインデックスの作成

Firebase Consoleで以下のインデックスを作成:

#### Index 1: furigana（昇順）
- Collection: `chains`
- Fields: `furigana` (Ascending)

#### Index 2: chainId + saleStartTime
- Collection: `campaigns`
- Fields:
  - `chainId` (Ascending)
  - `saleStartTime` (Descending)

---

## Core Implementation

### 1. データモデル

`app/src/main/java/com/shg25/limimeshi/data/model/Chain.kt`:
```kotlin
package com.shg25.limimeshi.data.model

import com.google.firebase.Timestamp

data class Chain(
    val id: String = "",
    val name: String = "",
    val furigana: String = "",
    val officialUrl: String? = null,
    val logoUrl: String? = null,
    val favoriteCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
```

`app/src/main/java/com/shg25/limimeshi/data/model/Campaign.kt`:
```kotlin
package com.shg25.limimeshi.data.model

import com.google.firebase.Timestamp

data class Campaign(
    val id: String = "",
    val chainId: String = "",
    val name: String = "",
    val description: String? = null,
    val xPostUrl: String? = null,
    val saleStartTime: Timestamp = Timestamp.now(),
    val saleEndTime: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
```

`app/src/main/java/com/shg25/limimeshi/data/model/CampaignStatus.kt`:
```kotlin
package com.shg25.limimeshi.data.model

sealed class CampaignStatus {
    object Scheduled : CampaignStatus()
    data class DaysElapsed(val days: Int) : CampaignStatus()
    data class MonthsElapsed(val months: Int) : CampaignStatus()
    object Ended : CampaignStatus()

    fun toDisplayString(): String = when (this) {
        is Scheduled -> "予定"
        is DaysElapsed -> "開始から${days}日経過"
        is MonthsElapsed -> "開始から${months}ヶ月以上経過"
        is Ended -> "終了"
    }
}
```

`app/src/main/java/com/shg25/limimeshi/data/model/ChainSortOrder.kt`:
```kotlin
package com.shg25.limimeshi.data.model

enum class ChainSortOrder(val displayName: String) {
    NEWEST("新着順"),
    FURIGANA("ふりがな順")
}
```

`app/src/main/java/com/shg25/limimeshi/data/model/ChainWithCampaigns.kt`:
```kotlin
package com.shg25.limimeshi.data.model

data class ChainWithCampaigns(
    val chain: Chain,
    val campaigns: List<CampaignWithStatus>
) {
    // 新着順ソート用: 最新キャンペーンのsaleStartTime
    val latestCampaignTime: Long
        get() = campaigns.maxOfOrNull { it.campaign.saleStartTime.toDate().time } ?: 0L
}

data class CampaignWithStatus(
    val campaign: Campaign,
    val status: CampaignStatus
)
```

### 2. ステータス判定ユーティリティ

`app/src/main/java/com/shg25/limimeshi/util/CampaignStatusUtil.kt`:
```kotlin
package com.shg25.limimeshi.util

import com.google.firebase.Timestamp
import com.shg25.limimeshi.data.model.CampaignStatus
import java.util.Date
import java.util.concurrent.TimeUnit

fun getCampaignStatus(
    saleStartTime: Timestamp,
    saleEndTime: Timestamp?
): CampaignStatus {
    val now = Date()
    val startDate = saleStartTime.toDate()
    val endDate = saleEndTime?.toDate()

    // 販売終了日時が設定されていて、現在時刻が終了日時を過ぎている
    if (endDate != null && now.after(endDate)) {
        return CampaignStatus.Ended
    }

    // 販売開始日時が未来
    if (startDate.after(now)) {
        return CampaignStatus.Scheduled
    }

    // 販売開始日時から経過日数を計算
    val daysPassed = TimeUnit.MILLISECONDS.toDays(now.time - startDate.time).toInt()

    // 30日以内
    if (daysPassed < 30) {
        return CampaignStatus.DaysElapsed(daysPassed)
    }

    // 30日以上
    val monthsPassed = daysPassed / 30
    return CampaignStatus.MonthsElapsed(monthsPassed)
}
```

### 3. Repository

`app/src/main/java/com/shg25/limimeshi/data/repository/ChainRepository.kt`:
```kotlin
package com.shg25.limimeshi.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shg25.limimeshi.data.model.Campaign
import com.shg25.limimeshi.data.model.CampaignWithStatus
import com.shg25.limimeshi.data.model.Chain
import com.shg25.limimeshi.data.model.ChainWithCampaigns
import com.shg25.limimeshi.util.getCampaignStatus
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChainRepository @Inject constructor() {
    private val db = Firebase.firestore

    suspend fun getChainsWithCampaigns(favoriteChainIds: List<String>? = null): List<ChainWithCampaigns> {
        val oneYearAgo = Timestamp(Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))

        // チェーン店一覧を取得（ふりがな順）
        val chainsQuery = if (favoriteChainIds != null && favoriteChainIds.isNotEmpty()) {
            // お気に入りフィルタON
            db.collection("chains")
                .whereIn("__name__", favoriteChainIds.take(10))
        } else {
            // 全チェーン店
            db.collection("chains")
                .orderBy("furigana", Query.Direction.ASCENDING)
        }

        val chainsSnapshot = chainsQuery.get().await()
        val chains = chainsSnapshot.toObjects(Chain::class.java)
            .mapIndexed { index, chain ->
                chain.copy(id = chainsSnapshot.documents[index].id)
            }
            .let { chainList ->
                if (favoriteChainIds != null) {
                    // お気に入りフィルタ時もふりがな順でソート
                    chainList.sortedBy { it.furigana }
                } else {
                    chainList
                }
            }

        // 各チェーン店に紐づくキャンペーンを取得
        return chains.map { chain ->
            val campaignsSnapshot = db.collection("campaigns")
                .whereEqualTo("chainId", chain.id)
                .whereGreaterThanOrEqualTo("saleStartTime", oneYearAgo)
                .orderBy("saleStartTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val campaigns = campaignsSnapshot.toObjects(Campaign::class.java)
                .mapIndexed { index, campaign ->
                    campaign.copy(id = campaignsSnapshot.documents[index].id)
                }

            val campaignsWithStatus = campaigns.map { campaign ->
                CampaignWithStatus(
                    campaign = campaign,
                    status = getCampaignStatus(campaign.saleStartTime, campaign.saleEndTime)
                )
            }

            ChainWithCampaigns(
                chain = chain,
                campaigns = campaignsWithStatus
            )
        }
    }
}
```

### 4. ViewModel

`app/src/main/java/com/shg25/limimeshi/ui/chain/ChainListViewModel.kt`:
```kotlin
package com.shg25.limimeshi.ui.chain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shg25.limimeshi.data.model.ChainSortOrder
import com.shg25.limimeshi.data.model.ChainWithCampaigns
import com.shg25.limimeshi.data.repository.ChainRepository
import com.shg25.limimeshi.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChainListUiState(
    val chainsWithCampaigns: List<ChainWithCampaigns> = emptyList(),
    val isLoading: Boolean = true,
    val showFavoritesOnly: Boolean = false,
    val sortOrder: ChainSortOrder = ChainSortOrder.NEWEST,
    val error: String? = null
)

@HiltViewModel
class ChainListViewModel @Inject constructor(
    private val chainRepository: ChainRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChainListUiState())
    val uiState: StateFlow<ChainListUiState> = _uiState.asStateFlow()

    // ソート前のデータを保持
    private var rawChainsWithCampaigns: List<ChainWithCampaigns> = emptyList()

    init {
        viewModelScope.launch {
            val savedFilter = preferencesRepository.showFavoritesOnly.first()
            val savedSortOrder = preferencesRepository.sortOrder.first()
            _uiState.value = _uiState.value.copy(
                showFavoritesOnly = savedFilter,
                sortOrder = savedSortOrder
            )
            loadChains()
        }
    }

    fun loadChains() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val favoriteChainIds = if (_uiState.value.showFavoritesOnly) {
                    // TODO: 003-favoritesから取得
                    emptyList()
                } else {
                    null
                }

                rawChainsWithCampaigns = chainRepository.getChainsWithCampaigns(favoriteChainIds)
                applySortOrder()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun applySortOrder() {
        val sorted = when (_uiState.value.sortOrder) {
            ChainSortOrder.NEWEST -> rawChainsWithCampaigns.sortedByDescending { it.latestCampaignTime }
            ChainSortOrder.FURIGANA -> rawChainsWithCampaigns.sortedBy { it.chain.furigana }
        }
        _uiState.value = _uiState.value.copy(
            chainsWithCampaigns = sorted,
            isLoading = false
        )
    }

    fun setSortOrder(sortOrder: ChainSortOrder) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sortOrder = sortOrder)
            preferencesRepository.setSortOrder(sortOrder)
            applySortOrder()
        }
    }

    fun toggleFavoritesFilter() {
        viewModelScope.launch {
            val newValue = !_uiState.value.showFavoritesOnly
            _uiState.value = _uiState.value.copy(showFavoritesOnly = newValue)
            preferencesRepository.setShowFavoritesOnly(newValue)
            loadChains()
        }
    }
}
```

### 5. 画面

`app/src/main/java/com/shg25/limimeshi/ui/chain/ChainListScreen.kt`:
```kotlin
package com.shg25.limimeshi.ui.chain

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shg25.limimeshi.data.model.ChainSortOrder
import com.shg25.limimeshi.data.model.ChainWithCampaigns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainListScreen(
    viewModel: ChainListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("チェーン店一覧") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // フィルタ・ソートバー
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ソート選択
                SortSelector(
                    selectedSortOrder = uiState.sortOrder,
                    onSortOrderSelected = { viewModel.setSortOrder(it) }
                )

                // お気に入りフィルタ
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("お気に入りのみ", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = uiState.showFavoritesOnly,
                        onCheckedChange = { viewModel.toggleFavoritesFilter() }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("エラー: ${uiState.error}")
                    }
                }
                uiState.chainsWithCampaigns.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (uiState.showFavoritesOnly)
                                "お気に入り登録したチェーン店がありません"
                            else
                                "データなし"
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.chainsWithCampaigns) { chainWithCampaigns ->
                            ChainCard(chainWithCampaigns = chainWithCampaigns)
                        }
                    }
                }
            }
        }
    }
}
```

`app/src/main/java/com/shg25/limimeshi/ui/chain/ChainCard.kt`:
```kotlin
package com.shg25.limimeshi.ui.chain

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shg25.limimeshi.data.model.ChainWithCampaigns

@Composable
fun ChainCard(chainWithCampaigns: ChainWithCampaigns) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // チェーン店情報
            Text(
                text = chainWithCampaigns.chain.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "お気に入り: ${chainWithCampaigns.chain.favoriteCount}人",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // キャンペーン一覧
            if (chainWithCampaigns.campaigns.isEmpty()) {
                Text(
                    text = "現在キャンペーンはありません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                chainWithCampaigns.campaigns.forEach { campaignWithStatus ->
                    CampaignItem(campaignWithStatus = campaignWithStatus)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
```

`app/src/main/java/com/shg25/limimeshi/ui/chain/CampaignItem.kt`:
```kotlin
package com.shg25.limimeshi.ui.chain

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shg25.limimeshi.data.model.CampaignWithStatus

@Composable
fun CampaignItem(campaignWithStatus: CampaignWithStatus) {
    val campaign = campaignWithStatus.campaign

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = campaign.name,
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "販売開始: ${campaign.saleStartTime.toDate()}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = campaignWithStatus.status.toDisplayString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            campaign.saleEndTime?.let {
                Text(
                    text = "販売終了: ${it.toDate()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            campaign.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            // X Post埋め込みはXPostEmbedコンポーネントで実装
        }
    }
}
```

`app/src/main/java/com/shg25/limimeshi/ui/components/SortSelector.kt`:
```kotlin
package com.shg25.limimeshi.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.shg25.limimeshi.data.model.ChainSortOrder

@Composable
fun SortSelector(
    selectedSortOrder: ChainSortOrder,
    onSortOrderSelected: (ChainSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }) {
            Text(selectedSortOrder.displayName)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "ソート順を選択")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ChainSortOrder.entries.forEach { sortOrder ->
                DropdownMenuItem(
                    text = { Text(sortOrder.displayName) },
                    onClick = {
                        onSortOrderSelected(sortOrder)
                        expanded = false
                    }
                )
            }
        }
    }
}
```

---

## Testing

### 単体テスト（ステータス判定ロジック）

`app/src/test/java/com/shg25/limimeshi/util/CampaignStatusUtilTest.kt`:
```kotlin
package com.shg25.limimeshi.util

import com.google.firebase.Timestamp
import com.shg25.limimeshi.data.model.CampaignStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class CampaignStatusUtilTest {

    @Test
    fun `販売終了日時を過ぎている場合、Endedを返す`() {
        val startTime = Timestamp(Date(System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000))
        val endTime = Timestamp(Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000))

        val result = getCampaignStatus(startTime, endTime)

        assertEquals(CampaignStatus.Ended, result)
    }

    @Test
    fun `販売開始日時が未来の場合、Scheduledを返す`() {
        val startTime = Timestamp(Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))

        val result = getCampaignStatus(startTime, null)

        assertEquals(CampaignStatus.Scheduled, result)
    }

    @Test
    fun `販売開始から10日経過している場合、DaysElapsedを返す`() {
        val startTime = Timestamp(Date(System.currentTimeMillis() - 10L * 24 * 60 * 60 * 1000))

        val result = getCampaignStatus(startTime, null)

        assertEquals(CampaignStatus.DaysElapsed(10), result)
    }

    @Test
    fun `販売開始から60日経過している場合、MonthsElapsedを返す`() {
        val startTime = Timestamp(Date(System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000))

        val result = getCampaignStatus(startTime, null)

        assertEquals(CampaignStatus.MonthsElapsed(2), result)
    }
}
```

### UIテスト

`app/src/androidTest/java/com/shg25/limimeshi/ui/chain/ChainListScreenTest.kt`:
```kotlin
package com.shg25.limimeshi.ui.chain

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ChainListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun チェーン店一覧が表示される() {
        // TODO: ViewModelをモック化してテスト
    }

    @Test
    fun チェーン店0件時にデータなしと表示される() {
        // TODO: ViewModelをモック化してテスト
    }

    @Test
    fun キャンペーン0件時に現在キャンペーンはありませんと表示される() {
        // TODO: ViewModelをモック化してテスト
    }

    @Test
    fun お気に入りフィルタの切り替えが動作する() {
        // TODO: ViewModelをモック化してテスト
    }

    @Test
    fun デフォルトで新着順でソートされる() {
        // TODO: ViewModelをモック化してテスト
    }

    @Test
    fun ソート順をふりがな順に切り替えられる() {
        // TODO: ViewModelをモック化してテスト
    }

    @Test
    fun ソート順設定が次回起動時も保持される() {
        // TODO: ViewModelをモック化してテスト
    }
}
```

---

## Deployment

### 開発環境

```bash
# Firestore Emulatorで動作確認
firebase emulators:start

# Android Studioからアプリを実行
# Run > Run 'app' (Shift+F10)
```

### 内部テスト版リリース

1. Android Studio: Build > Generate Signed Bundle / APK
2. Google Play Console: 内部テストトラックにアップロード
3. テスターを招待して動作確認

---

## Troubleshooting

### Q1: Firestoreクエリが遅い
- **A**: インデックスが作成されているか確認。Firebase Consoleでインデックスステータスを確認。

### Q2: X Post埋め込みが表示されない
- **A**: WebViewのJavaScriptが有効か確認。Post作成者が削除した可能性もあり。

### Q3: お気に入りフィルタが動作しない
- **A**: ログイン済みユーザーか確認。Firebase Authの認証状態を確認。

### Q4: フィルタ設定が保存されない
- **A**: DataStoreの初期化が正しいか確認。アプリを再インストールすると設定はリセットされる。

### Q5: google-services.jsonが見つからない
- **A**: Firebase Consoleからダウンロードして `app/` 直下に配置。

### Q6: チェーン店がふりがな順で表示されない
- **A**: ソート順が「ふりがな順」に設定されているか確認。デフォルトは「新着順」。

### Q7: ソート順設定が保存されない
- **A**: DataStoreの初期化が正しいか確認。アプリを再インストールすると設定はリセットされる。

---

## Next Steps

1. **003-favorites機能の実装**: お気に入り登録・解除機能
2. **X Post埋め込み実装**: WebViewでの埋め込み表示
3. **パフォーマンス最適化**: 遅延読み込み、キャッシュ戦略
4. **エラーハンドリング**: ネットワークエラー時の再試行ロジック
5. **アクセシビリティ改善**: TalkBack対応、コントラスト比確認
