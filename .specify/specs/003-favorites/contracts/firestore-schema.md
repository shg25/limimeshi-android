# Firestore Schema: お気に入り登録（Favorites）

**Feature**: 003-favorites  
**Date**: 2025-11-28  
**Status**: Phase 1 - Contract  
**Related**: [research.md](../research.md), [002-chain-list Firestore Schema](../../002-chain-list/contracts/firestore-schema.md)  

## Overview

お気に入り登録機能で使用するFirestoreコレクション構造（読み書き可能）

## Collections

### /users/{userId}/favorites/{chainId}

ユーザーごとのお気に入りチェーン店（003-favoritesで登録、002-chain-listで読み取り）

```kotlin
data class Favorite(
    val chainId: String = "",            // お気に入り登録したチェーンID（ドキュメントIDと同じ）
    val createdAt: Timestamp = Timestamp.now()  // 登録日時（Firebase Server Timestamp）
)
```

**Operations Used**:

1. **お気に入り登録**:
   ```kotlin
   import com.google.firebase.firestore.FirebaseFirestore
   import com.google.firebase.firestore.FieldValue
   import kotlinx.coroutines.tasks.await

   val db = FirebaseFirestore.getInstance()

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
   ```

2. **お気に入り解除**:
   ```kotlin
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

3. **お気に入り状態の確認**:
   ```kotlin
   val favoriteRef = db.collection("users").document(userId)
       .collection("favorites").document(chainId)
   val favoriteSnap = favoriteRef.get().await()
   val isFavorite = favoriteSnap.exists()
   ```

4. **ユーザーのお気に入り一覧取得**:
   ```kotlin
   val favoritesSnapshot = db.collection("users")
       .document(userId)
       .collection("favorites")
       .get()
       .await()

   val favoriteChainIds = favoritesSnapshot.documents.map { it.id }
   ```

---

## Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // お気に入りサブコレクション
    match /users/{userId}/favorites/{chainId} {
      // ログインユーザー本人のみ読み書き可能
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // チェーンコレクション（お気に入り登録数の更新）
    match /chains/{chainId} {
      // 全ユーザーが読み取り可能（001, 002で使用）
      allow read: if true;

      // お気に入り登録・解除時のfavoriteCount更新のみ許可
      allow update: if request.auth != null
                    && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['favoriteCount', 'updatedAt'])
                    && request.resource.data.favoriteCount >= 0;  // 負の値を防止
    }
  }
}
```

---

## Data Model（Kotlin型定義）

```kotlin
import com.google.firebase.Timestamp

// お気に入り
data class Favorite(
    val chainId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

// お気に入り状態（UIで使用）
data class FavoriteState(
    val chainId: String,
    val isFavorite: Boolean,
    val isLoading: Boolean = false
)

// チェーン店（favoriteCountフィールドを含む）
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

---

## Performance Considerations

### Firestore操作回数（Phase2想定）

#### シナリオ1: お気に入り登録
- **Transaction**: 2 writes（favorite + chain.favoriteCount）
- **合計**: 2 writes / 操作

#### シナリオ2: お気に入り解除
- **Transaction**: 2 writes（favorite削除 + chain.favoriteCount）
- **合計**: 2 writes / 操作

#### シナリオ3: お気に入り状態の初期化（画面表示時）
- **チェーンごとのお気に入り状態確認**: 1 read / チェーン
- **16チェーンの場合**: 16 reads（ただし、002-chain-listのお気に入りフィルタで一括取得する場合は1回のクエリで済む）

### コスト試算（Phase2想定）
- **DAU**: 100人
- **1日あたりお気に入り操作**: 100人 × 0.1回 = 10回（平均、月に数回の操作）
- **1日あたり書き込み**: 10回 × 2 writes = 20 writes
- **月間書き込み**: 20 writes × 30日 = 600 writes
- **Firestore無料枠**: 20,000 writes / 日 = 600,000 writes / 月
- **Phase2コスト**: 無料枠内

---

## Notes

- **読み書き可能**: 003-favoritesはお気に入りデータの読み書きを行う
- **002との連携**: 002-chain-listはお気に入りデータを読み取り専用で使用
- **001との整合性**: /chainsのスキーマは001-admin-panelと完全一致（favoriteCountフィールドを追加）
- **Transaction使用**: お気に入り登録・解除時はTransactionを使用してデータ整合性を保証
- **Android優先**: Phase2ではAndroidアプリを優先（Webアプリ対応はPhase3以降）
