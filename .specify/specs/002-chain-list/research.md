# Research: チェーン店一覧（Chain List）

- **Feature**: 002-chain-list
- **Date**: 2025-11-28
- **Updated**: 2025-12-14
- **Status**: Phase 0 - Research

## Overview

チェーン店一覧機能（Androidアプリ）の実装に必要な技術選定と設計パターンの調査結果。チェーン店を一覧表示し、各チェーン店に紐づくキャンペーンをチェーン店単位で確認できる画面。

## Strategy Update（2025/12/07）

**方針変更：YAGNI緩和 → ポートフォリオ戦略**

本プロジェクトの目的が「Androidエンジニアとしての専門性・技術力の証明」に変更されたため、以下の方針を採用：

- **技術要素の網羅的導入**: 多くのライブラリ・パターンを実装し、技術幅を証明
- **マルチモジュール構成**: 早期からモジュール分割を実施し、設計力を証明
- **非機能要件の充実**: CI/CD、テスト、品質基盤を整備し、運用力を証明

詳細は `docs/roadmap.md`（方針変更セクション）を参照。

## 1. Kotlin + Jetpack Compose（Androidフロントエンド）

### Decision
Kotlin + Jetpack Compose + Material 3 を使用

### Rationale
- **Kotlin**: Android公式推奨言語、Null安全性、コルーチンによる非同期処理
- **Jetpack Compose**: Android公式のモダンUIフレームワーク、宣言的UI、リコンポジション
- **Material 3**: 最新のMaterial Designガイドライン、Dynamic Color対応

### Alternatives Considered
- **React Native / Flutter**: クロスプラットフォームだが、Phase2ではAndroid専用でネイティブ品質を優先
- **View System（XML）**: Composeの方がコード量削減、状態管理が容易
- **Material 2**: Material 3の方が最新でAndroid 12+に適合

### Reference
- [Kotlin公式](https://kotlinlang.org/)
- [Jetpack Compose公式](https://developer.android.com/jetpack/compose)
- [Material 3 for Android](https://m3.material.io/)

---

## 2. Firebase SDK for Android（データ読み取り）

### Decision
Firebase Android SDK（Firestore、Authentication）を使用

### Rationale
- **Kotlinネイティブ対応**: Firebase SDKはKotlinフレンドリーAPI提供
- **コルーチン対応**: `kotlinx-coroutines-play-services` で非同期処理
- **オフライン対応**: Firestoreのキャッシュ機能が標準搭載
- **リアルタイム更新**: `addSnapshotListener` でリアルタイムリスニング可能（Phase3以降で活用）

### Key APIs
```kotlin
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// チェーン店一覧取得（ふりがな順）
val db = Firebase.firestore
val chains = db.collection("chains")
    .orderBy("furigana", Query.Direction.ASCENDING)
    .get()
    .await()
    .toObjects(Chain::class.java)

// キャンペーン取得（チェーン店ごと、1年以内、販売開始日時降順）
val oneYearAgo = Timestamp(Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))

val campaigns = db.collection("campaigns")
    .whereEqualTo("chainId", chainId)
    .whereGreaterThanOrEqualTo("saleStartTime", oneYearAgo)
    .orderBy("saleStartTime", Query.Direction.DESCENDING)
    .get()
    .await()
    .toObjects(Campaign::class.java)
```

### Firestore Indexes
以下の複合インデックスが必要：
- `/chains`: `furigana` (ascending)
- `/campaigns`: `chainId` (ascending) + `saleStartTime` (descending)

### Alternatives Considered
- **Realtime Database**: NoSQLだがクエリ機能が弱い、Firestoreが推奨
- **Cloud Functions経由**: Phase2ではオーバーエンジニアリング
- **Retrofit + REST API**: Firestore SDKの方がリアルタイム更新やキャッシュが容易

### Reference
- [Firebase Android SDK](https://firebase.google.com/docs/android/setup)
- [Firestore Kotlin Extensions](https://firebase.google.com/docs/reference/kotlin/com/google/firebase/firestore/ktx/package-summary)

---

## 3. Firebase Authentication（ログインユーザー判定）

### Decision
Firebase Authentication の `AuthStateListener` を使用

### Rationale
- **リアルタイム認証状態監視**: ログイン/ログアウトを即座に検知
- **Googleログイン対応**: One Tap Sign-In + 従来のGoogleログイン
- **Kotlinコルーチン対応**: 非同期処理がシンプル

### Key APIs
```kotlin
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

val auth = Firebase.auth

// 認証状態の監視（Compose）
@Composable
fun rememberAuthState(): State<FirebaseUser?> {
    val user = remember { mutableStateOf(auth.currentUser) }
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener {
            user.value = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }
    return user
}
```

### Alternatives Considered
- **Custom Auth**: Phase2ではFirebase Authで十分
- **Credential Manager API**: Phase3以降で検討（Passkey対応）

### Reference
- [Firebase Authentication Android](https://firebase.google.com/docs/auth/android/start)

---

## 4. X（Twitter）Post埋め込み

### Decision
**Android WebView** でX公式の埋め込み表示を使用

### Rationale
- **公式API**: Twitter/X公式の埋め込み方式、利用規約遵守
- **WebViewで埋め込み**: ネイティブSDKは提供されていないため、WebViewが標準的アプローチ
- **画像自動表示**: Postに画像が添付されていれば自動的に表示
- **レスポンシブ対応**: WebViewでCSS制御可能

### Implementation
```kotlin
@Composable
fun XPostEmbed(postUrl: String) {
    val tweetId = extractTweetId(postUrl) ?: return

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadData(
                    """
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                    </head>
                    <body style="margin:0">
                        <blockquote class="twitter-tweet">
                            <a href="$postUrl"></a>
                        </blockquote>
                        <script async src="https://platform.twitter.com/widgets.js"></script>
                    </body>
                    </html>
                    """.trimIndent(),
                    "text/html",
                    "UTF-8"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

fun extractTweetId(url: String): String? {
    val regex = """status/(\d+)""".toRegex()
    return regex.find(url)?.groupValues?.getOrNull(1)
}
```

### Edge Cases
- **Post削除**: Post作成者が削除した場合、埋め込み表示ができなくなる（外部依存の制約として受け入れる）
- **画像なし**: 画像がない場合でもPostテキストのみ表示される
- **読み込み遅延**: X APIの応答が遅い場合があるが、UI本体の読み込みとは分離される

### Alternatives Considered
- **Twitter SDK**: 2023年にサポート終了、現在は非推奨
- **画像のみ保存**: 著作権リスク、利用規約違反の可能性
- **oEmbed API直接呼び出し**: 追加のネットワークリクエストが必要、WebViewの方がシンプル

### Reference
- [Twitter Embedded Tweets](https://developer.twitter.com/en/docs/twitter-for-websites/embedded-tweets/overview)

---

## 5. フィルタ選択の永続化

### Decision
`DataStore Preferences` を使用してフィルタ選択（全て表示/お気に入りのみ表示）を保存

### Rationale
- **Googleの推奨**: SharedPreferencesの後継、型安全、コルーチン対応
- **永続化**: アプリを閉じても設定が保持される
- **ログインユーザー専用**: Phase2ではログインユーザーのみフィルタ機能を提供

### Key Pattern
```kotlin
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val SHOW_FAVORITES_ONLY = booleanPreferencesKey("show_favorites_only")

// フィルタ設定の読み込み
val showFavoritesOnlyFlow: Flow<Boolean> = context.dataStore.data
    .map { preferences ->
        preferences[SHOW_FAVORITES_ONLY] ?: false
    }

// フィルタ設定の保存
suspend fun saveFilterPreference(showFavoritesOnly: Boolean) {
    context.dataStore.edit { preferences ->
        preferences[SHOW_FAVORITES_ONLY] = showFavoritesOnly
    }
}
```

### Alternatives Considered
- **SharedPreferences**: 非推奨、メインスレッドでのブロッキングリスク
- **Firestore保存**: Phase2では不要（ユーザーごとに設定を保存するとFirestore読み取り回数が増える）
- **Room Database**: キーバリューストアにはDataStoreの方がシンプル

### Reference
- [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)

---

## 6. ローカルキャッシュ（Room）

### Decision
**Room Database** を使用してFirestoreデータをローカルキャッシュ

### Rationale
- **オフライン対応**: ネットワーク接続なしでもデータ表示可能
- **パフォーマンス向上**: ローカルDBからの読み込みは高速
- **ポートフォリオ戦略**: Roomの使用経験を証明
- **Single Source of Truth**: ローカルDBを唯一の信頼源とするRepository Pattern

### Implementation
```kotlin
// Entity
@Entity(tableName = "chains")
data class ChainEntity(
    @PrimaryKey val id: String,
    val name: String,
    val furigana: String,
    val logoUrl: String?,
    val officialUrl: String?,
    val favoriteCount: Int,
    val updatedAt: Long
)

// DAO
@Dao
interface ChainDao {
    @Query("SELECT * FROM chains ORDER BY furigana ASC")
    fun getChains(): Flow<List<ChainEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChains(chains: List<ChainEntity>)

    @Query("DELETE FROM chains")
    suspend fun deleteAll()
}

// Database
@Database(entities = [ChainEntity::class, CampaignEntity::class], version = 1)
abstract class LimimeshiDatabase : RoomDatabase() {
    abstract fun chainDao(): ChainDao
    abstract fun campaignDao(): CampaignDao
}
```

### Data Flow (Repository Pattern)
```
┌─────────────┐     ┌──────────────┐     ┌───────────┐
│  ViewModel  │────▶│  Repository  │────▶│   Room    │
│             │◀────│              │◀────│  (Local)  │
└─────────────┘     └──────┬───────┘     └───────────┘
                           │
                           │ refresh
                           ▼
                    ┌──────────────┐
                    │  Firestore   │
                    │  (Remote)    │
                    └──────────────┘
```

1. ViewModelはRepositoryからFlowを購読
2. Repositoryは常にRoomからデータを返す（Single Source of Truth）
3. リフレッシュ時はFirestoreから取得→Roomに保存→Flowが自動更新

### Alternatives Considered
- **Firestoreキャッシュのみ**: 標準機能だがRoom使用経験を示せない
- **DataStore**: 構造化データにはRoomの方が適切
- **SQLDelight**: Roomの方がAndroid公式推奨

### Reference
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Offline-first Repository Pattern](https://developer.android.com/topic/architecture/data-layer/offline-first)

---

## 7. Firestoreクエリ設計

### Decision
クライアント側で複合クエリを実行し、Roomにキャッシュ

### Query Patterns

#### パターン1: チェーン店一覧取得（ふりがな順）
```kotlin
db.collection("chains")
    .orderBy("furigana", Query.Direction.ASCENDING)
    .get()
    .await()
```

**必要なインデックス**:
- `furigana` (ascending) - 単一フィールドインデックス（自動作成）

#### パターン2: チェーン店のキャンペーン取得（1年以内、販売開始日時降順）
```kotlin
val oneYearAgo = Timestamp(Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))

db.collection("campaigns")
    .whereEqualTo("chainId", chainId)
    .whereGreaterThanOrEqualTo("saleStartTime", oneYearAgo)
    .orderBy("saleStartTime", Query.Direction.DESCENDING)
    .get()
    .await()
```

**必要なインデックス**:
- `chainId` (ascending) + `saleStartTime` (descending)

#### パターン3: お気に入りフィルタ（チェーン店IDの配列でフィルタ）
```kotlin
// お気に入りチェーンIDを取得（003-favoritesの機能）
val favoriteChainIds = getFavoriteChainIds(user.uid)

// chainIdが配列に含まれるチェーン店を取得
db.collection("chains")
    .whereIn(FieldPath.documentId(), favoriteChainIds)
    .orderBy("furigana", Query.Direction.ASCENDING)
    .get()
    .await()
```

**制約**:
- `whereIn` は最大10個まで
- お気に入りチェーンが10個を超える場合は、クライアント側でフィルタリング

### Performance Considerations
- **キャッシュ活用**: Firestoreのキャッシュ機能を有効化（デフォルトで有効）
- **データ量**: Phase2では16チェーン店 + 各チェーンあたり1〜5件のキャンペーン

### Alternatives Considered
- **Cloud Functions経由**: Phase2ではオーバーエンジニアリング
- **Realtime Listener**: Phase2では不要（リアルタイム更新はPhase3以降）

---

## 7. ステータス自動判定ロジック

### Decision
クライアント側で販売開始日時と販売終了日時を元にステータスを自動判定

### Logic
```kotlin
sealed class CampaignStatus {
    object Scheduled : CampaignStatus()  // 予定
    data class DaysElapsed(val days: Int) : CampaignStatus()  // 開始から◯日経過
    data class MonthsElapsed(val months: Int) : CampaignStatus()  // 開始から◯ヶ月以上経過
    object Ended : CampaignStatus()  // 終了
}

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

// 表示用文字列変換
fun CampaignStatus.toDisplayString(): String = when (this) {
    is CampaignStatus.Scheduled -> "予定"
    is CampaignStatus.DaysElapsed -> "開始から${days}日経過"
    is CampaignStatus.MonthsElapsed -> "開始から${months}ヶ月以上経過"
    is CampaignStatus.Ended -> "終了"
}
```

### Rationale
- **シンプル**: Firestoreに保存せず、表示時に計算
- **リアルタイム**: 常に最新の状態を表示
- **コスト削減**: Firestore書き込み不要
- **Sealed Class**: Kotlinの型安全性を活用、網羅性チェック

### Alternatives Considered
- **Firestore保存**: 日次バッチでステータスを更新する方法（オーバーエンジニアリング、コスト増）

---

## 8. アーキテクチャパターン

### Decision
**MVVM + Clean Architecture + マルチモジュール構成** を採用

### Rationale
- **Googleの推奨**: Android公式のアーキテクチャガイドライン
- **テスタビリティ**: ViewModelをユニットテスト可能
- **関心の分離**: UI、ビジネスロジック、データ層を分離
- **Jetpack Compose対応**: StateHolderパターンとの親和性
- **ポートフォリオ戦略**: マルチモジュール構成で設計力を証明

### Multi-Module Structure
```
app/                    # アプリケーションエントリポイント
core/
  ├── designsystem/     # 共通UI（Theme、Components）
  ├── model/            # ドメインモデル
  ├── domain/           # UseCase
  ├── data/             # Repository実装、DataSource
  └── common/           # 共通ユーティリティ
feature/
  └── chainlist/        # チェーン店一覧機能
```

### Clean Architecture Layers
```
┌─────────────────────────────────────────────┐
│  Presentation Layer (feature/chainlist)     │
│  - Composable Functions                     │
│  - ViewModel + UiState                      │
│  - Navigation                               │
└──────────────────┬──────────────────────────┘
                   │ depends on
┌──────────────────▼──────────────────────────┐
│  Domain Layer (core/domain)                 │
│  - UseCase                                  │
│  - Repository Interface                     │
│  - Domain Model                             │
└──────────────────┬──────────────────────────┘
                   │ depends on
┌──────────────────▼──────────────────────────┐
│  Data Layer (core/data)                     │
│  - Repository Implementation                │
│  - DataSource (Remote/Local)                │
│  - DTO / Entity                             │
└─────────────────────────────────────────────┘
```

### Key Components
```kotlin
// Domain Layer: Repository Interface
interface ChainRepository {
    fun getChains(): Flow<List<Chain>>
    suspend fun refreshChains()
}

// Domain Layer: UseCase
class GetChainsWithCampaignsUseCase @Inject constructor(
    private val chainRepository: ChainRepository,
    private val campaignRepository: CampaignRepository
) {
    operator fun invoke(): Flow<List<ChainWithCampaigns>> = /* ... */
}

// Data Layer: Repository Implementation
class ChainRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChainRemoteDataSource,
    private val localDataSource: ChainLocalDataSource
) : ChainRepository {
    override fun getChains(): Flow<List<Chain>> = /* ... */
}

// Presentation Layer: UIステート
data class ChainListUiState(
    val chainsWithCampaigns: List<ChainWithCampaigns> = emptyList(),
    val isLoading: Boolean = true,
    val showFavoritesOnly: Boolean = false,
    val error: String? = null
)

// Presentation Layer: ViewModel
@HiltViewModel
class ChainListViewModel @Inject constructor(
    private val getChainsWithCampaignsUseCase: GetChainsWithCampaignsUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChainListUiState())
    val uiState: StateFlow<ChainListUiState> = _uiState.asStateFlow()

    fun loadChains() { /* ... */ }
    fun toggleFavoritesFilter() { /* ... */ }
}
```

### Alternatives Considered
- **MVI**: より厳格な状態管理だが、本プロジェクトではMVVMで十分
- **Plain Compose**: 状態管理が複雑になる、テストしにくい
- **シングルモジュール**: シンプルだが、ポートフォリオとして設計力を示せない

### Reference
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Guide to Android app modularization](https://developer.android.com/topic/modularization)

---

## 9. 状態管理

### Decision
`StateFlow` + `Jetpack Compose State` を使用

### Rationale
- **Kotlin Coroutines**: 非同期処理との統合が自然
- **Compose対応**: `collectAsState()` でシームレスに連携
- **ライフサイクル対応**: `viewModelScope` で自動キャンセル

### Pattern
```kotlin
// ViewModel
private val _uiState = MutableStateFlow(ChainListUiState())
val uiState: StateFlow<ChainListUiState> = _uiState.asStateFlow()

// Composable
@Composable
fun ChainListScreen(viewModel: ChainListViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(uiState.error)
        uiState.chainsWithCampaigns.isEmpty() -> EmptyState()
        else -> ChainList(uiState.chainsWithCampaigns)
    }
}
```

### Alternatives Considered
- **LiveData**: 非推奨（Kotlin Flowの方がモダン）
- **State Hoisting only**: 複雑な画面では管理が困難

---

## 10. テスト戦略

### Decision
- **単体テスト**: JUnit 5 + MockK + Turbine（Flow testing）
- **UIテスト**: Compose Testing + Robolectric
- **E2Eテスト**: Firebase Test Lab + Espresso

### Rationale
- **JUnit 5**: 最新のJava/Kotlin標準テストフレームワーク
- **MockK**: Kotlin専用のモッキングライブラリ、コルーチン対応
- **Turbine**: Kotlin Flow専用のテストユーティリティ
- **Compose Testing**: Jetpack Compose公式テストライブラリ
- **Firebase Test Lab**: 実機テスト（Phase3以降で本格利用）

### Test Coverage
- **単体テスト**:
  - ステータス判定ロジック（`getCampaignStatus`）
  - フィルタロジック（お気に入りのみ表示）
  - 1年フィルタロジック
  - ViewModel状態遷移
- **UIテスト**:
  - チェーン店一覧表示
  - キャンペーン一覧表示（チェーン店ごと）
  - お気に入りフィルタのON/OFF
  - 空リスト表示
- **E2Eテスト**:
  - Firestoreとの統合
  - フィルタ選択永続化

### Reference
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [MockK](https://mockk.io/)
- [Turbine](https://github.com/cashapp/turbine)

---

## Summary

| 項目 | 選定技術 | 理由 |
|------|---------|------|
| 言語 | Kotlin | Android公式推奨、Null安全性 |
| UIフレームワーク | Jetpack Compose + Material 3 | モダン、宣言的UI |
| リモートデータ | Firebase Android SDK (Firestore) | Kotlin対応、リアルタイム更新 |
| ローカルデータ | Room Database | オフライン対応、Single Source of Truth |
| 認証 | Firebase Authentication | Googleログイン、AuthStateListener |
| 設定永続化 | DataStore Preferences | 型安全、コルーチン対応 |
| DI | Hilt | Android公式推奨、Jetpack統合 |
| アーキテクチャ | MVVM + Clean Architecture + マルチモジュール | Google推奨、設計力証明 |
| 状態管理 | StateFlow + Compose State | Kotlinネイティブ、Compose統合 |
| テスト | JUnit 5 + MockK + Turbine | Kotlin対応、モダン |
| 品質基盤 | Lint + Detekt + JaCoCo | 静的解析、カバレッジ計測 |
| CI/CD | GitHub Actions | 自動テスト、自動配信 |

全ての技術選定は `.specify/memory/constitution.md` および `docs/adr/001-adopt-portfolio-driven-tech-stack.md` に準拠しています。

### 2025/12/14 再確認結果

Android Developers公式ドキュメントを確認し、上記技術選定が2025年12月時点でも公式推奨に沿っていることを確認。
唯一、JUnit 5は公式採用前だが、コミュニティ標準として広く使用されており問題なし。
