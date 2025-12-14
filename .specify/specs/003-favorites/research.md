# Research: お気に入り登録（Favorites）

- **Feature**: 003-favorites
- **Date**: 2025-11-28
- **Updated**: 2025-12-14
- **Status**: Phase 0 - Research

## Overview

お気に入り登録機能の実装に必要な技術選定と設計パターンの調査結果（Android版）

## Strategy Update（2025/12/07）

**方針変更：YAGNI緩和 → ポートフォリオ戦略**

本プロジェクトの目的が「Androidエンジニアとしての専門性・技術力の証明」に変更されたため、以下の方針を採用：

- **技術要素の網羅的導入**: 多くのライブラリ・パターンを実装し、技術幅を証明
- **マルチモジュール構成**: 早期からモジュール分割を実施し、設計力を証明
- **002-chain-listとの整合性**: アーキテクチャ・技術選定は002と統一

詳細は `docs/roadmap.md`（方針変更セクション）および `002-chain-list/research.md` を参照。

## 1. Firebase Authentication（ログイン状態の管理）

### Decision
Firebase Authentication の `AuthStateListener` を使用してログイン状態をリアルタイム監視

### Rationale
- **リアルタイム認証状態監視**: ログイン/ログアウトを即座に検知し、UIを更新
- **Kotlin完全対応**: FirebaseUser型で型安全なユーザー情報取得
- **002-chain-listとの一貫性**: 002でも同じパターンを使用
- **複数デバイス対応**: 同じユーザーが複数デバイスからログインした場合でも一貫した状態を維持

### Key APIs
```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

val auth = FirebaseAuth.getInstance()

// リスナーの追加
val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
    val user: FirebaseUser? = firebaseAuth.currentUser
    if (user != null) {
        // ログイン済み → お気に入り登録ボタンを表示
        println("Logged in: ${user.uid}")
    } else {
        // 未ログイン → お気に入り登録ボタンを非表示
        println("Not logged in")
    }
}

auth.addAuthStateListener(authStateListener)

// リスナーの削除（Composableのライフサイクルに合わせて）
auth.removeAuthStateListener(authStateListener)
```

### Alternatives Considered
- **auth.currentUser**: リアルタイム監視できず、ログイン状態の変化を検知できない

### Reference
- [Firebase Authentication Android](https://firebase.google.com/docs/auth/android/start)
- [AuthStateListener](https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth.AuthStateListener)

---

## 2. Firestore（お気に入りデータの永続化）

### Decision
Firestore の `/users/{userId}/favorites/{chainId}` サブコレクションパターンを使用

### Rationale
- **ユーザーごとの隔離**: 各ユーザーのお気に入りデータを独立して管理
- **Security Rulesの簡潔性**: `request.auth.uid == userId` で簡単にアクセス制御
- **スケーラビリティ**: ユーザー数が増えても各ユーザーのお気に入りは独立しているため、クエリパフォーマンスが劣化しない
- **002-chain-listとの一貫性**: 002でも同じパターンで読み取り

### Schema Design

```kotlin
// /users/{userId}/favorites/{chainId}
data class Favorite(
    val chainId: String = "",            // お気に入り登録したチェーンID（ドキュメントIDと同じ）
    val createdAt: Timestamp = Timestamp.now()  // 登録日時（Firebase Server Timestamp）
)
```

### Key APIs

```kotlin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

val db = FirebaseFirestore.getInstance()

// お気に入り登録
val favoriteRef = db.collection("users").document(userId)
    .collection("favorites").document(chainId)
favoriteRef.set(
    hashMapOf(
        "chainId" to chainId,
        "createdAt" to FieldValue.serverTimestamp()
    )
).await()

// お気に入り解除
favoriteRef.delete().await()

// お気に入り状態の確認
val favoriteSnap = favoriteRef.get().await()
val isFavorite = favoriteSnap.exists()
```

### Alternatives Considered
- **配列フィールド**: `/users/{userId}` に `favoriteChainIds: List<String>` を保存する方法（Security Rulesで要素単位のアクセス制御が難しい、配列サイズ制限）
- **ルートレベルコレクション**: `/favorites/{favoriteId}` にユーザーIDとチェーンIDを保存（クエリが複雑になる）

### Reference
- [Firestore Subcollections](https://firebase.google.com/docs/firestore/data-model#subcollections)

---

## 3. Firestore Transactions（お気に入り登録数の集約データ更新）

### Decision
Firestore Transactions を使用して、お気に入り登録・解除時に `/chains/{chainId}` の `favoriteCount` を増減

### Rationale
- **データ整合性**: お気に入り登録とカウント増加を原子的に実行
- **同時実行制御**: 複数ユーザーが同時にお気に入り登録しても、カウントが正確に増減
- **パフォーマンス**: リアルタイムカウントではなく、集約データとして管理することで、002-chain-listでの表示が高速化

### Key APIs

```kotlin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

val db = FirebaseFirestore.getInstance()

// お気に入り登録
db.runTransaction { transaction ->
    val favoriteRef = db.collection("users").document(userId)
        .collection("favorites").document(chainId)
    val chainRef = db.collection("chains").document(chainId)

    // お気に入り登録
    transaction.set(favoriteRef, hashMapOf(
        "chainId" to chainId,
        "createdAt" to FieldValue.serverTimestamp()
    ))

    // お気に入り登録数を+1
    transaction.update(chainRef, mapOf(
        "favoriteCount" to FieldValue.increment(1),
        "updatedAt" to FieldValue.serverTimestamp()
    ))
}.await()

// お気に入り解除
db.runTransaction { transaction ->
    val favoriteRef = db.collection("users").document(userId)
        .collection("favorites").document(chainId)
    val chainRef = db.collection("chains").document(chainId)

    // お気に入り解除
    transaction.delete(favoriteRef)

    // お気に入り登録数を-1
    transaction.update(chainRef, mapOf(
        "favoriteCount" to FieldValue.increment(-1),
        "updatedAt" to FieldValue.serverTimestamp()
    ))
}.await()
```

### Edge Cases
- **負の値防止**: `favoriteCount` が0の場合、デクリメントしても-1にならないようにする（Firestoreのincrement()は負の値を許容するため、クライアント側で0チェックが必要）
- **トランザクション失敗**: ネットワークエラーや競合でトランザクションが失敗した場合、リトライまたはエラーメッセージを表示

### Alternatives Considered
- **Cloud Functions トリガー**: お気に入り登録時にCloud Functionsで `favoriteCount` を更新する方法（Phase2では不要、コスト増）
- **リアルタイムカウント**: 毎回 `/users/*/favorites/{chainId}` をカウントする方法（パフォーマンス劣化、Firestore読み取り回数増）

### Reference
- [Firestore Transactions (Android)](https://firebase.google.com/docs/firestore/manage-data/transactions#android)

---

## 4. Jetpack Compose UI（お気に入りボタン）

### Decision
Jetpack Compose + Material 3 IconButton を使用

### Rationale
- **002-chain-listとの一貫性**: 002でもJetpack Compose + Material 3を使用
- **宣言的UI**: 状態変更に応じて自動的にUIが更新される
- **Material 3準拠**: ダイナミックカラー対応、ダークモード対応

### Key APIs

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    isLoading: Boolean,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    IconButton(
        onClick = onToggle,
        enabled = isEnabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else if (isFavorite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "お気に入り解除",
                tint = MaterialTheme.colorScheme.error
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "お気に入り登録",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
```

### Reference
- [Jetpack Compose Material 3](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Material 3 Icons](https://m3.material.io/styles/icons/overview)

---

## Summary

| 項目 | 選定技術 | 理由 |
|------|---------|------|
| 認証状態管理 | Firebase Authentication (AuthStateListener) | リアルタイム監視、Kotlin完全対応 |
| データ永続化 | Firestore サブコレクション | ユーザーごとの隔離、Security Rules簡潔 |
| ローカルキャッシュ | Room Database | オフライン対応（002と共通） |
| 集約データ更新 | Firestore Transactions | データ整合性、同時実行制御 |
| UIコンポーネント | Jetpack Compose + Material 3 | 宣言的UI、002との一貫性 |
| アーキテクチャ | MVVM + Clean Architecture + マルチモジュール | 002と統一 |
| DI | Hilt | Android公式推奨 |

全ての技術選定は `.specify/memory/constitution.md` および `docs/adr/001-adopt-portfolio-driven-tech-stack.md` に準拠しています。

### 2025/12/14 再確認結果

Android Developers公式ドキュメントを確認し、上記技術選定が2025年12月時点でも公式推奨に沿っていることを確認。
詳細は `002-chain-list/research.md` を参照。
