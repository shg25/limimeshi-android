# Quickstart: お気に入り登録（Favorites）

- **Feature**: 003-favorites
- **Date**: 2025-11-28
- **Related**: [spec.md](./spec.md), [research.md](./research.md), [contracts/firestore-schema.md](./contracts/firestore-schema.md)  

## Prerequisites

- Android Studio Hedgehog (2023.1.1) 以上
- JDK 17 以上
- Firebase Project作成済み（`limimeshi-dev` or `limimeshi-prod`）
- Firestore有効化済み
- Firebase Authentication有効化済み（Googleログイン、匿名認証）
- limimeshi-android リポジトリがセットアップ済み（002-chain-listで構築）
- 002-chain-list機能が実装済み（お気に入りフィルタとの連携のため）

---

## Core Implementation

### 1. お気に入りボタンコンポーネント

`app/src/main/java/com/shg25/limimeshi/ui/components/FavoriteButton.kt`:
```kotlin
package com.shg25.limimeshi.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    isLoading: Boolean,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val description = if (isFavorite) "お気に入り解除" else "お気に入り登録"

    IconButton(
        onClick = onToggle,
        enabled = isEnabled && !isLoading,
        modifier = modifier.semantics { contentDescription = description }
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
            isFavorite -> {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = description,
                    tint = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = description,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
```

### 2. お気に入りリポジトリ

`app/src/main/java/com/shg25/limimeshi/data/repository/FavoritesRepository.kt`:
```kotlin
package com.shg25.limimeshi.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun isFavorite(userId: String, chainId: String): Boolean {
        val favoriteRef = db.collection("users").document(userId)
            .collection("favorites").document(chainId)
        val snapshot = favoriteRef.get().await()
        return snapshot.exists()
    }

    suspend fun getFavoriteChainIds(userId: String): List<String> {
        val favoritesSnapshot = db.collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .await()
        return favoritesSnapshot.documents.map { it.id }
    }

    suspend fun addFavorite(userId: String, chainId: String) {
        db.runTransaction { transaction ->
            val favoriteRef = db.collection("users").document(userId)
                .collection("favorites").document(chainId)
            val chainRef = db.collection("chains").document(chainId)

            transaction.set(favoriteRef, hashMapOf(
                "chainId" to chainId,
                "createdAt" to FieldValue.serverTimestamp()
            ))

            transaction.update(chainRef, mapOf(
                "favoriteCount" to FieldValue.increment(1),
                "updatedAt" to FieldValue.serverTimestamp()
            ))
        }.await()
    }

    suspend fun removeFavorite(userId: String, chainId: String) {
        db.runTransaction { transaction ->
            val favoriteRef = db.collection("users").document(userId)
                .collection("favorites").document(chainId)
            val chainRef = db.collection("chains").document(chainId)

            transaction.delete(favoriteRef)

            transaction.update(chainRef, mapOf(
                "favoriteCount" to FieldValue.increment(-1),
                "updatedAt" to FieldValue.serverTimestamp()
            ))
        }.await()
    }
}
```

### 3. お気に入り登録数表示コンポーネント

`app/src/main/java/com/shg25/limimeshi/ui/components/FavoriteCount.kt`:
```kotlin
package com.shg25.limimeshi.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteCount(
    count: Int,
    modifier: Modifier = Modifier
) {
    // 0件の場合は表示しない
    if (count <= 0) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${count}人がお気に入り登録",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

---

## Testing

### 単体テスト

`app/src/test/java/com/shg25/limimeshi/ui/components/FavoriteButtonTest.kt`:
```kotlin
package com.shg25.limimeshi.ui.components

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test

class FavoriteButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ログインユーザーの場合、ボタンが有効化される`() {
        composeTestRule.setContent {
            FavoriteButton(
                isFavorite = false,
                isLoading = false,
                isEnabled = true,
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("お気に入り登録")
            .assertIsEnabled()
    }

    @Test
    fun `未ログインユーザーの場合、ボタンが無効化される`() {
        composeTestRule.setContent {
            FavoriteButton(
                isFavorite = false,
                isLoading = false,
                isEnabled = false,
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("お気に入り登録")
            .assertIsNotEnabled()
    }

    @Test
    fun `お気に入り登録済みの場合、解除ボタンが表示される`() {
        composeTestRule.setContent {
            FavoriteButton(
                isFavorite = true,
                isLoading = false,
                isEnabled = true,
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("お気に入り解除")
            .assertExists()
    }

    @Test
    fun `ローディング中の場合、ボタンが無効化される`() {
        composeTestRule.setContent {
            FavoriteButton(
                isFavorite = false,
                isLoading = true,
                isEnabled = true,
                onToggle = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("お気に入り登録")
            .assertIsNotEnabled()
    }
}
```

### リポジトリテスト

`app/src/test/java/com/shg25/limimeshi/data/repository/FavoritesRepositoryTest.kt`:
```kotlin
package com.shg25.limimeshi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FavoritesRepositoryTest {

    private lateinit var repository: FavoritesRepository
    private lateinit var mockDb: FirebaseFirestore

    @Before
    fun setup() {
        mockDb = mockk(relaxed = true)
        repository = FavoritesRepository(mockDb)
    }

    // Note: Firestoreのモックは複雑なため、
    // 実際のテストではFirebase Emulatorを使用することを推奨
}
```

---

## Integration with Campaign List

### CampaignCardへのお気に入りボタン追加

`app/src/main/java/com/shg25/limimeshi/ui/campaign/CampaignCard.kt` に以下を追加:
```kotlin
@Composable
fun CampaignCard(
    campaign: CampaignWithChain,
    isFavorite: Boolean,
    isLoggedIn: Boolean,
    isLoading: Boolean,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campaign.chainName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                FavoriteButton(
                    isFavorite = isFavorite,
                    isLoading = isLoading,
                    isEnabled = isLoggedIn,
                    onToggle = onFavoriteToggle
                )
            }
            // ... 既存のキャンペーン情報表示
        }
    }
}
```

---

## Notes

- **002-chain-listとの統合**: お気に入りボタンは002で作成したChainCardに追加
- **認証状態の管理**: ViewModelでFirebase Auth AuthStateListenerを使用
- **エラーハンドリング**: Snackbarでエラーメッセージを表示
- **パフォーマンス**: お気に入り状態は画面表示時に一括取得
- **Android優先**: Phase2ではAndroidアプリを優先（Webアプリ対応はPhase3以降）
