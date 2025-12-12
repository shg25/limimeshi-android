# Architecture

本プロジェクトは、Android Developersが推奨するモダンアーキテクチャと、Clean Architectureの思想を統合した構成を採用する

UI・ビジネスロジック・データアクセスの責務を明確に分離し、テスト可能性と拡張性の高いAndroidアプリ基盤を提供する

# 1. 全体構成（High-level Architecture）

```
┌──────────────────────────────────┐
│ Presentation Layer │ ← Jetpack Compose / ViewModel
└───────────────▲──────────────────┘
│ (UiState / Events)
┌───────────────┴──────────────────┐
│ Domain Layer │ ← UseCase / Pure Kotlin
└───────────────▲──────────────────┘
│ (Repository Interface)
┌───────────────┴──────────────────┐
│ Data Layer │ ← Repository / Remote / Local
└──────────────────────────────────┘
```

Android Developersの推奨構造に、Clean Architectureの依存方向ルールを適用

# 2. Presentation Layer（UI & ViewModel）

## 2.1 UI（Jetpack Compose）

- UIは**関数コンポーネント（@Composable）**として構築
- Material3をベースにDesignsystem（core:designsystem）を活用
- UI stateはimmutable data classで表現し、UIは状態に基づいて描画

### 責務
- ユーザ操作のイベント発行
- ViewModelのUiStateを描画
- Navigation遷移
- 副作用（Side-effects）はLaunchedEffect / DisposableEffectで管理

## 2.2 ViewModel（MVVM）

ViewModelはPresentation層の中心で、UI stateとイベント処理を担う

### 責務
- UiStateの保持と更新
- UseCaseの呼び出し
- 非同期処理のスコープ管理（viewModelScope）
- エラーハンドリング
- SavedStateHandleの利用による状態の永続化

### 利用技術
- Kotlin Coroutines（structured concurrency）
- Kotlin Flow（StateFlow / SharedFlow）

### イメージ図

```
UI (Compose)
↓ events
ViewModel
↓ call
UseCase
```

# 3. Domain Layer（UseCase）

アプリのビジネスロジック（ドメインルール）を表現する純粋Kotlin層

### 特徴
- Androidフレームワーク非依存
- UIやデータソースの詳細を知らない
- 単体テストが容易

### UseCaseの役割
- 入力（params）を取り、Repository（interface）を通じてデータを取得・操作し、結果をPresentationに返す

### 依存の方向
- Domain →（依存しない）
- Data → Domain
- Presentation → Domain

# 4. Data Layer（Repository / Local / Remote）

## 4.1 Repository

ドメインで定義された`Repository Interface`を実装する層

### 責務
- Remote（API）とLocal（DB/Cache）の調停
- データ変換（DTO → Domain Model）
- オフライン対応

### Repositoryの理想フロー

```
RemoteDataSource
↓
Mapper → Domain Model
↓
LocalDataSource
↓
Repository
```

## 4.2 Remote DataSource

- Retrofit + OkHttp
- Moshi or Kotlin Serialization
- エラーハンドリング（HTTP / Network / JSON）

## 4.3 Local DataSource

- Room
- Entity / DAOを保持
- Flowを利用したreactiveなデータ更新

# 5. Flow / Coroutinesの使い分け

| 用途 | 技術 |
|------|------|
| UI状態の通知 | StateFlow |
| UIで一度だけ発火するイベント | SharedFlow（EventChannel） |
| 非同期単発処理 | suspend関数 |
| データストリーム | Flow |
| エラーハンドリング | `catch` / `retry` / `onCompletion` |

# 6. Navigation（Compose）

- Navigation Composeを採用
- 画面遷移は`Screen` sealed classで管理
- 引数はNavType / Parcelableで扱う
- 複雑化時は階層的なNavGraphに分割

# 7. DI（Hilt）

### 利用目的
- Repository / UseCase / DataSourceの依存注入
- appモジュールの膨張防止
- テスト可能性の確保

```
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule
```

# 8. 状態保持（State Restoration）

Androidフレームワーク理解を示す重要要素

### 利用する技術
- SavedStateHandle
- rememberSaveable
- ViewModelでの永続化
- Process death時の復元（Navigationと連携）

# 9. WorkManager（バックグラウンド）

必要に応じて以下を実装

- データ同期
- キャッシュ更新
- スケジュールタスク

```
PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.DAYS)
```

# 10. グラフ描画（UI応用）

Compose Charts利用

- 折れ線グラフ（推移）
- 円グラフ（カテゴリの割合）

技術的意図
- ComposeでのUIカスタマイズ力
- 状態変化による再描画
- 複雑UIの実装能力の証明

# 11. アーキ構成のメリット

- 責務分離がはっきりしており拡張しやすい
- テスト容易性が高い（特にDomain）
- マルチモジュールとの相性が良い
- AIコーディングが使いやすい（層ごとに責務が明確）
- 大規模アプリに発展させる余地がある
- モダンAndroidの潮流に沿っているため評価が高い

# 12. 参考・設計規範

- Android Developers Architecture Guide
  - https://developer.android.com/topic/architecture
- Compose UI Best Practices
- Kotlin Coroutines Best Practices
- Now in Android（公式サンプル）
