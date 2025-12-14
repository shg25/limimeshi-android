# Tasks: チェーン店一覧（Chain List）

- **Input**: Design documents from `/specs/002-chain-list/`
- **Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, contracts/firestore-schema.md, quickstart.md
- **Tests**: このタスクリストにはテスト作成タスクが含まれています（spec.mdで明示的に要求されているため）
- **Organization**: タスクはユーザーストーリーごとにグループ化され、各ストーリーを独立して実装・テスト可能にしています

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 並列実行可能（異なるファイル、依存関係なし）
- **[Story]**: このタスクが属するユーザーストーリー（例: US1, US2）
- 説明には正確なファイルパスを含める

## Path Conventions

公式ドキュメント「Guide to Android app modularization」準拠（ADR-002参照）:

- **app/**: `app/src/main/java/com/shg25/limimeshi/` - アプリケーションモジュール
- **feature/chainlist/**: `feature/chainlist/src/main/java/com/shg25/limimeshi/feature/chainlist/` - チェーン店一覧機能
- **core/ui/**: `core/ui/src/main/java/com/shg25/limimeshi/core/ui/` - 共通UIコンポーネント
- **core/model/**: `core/model/src/main/java/com/shg25/limimeshi/core/model/` - ドメインモデル
- **core/domain/**: `core/domain/src/main/java/com/shg25/limimeshi/core/domain/` - ドメイン層（UseCase）
- **core/data/**: `core/data/src/main/java/com/shg25/limimeshi/core/data/` - データ層（Repository）
- **core/network/**: `core/network/src/main/java/com/shg25/limimeshi/core/network/` - ネットワーク層（Firebase）
- **core/database/**: `core/database/src/main/java/com/shg25/limimeshi/core/database/` - ローカルデータベース（Room）
- **core/common/**: `core/common/src/main/java/com/shg25/limimeshi/core/common/` - 共通ユーティリティ
- **テスト**: 各モジュールの `src/test/`, `src/androidTest/`

パスはplan.mdのProject Structureセクションに基づく

---

## Phase 1: Setup（共有インフラストラクチャ）

**Purpose**: プロジェクトの初期化とマルチモジュール構造の構築（公式ドキュメント準拠）

- [ ] T001 limimeshi-androidリポジトリを作成（GitHub、ローカルにクローン）
- [ ] T002 [P] Android Studio でKotlin + Jetpack Composeプロジェクトを初期化（Empty Activity with Compose）
- [ ] T003 [P] 必要なパッケージをインストール（Firebase SDK、Hilt、Room、DataStore、JUnit 5、MockK、Turbine、Compose Testing）
- [ ] T004 [P] マルチモジュール構造を作成（公式ドキュメント「Guide to Android app modularization」準拠）:
  - `app/` - アプリケーションモジュール
  - `feature/chainlist/` - チェーン店一覧機能
  - `core/ui/` - 共通UIコンポーネント
  - `core/model/` - ドメインモデル
  - `core/domain/` - ドメイン層（UseCase）
  - `core/data/` - データ層（Repository）
  - `core/network/` - ネットワーク層（Firebase）
  - `core/database/` - ローカルデータベース（Room）
  - `core/common/` - 共通ユーティリティ
- [ ] T005 [P] Firebase設定ファイルを配置 app/google-services.json（Firebase Consoleからダウンロード）
- [ ] T006 [P] Hilt DIセットアップ app/.../di/AppModule.kt、app/.../LimimeshiApp.kt（@HiltAndroidApp）
- [ ] T006a [P] Room Database セットアップ core/database/.../LimimeshiDatabase.kt
- [ ] T006b [P] Navigation Compose セットアップ app/.../navigation/LimimeshiNavHost.kt
- [ ] T006c [P] NetworkModule DIセットアップ core/network/.../di/NetworkModule.kt
- [ ] T006d [P] DatabaseModule DIセットアップ core/database/.../di/DatabaseModule.kt

---

## Phase 2: Foundational（ブロッキング前提条件）

**Purpose**: すべてのユーザーストーリーを実装する前に完了する必要があるコアインフラストラクチャ

**⚠️ CRITICAL**: このフェーズが完了するまで、ユーザーストーリーの作業を開始できません

- [ ] T007 Firestore Security Rulesを確認（/campaigns, /chainsが公開読み取り可能、書き込み不可）
- [ ] T008 Firestoreインデックスを作成（chains: furigana asc、campaigns: chainId + saleStartTime desc）
- [ ] T009 Kotlinデータクラス定義を作成 core/model/.../Chain.kt、Campaign.kt、CampaignStatus.kt、ChainSortOrder.kt（Enum: NEWEST/FURIGANA）、ChainWithCampaigns.kt
- [ ] T009a Room Entity定義を作成 core/database/.../entity/ChainEntity.kt、CampaignEntity.kt（Firestoreモデルからローカルキャッシュ用に変換）
- [ ] T009b Room DAO定義を作成 core/database/.../dao/ChainDao.kt、CampaignDao.kt
- [ ] T010 ステータス判定UseCaseを作成 core/domain/.../GetCampaignStatusUseCase.kt（saleStartTime, saleEndTimeからステータスを判定）

**Checkpoint**: 基盤が整い、ユーザーストーリーの並列実装が可能になります

---

## Phase 3: User Story 1 - チェーン店一覧の閲覧 (Priority: P1) 🎯 MVP

**Goal**: 一般ユーザーがチェーン店を一覧表示し、各チェーン店に紐づくキャンペーン情報をチェーン店単位で確認できる。ソート順は新着順（デフォルト）/ふりがな順から選択可能。

**Independent Test**: チェーン店の一覧表示が正しく動作し、デフォルトで新着順にソートされ、各チェーン店に紐づくキャンペーンが販売開始日時の降順で表示されれば、独立して価値を提供できる。

### Tests for User Story 1

> **NOTE: これらのテストを最初に書き、実装前に失敗することを確認**

- [ ] T011 [P] [US1] ステータス判定UseCaseの単体テストを作成 core/domain/src/test/.../GetCampaignStatusUseCaseTest.kt（「予定」「開始から◯日経過」「終了」の3パターン、販売終了日時未設定時の処理）
- [ ] T012 [P] [US1] チェーン店一覧表示のUIテストを作成 feature/chainlist/src/androidTest/.../ChainListScreenTest.kt（デフォルトで新着順表示、ソート切り替え（新着順↔ふりがな順）、キャンペーンが販売開始日時降順で表示、1年経過キャンペーンは非表示、空リスト時「データなし」表示）

### Implementation for User Story 1

- [ ] T013 [P] [US1] ステータス判定UseCaseを実装 core/domain/.../GetCampaignStatusUseCase.kt（現在時刻とsaleStartTime/saleEndTimeを比較）
- [ ] T014 [US1] ChainRepositoryを作成 core/data/.../repository/ChainRepository.kt（Firestoreからチェーン店一覧を取得、Roomにキャッシュ、1年以内・販売開始日時降順で取得）
- [ ] T014a [US1] FirestoreDataSourceを作成 core/network/.../FirestoreDataSource.kt（Firestore読み取り処理をカプセル化）
- [ ] T014b [US1] GetChainListUseCaseを作成 core/domain/.../GetChainListUseCase.kt（Repository呼び出し、ソートロジック）
- [ ] T015 [US1] ChainListViewModelを作成 feature/chainlist/.../ChainListViewModel.kt（UIステート管理、UseCase呼び出し、ソート順管理、エラーハンドリング）
- [ ] T015a [US1] ChainListUiStateを作成 feature/chainlist/.../ChainListUiState.kt（UI State定義）
- [ ] T016 [US1] チェーン店一覧画面を作成 feature/chainlist/.../ChainListScreen.kt（LazyColumn、ソート選択UI、ローディング状態、エラー状態、空リスト状態）
- [ ] T016a [US1] ソート順選択コンポーネントを作成 core/ui/.../component/SortSelector.kt（Material 3 DropdownMenu、新着順/ふりがな順切り替え）
- [ ] T016b [US1] チェーン店のソートロジックを実装 core/domain/.../GetChainListUseCase.kt（新着順: 最新キャンペーンのsaleStartTime降順、ふりがな順: furigana昇順）
- [ ] T017 [US1] チェーン店カードコンポーネントを作成 feature/chainlist/.../ChainCard.kt（チェーン名、お気に入り登録数、紐づくキャンペーン一覧を表示）
- [ ] T018 [US1] キャンペーン項目コンポーネントを作成 feature/chainlist/.../CampaignItem.kt（キャンペーン名、販売開始日時、販売終了日時（未設定時は非表示）、ステータス、説明を表示）
- [ ] T019 [US1] X Post埋め込みコンポーネントを作成 core/ui/.../component/XPostEmbed.kt（WebViewでTwitter Widgets使用、Post ID抽出、エラーハンドリング）
- [ ] T020 [US1] キャンペーン項目にX Post埋め込みを統合 feature/chainlist/.../CampaignItem.kt（X Post URLがあれば表示、なければ非表示）
- [ ] T021 [US1] 1年経過フィルタを実装 core/data/.../repository/ChainRepository.kt（Firestoreクエリで`whereGreaterThanOrEqualTo("saleStartTime", oneYearAgo)`）
- [ ] T022 [US1] 空リスト時の表示を実装 feature/chainlist/.../ChainListScreen.kt（チェーン店0件時に「データなし」、キャンペーン0件時に「現在キャンペーンはありません」と表示）

**Checkpoint**: この時点で、User Story 1は完全に機能し、独立してテスト可能です（チェーン店一覧表示機能が動作）

---

## Phase 4: User Story 2 - お気に入りチェーン店フィルタ (Priority: P1)

**Goal**: ログインユーザーがお気に入り登録したチェーン店のみを表示し、関心の高いチェーンの新キャンペーンを素早く確認できる。フィルタ選択は次回訪問時も保持される。

**Independent Test**: お気に入りフィルタが正しく動作し、お気に入り登録済みチェーン店のみが表示され、フィルタ選択が次回訪問時も保持されれば、独立して価値を提供できる。

**Dependencies**: User Story 1（チェーン店一覧表示）、003-favorites（お気に入り登録機能）

### Tests for User Story 2

- [ ] T023 [P] [US2] フィルタ・ソート選択永続化の単体テストを作成 core/data/src/test/.../repository/PreferencesRepositoryTest.kt（DataStoreへの保存・読み込み、デフォルト値の処理、ソート順永続化）
- [ ] T024 [P] [US2] お気に入りフィルタ・ソート永続化のUIテストを作成 feature/chainlist/src/androidTest/.../ChainListScreenTest.kt（フィルタON→お気に入りチェーン店のみ表示、フィルタOFF→全チェーン店表示、次回起動時もフィルタ・ソート設定保持）

### Implementation for User Story 2

- [ ] T025 [P] [US2] PreferencesRepositoryを作成 core/data/.../repository/PreferencesRepository.kt（DataStore Preferences、showFavoritesOnly + sortOrder保存・読み込み）
- [ ] T026 [US2] お気に入りフィルタコンポーネントを作成 core/ui/.../component/FavoritesFilter.kt（Material 3 Switch、フィルタON/OFF切り替え）
- [ ] T027 [US2] ChainListViewModelに認証状態を追加 feature/chainlist/.../ChainListViewModel.kt（Firebase Auth AuthStateListener、ログインユーザーのみフィルタ表示）
- [ ] T028 [US2] お気に入りチェーンデータを取得 core/data/.../repository/FavoritesRepository.kt（Firestoreから/users/{userId}/favoritesを取得、chainIdリストを作成）
- [ ] T029 [US2] お気に入りフィルタロジックを実装 core/data/.../repository/ChainRepository.kt（フィルタON時は`whereIn("__name__", favoriteChainIds)`でクエリ、10件制限の考慮）
- [ ] T030 [US2] フィルタ・ソート選択永続化を統合 feature/chainlist/.../ChainListViewModel.kt（初回起動時にDataStoreから設定を読み込み、フィルタ・ソート変更時に保存）
- [ ] T031 [US2] お気に入りチェーン0件時の処理 feature/chainlist/.../ChainListScreen.kt（フィルタON時にお気に入り0件の場合は「お気に入り登録したチェーン店がありません」と表示）

**Checkpoint**: この時点で、User Stories 1 AND 2は両方とも独立して動作します（チェーン店一覧表示＋お気に入りフィルタ）

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: 複数のユーザーストーリーに影響する改善

- [ ] T032 [P] Firestore Security Rulesのデプロイ確認（Firebase Consoleで設定、またはfirebase deployコマンド）
- [ ] T033 [P] Firestoreインデックスのデプロイ確認（Firebase Consoleで設定）
- [ ] T034 [P] パフォーマンス検証（3秒以内の初期表示、モバイル4G環境、X Post埋め込み部分を除く）
- [ ] T035 [P] エラーハンドリングの網羅性確認（Firestoreエラー、X Post埋め込みエラー、ネットワークエラー）
- [ ] T036 [P] アクセシビリティ改善（contentDescription追加、TalkBack対応、コントラスト比確認）
- [ ] T037 [P] Material 3テーマ設定確認 core/ui/.../theme/Theme.kt（ダイナミックカラー、ダークモード対応）
- [ ] T038 [P] テストカバレッジの確認（70%以上のターゲット達成確認）
- [ ] T039 [P] ProGuard/R8設定確認（リリースビルドで動作確認）
- [ ] T040 Google Play Console設定を確認（内部テストトラック、アプリ署名）

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 依存関係なし - 即座に開始可能
- **Foundational (Phase 2)**: Setupの完了に依存 - すべてのユーザーストーリーをブロック
- **User Stories (Phase 3+)**: すべてFoundationalフェーズの完了に依存
  - User Story 1: Foundational完了後に開始可能 - 他のストーリーへの依存なし
  - User Story 2: User Story 1完了後に開始可能 - お気に入り機能（003-favorites）に依存
- **Polish (Final Phase)**: すべての希望するユーザーストーリーの完了に依存

### User Story Dependencies

- **User Story 1 (P1)**: Foundational (Phase 2)完了後に開始可能 - 他のストーリーへの依存なし
- **User Story 2 (P1)**: User Story 1完了後に開始可能 - 003-favorites（お気に入り登録機能）に依存

---

## 成果物の確認

### 総タスク数: 52タスク（公式ドキュメント準拠マルチモジュール対応）

### ユーザーストーリーごとのタスク数:
- **User Story 1（チェーン店一覧の閲覧）**: 17タスク（テスト2 + 実装15）- ソート機能含む、UseCase/DataSource/UiState追加
- **User Story 2（お気に入りチェーン店フィルタ）**: 9タスク（テスト2 + 実装7）

### 各ストーリーの独立テスト基準:
- **User Story 1**: チェーン店の一覧表示が正しく動作し、デフォルトで新着順にソートされ（ふりがな順に切り替え可能）、各チェーン店に紐づくキャンペーンが販売開始日時の降順で表示される
- **User Story 2**: お気に入りフィルタが正しく動作し、お気に入り登録済みチェーン店のみが表示され、フィルタ・ソート選択が次回訪問時も保持される

### 推奨MVPスコープ:
- **Phase 1 + Phase 2 + Phase 3（User Story 1のみ）**: チェーン店一覧表示機能
- User Story 2はお気に入り機能との連携として有用だが、User Story 1完了後に追加可能
