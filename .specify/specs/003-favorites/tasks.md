# Tasks: お気に入り登録（Favorites）

**Input**: Design documents from `/specs/003-favorites/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, contracts/firestore-schema.md, quickstart.md  

**Tests**: このタスクリストにはテスト作成タスクが含まれています（spec.mdで明示的に要求されているため）

**Organization**: タスクはユーザーストーリーごとにグループ化され、各ストーリーを独立して実装・テスト可能にしています

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 並列実行可能（異なるファイル、依存関係なし）
- **[Story]**: このタスクが属するユーザーストーリー（例: US1, US2）
- 説明には正確なファイルパスを含める

## Path Conventions

- **limimeshi-android repository**: `app/src/main/java/com/shg25/limimeshi/`, `app/src/test/`, `app/src/androidTest/`
- パスはplan.mdのProject Structureセクションに基づく

---

## Phase 1: Setup（共有インフラストラクチャ）

**Purpose**: プロジェクトの初期化と基本構造の構築（002-chain-listで大部分が完了済み）

- [ ] T001 limimeshi-androidリポジトリのプロジェクト構造を確認（ui/, data/repository/, data/model/, util/, di/が存在することを確認）
- [ ] T002 [P] Material Icons依存関係が追加されていることを確認（androidx.compose.material:material-icons-extended）、未追加の場合は追加

---

## Phase 2: Foundational（ブロッキング前提条件）

**Purpose**: すべてのユーザーストーリーを実装する前に完了する必要があるコアインフラストラクチャ

**⚠️ CRITICAL**: このフェーズが完了するまで、ユーザーストーリーの作業を開始できません

- [ ] T003 Firestore Security Rulesを更新（/users/{userId}/favorites/{chainId}の読み書きルール、/chains/{chainId}のfavoriteCount更新ルール）
- [ ] T004 Kotlinデータクラス定義を作成 data/model/Favorite.kt（Favorite型、FavoriteState型）
- [ ] T005 Chain.ktにfavoriteCountフィールドを追加 data/model/Chain.kt（Int型、デフォルト0）

**Checkpoint**: 基盤が整い、ユーザーストーリーの並列実装が可能になります

---

## Phase 3: User Story 1 - チェーン店のお気に入り登録・解除 (Priority: P1) 🎯 MVP

**Goal**: ログインユーザーがチェーン店をお気に入り登録・解除でき、お気に入り状態が即座にUIに反映される。お気に入り登録したチェーンは002のチェーン店一覧フィルタで使用される。

**Independent Test**: お気に入り登録・解除が正しく動作し、状態がFirestoreに永続化され、UIに即座に反映されれば、独立して価値を提供できる。

### Tests for User Story 1

> **NOTE: これらのテストを最初に書き、実装前に失敗することを確認**

- [ ] T006 [P] [US1] お気に入りボタンコンポーネントの単体テストを作成 app/src/test/java/.../ui/components/FavoriteButtonTest.kt（ログインユーザーのボタン有効化、未ログインユーザーのボタン無効化、登録・解除状態の表示切り替え、ローディング状態）
- [ ] T007 [P] [US1] お気に入りリポジトリの単体テストを作成 app/src/test/java/.../data/repository/FavoritesRepositoryTest.kt（登録・解除処理、Firestore Transaction確認）

### Implementation for User Story 1

- [ ] T008 [P] [US1] お気に入りボタンコンポーネントを作成 ui/components/FavoriteButton.kt（Material 3 IconButton + Favorite/FavoriteBorderアイコン、ローディング状態、無効化状態）
- [ ] T009 [US1] FavoritesRepositoryを作成 data/repository/FavoritesRepository.kt（Firestoreからお気に入り状態を取得、登録・解除処理、Transactionでカウント更新）
- [ ] T010 [US1] ChainListViewModelに認証状態を追加 ui/chain/ChainListViewModel.kt（Firebase Auth AuthStateListener、ログインユーザーのみお気に入りボタン有効化）
- [ ] T011 [US1] ChainListViewModelにお気に入り状態を追加 ui/chain/ChainListViewModel.kt（お気に入りチェーンIDリストを管理、登録・解除処理）
- [ ] T012 [US1] ChainCardへのお気に入りボタン統合 ui/chain/ChainCard.kt（FavoriteButtonコンポーネントを追加、chainIdごとのお気に入り状態を表示）
- [ ] T013 [US1] エラーハンドリングの追加 ui/chain/ChainListScreen.kt（Snackbar + SnackbarHostState でエラーメッセージ表示、permission-denied/unavailableエラーの処理）
- [ ] T014 [US1] ローディング状態の管理 ui/chain/ChainListViewModel.kt（chainIdごとのローディング状態をMapで管理）

**Checkpoint**: この時点で、User Story 1は完全に機能し、独立してテスト可能です（お気に入り登録・解除機能が動作）

---

## Phase 4: User Story 2 - お気に入り登録数の表示 (Priority: P2)

**Goal**: 各チェーン店のお気に入り登録数を表示し、人気のチェーンを視覚的に示す。

**Independent Test**: お気に入り登録数が正しくカウントされ、各チェーン店に表示されれば、独立して価値を提供できる。

**Dependencies**: User Story 1（お気に入り登録・解除機能）

### Tests for User Story 2

- [ ] T015 [P] [US2] お気に入り登録数表示の単体テストを作成 app/src/test/java/.../ui/components/FavoriteCountTest.kt（0件時の非表示、1件以上の表示確認）

### Implementation for User Story 2

- [ ] T016 [P] [US2] お気に入り登録数表示コンポーネントを作成 ui/components/FavoriteCount.kt（count=0の場合は非表示、それ以外は「♥ {count}人がお気に入り登録」を表示）
- [ ] T017 [US2] ChainCardへのお気に入り登録数の追加 ui/chain/ChainCard.kt（FavoriteCountコンポーネントを追加、chain.favoriteCountを渡す）
- [ ] T018 [US2] お気に入りボタンでの登録数ローカル更新 ui/chain/ChainListViewModel.kt（登録・解除時にfavoriteCountをローカルステートで即座に更新、Optimistic UI）

**Checkpoint**: この時点で、User Stories 1 AND 2は両方とも独立して動作します（お気に入り登録・解除＋登録数表示）

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: 複数のユーザーストーリーに影響する改善

- [ ] T019 [P] Firestore Security Rulesのデプロイ確認（Firebase Consoleで設定、またはfirebase deployコマンド）
- [ ] T020 [P] パフォーマンス検証（1秒以内の操作完了、1秒以内のUI反映を確認、モバイル4G環境）
- [ ] T021 [P] エラーハンドリングの網羅性確認（permission-denied, unavailable, network errorのケース）
- [ ] T022 [P] アクセシビリティ改善（contentDescription追加、TalkBack対応、コントラスト比確認）
- [ ] T023 [P] 複数デバイス同期の確認（別デバイスでログイン後、アプリ再起動で5秒以内に同期されることを確認）
- [ ] T024 [P] テストカバレッジの確認（70%以上のターゲット達成確認）
- [ ] T025 [P] ProGuard/R8設定確認（リリースビルドで動作確認）
- [ ] T026 quickstart.mdの検証（手順通りに動作するか確認）

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 依存関係なし - 即座に開始可能（002-chain-listで大部分完了済み）
- **Foundational (Phase 2)**: Setupの完了に依存 - すべてのユーザーストーリーをブロック
- **User Stories (Phase 3+)**: すべてFoundationalフェーズの完了に依存
  - User Story 1: Foundational完了後に開始可能 - 他のストーリーへの依存なし
  - User Story 2: Foundational完了後に開始可能 - User Story 1と統合し、お気に入り登録・解除機能に依存
- **Polish (Final Phase)**: すべての希望するユーザーストーリーの完了に依存

### User Story Dependencies

- **User Story 1 (P1)**: Foundational (Phase 2)完了後に開始可能 - 他のストーリーへの依存なし
- **User Story 2 (P2)**: Foundational (Phase 2)完了後に開始可能 - User Story 1（お気に入り登録・解除）に依存

---

## 成果物の確認

### 総タスク数: 26タスク

### ユーザーストーリーごとのタスク数:
- **User Story 1（チェーン店のお気に入り登録・解除）**: 9タスク（テスト2 + 実装7）
- **User Story 2（お気に入り登録数の表示）**: 4タスク（テスト1 + 実装3）

### 各ストーリーの独立テスト基準:
- **User Story 1**: お気に入り登録・解除が正しく動作し、状態がFirestoreに永続化され、UIに即座に反映される
- **User Story 2**: お気に入り登録数が正しくカウントされ、各チェーン店に表示される

### 推奨MVPスコープ:
- **Phase 1 + Phase 2 + Phase 3（User Story 1のみ）**: お気に入り登録・解除機能
- User Story 2は人気指標として有用だが、User Story 1完了後に追加可能
