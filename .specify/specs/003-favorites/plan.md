# Implementation Plan: お気に入り登録（Favorites）

- **Branch**: `003-favorites`
- **Date**: 2025-11-28
- **Spec**: [spec.md](./spec.md)
- **Input**: Feature specification from `/specs/003-favorites/spec.md`

## Summary

ログインユーザーがチェーン店をお気に入り登録・解除できる機能。お気に入り登録したチェーンは002のチェーン店一覧フィルタで使用される。お気に入り状態はFirestoreに永続化され、複数デバイス間で同期される。お気に入り登録数を各チェーンに表示し、人気指標として提供。Phase2 MVPで実装。

**技術アプローチ**:
- Kotlin 1.9+ + Jetpack Compose 1.5+ のAndroidアプリケーション
- Firebase Android SDK のFirestore Transactionsでお気に入り登録・解除とカウント更新を原子的に実行
- Material 3 IconButton + Favorite/FavoriteBorderアイコン でUI実装
- Firestore サブコレクション `/users/{userId}/favorites/{chainId}` でお気に入りデータを管理
- Firestore Security Rulesでログインユーザー本人のみ読み書き可能に制限
- Room Database でローカルキャッシュ（002-chain-listと共通）
- Phase2では `get()` による初回読み込みのみ、リアルタイムリスナーはPhase3以降

**ポートフォリオ戦略**（2025/12/07方針変更）:
- 技術要素の網羅的導入（技術幅を証明）
- マルチモジュール構成（002-chain-listと統一）
- 詳細は `docs/adr/001-adopt-portfolio-driven-tech-stack.md` を参照

## Technical Context

- **Language/Version**: Kotlin 1.9+, Jetpack Compose 1.5+
- **Primary Dependencies**: Firebase Android SDK (BOM), Material 3, Hilt
- **Storage**: Firestore（サブコレクション `/users/{userId}/favorites/{chainId}`）
- **Testing**: JUnit 5, MockK, Turbine, Compose Testing, Robolectric
- **Target Platform**: Android（minSdk 26, targetSdk 34）
- **Project Type**: Android（limimeshi-android リポジトリ）
- **Performance Goals**: お気に入り登録・解除操作が1秒以内に完了、UI反映も1秒以内
- **Constraints**: Firestore Transactionの制約（同時実行制御、リトライロジック）
- **Scale/Scope**: Phase2の100ユーザー、平均5〜10件のお気に入り登録、月間3,000 writes（Firestore無料枠内）

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ I. Spec-Driven（仕様駆動開発）
- **Status**: PASS
- **Evidence**: spec.md、plan.md、tasks.md が生成済み

### ✅ II. Test-First（テスト駆動）
- **Status**: PASS
- **Plan**: 単体テスト（お気に入りボタンロジック）、UIテスト（お気に入り登録・解除）を先に作成

### ✅ III. Simplicity（シンプルさ優先）
- **Status**: PASS
- **Evidence**: StateFlow + Compose State + Clean Architecture（ポートフォリオ戦略により、技術幅証明のためマルチモジュール構成を採用、ADR-001参照）

### ✅ IV. Firebase-First
- **Status**: PASS
- **Evidence**: Firebase Android SDK、Firestore、Firebase Authentication を使用

### ✅ V. Legal Risk Zero（法的リスクゼロ）
- **Status**: PASS
- **Evidence**: ユーザー生成データ（お気に入り登録）のみ、スクレイピング不使用

### ✅ VI. Mobile & Performance First
- **Status**: PASS
- **Evidence**: Material 3（モバイルネイティブ）、1秒以内のレスポンス目標、Firestore Transaction最適化

### ✅ VII. Cost Awareness（コスト意識）
- **Status**: PASS
- **Evidence**: Firestore Transaction最適化、月間3,000 writes（無料枠600,000以内）

### ✅ VIII. Security & Privacy First
- **Status**: PASS
- **Evidence**: Firestore Security Rules（ログインユーザー本人のみ読み書き可能）、プライバシー保護

### ✅ IX. Observability（可観測性）
- **Status**: PASS (Phase2向け)
- **Plan**: Firebase Console、Firebase Analytics でお気に入り登録数を監視、エラー率をPhase3で追加

**Overall**: ✅ ALL GATES PASSED

## Project Structure

### Documentation (this feature)

```text
specs/003-favorites/
├── spec.md              # 機能仕様（既存）
├── plan.md              # 実装計画（/speckit-plan コマンドで生成）
├── research.md          # Phase 0 研究（既存）
├── quickstart.md        # Phase 1 研究（既存）
├── contracts/           # Phase 1 研究（既存）
│   └── firestore-schema.md
└── tasks.md             # Phase 2 研究（/speckit-tasks コマンドで生成）
```

### Source Code (limimeshi-android repository)

公式ドキュメント「Guide to Android app modularization」に準拠（ADR-002参照）

```text
limimeshi-android/
├── app/                                              # アプリケーションモジュール
│   ├── src/main/java/com/shg25/limimeshi/
│   │   ├── LimimeshiApp.kt                           # Applicationクラス（@HiltAndroidApp）
│   │   ├── MainActivity.kt                           # メインActivity
│   │   ├── di/
│   │   │   └── AppModule.kt                          # アプリレベルのDI設定
│   │   └── navigation/
│   │       └── LimimeshiNavHost.kt                   # Navigation Compose
│   └── google-services.json                          # Firebase設定（.gitignore）
│
├── feature/                                          # 機能モジュール群
│   ├── chainlist/                                    # チェーン店一覧機能（002で作成済み）
│   │   └── src/main/java/com/shg25/limimeshi/feature/chainlist/
│   │       ├── ChainListScreen.kt                    # チェーン店一覧画面（お気に入りボタン統合）
│   │       ├── ChainListViewModel.kt                 # ViewModel（認証状態追加）
│   │       └── ChainCard.kt                          # チェーン店カード（お気に入りボタン追加）
│   │
│   └── favorites/                                    # お気に入り機能
│       ├── src/test/java/com/shg25/limimeshi/feature/favorites/
│       │   └── ToggleFavoriteUseCaseTest.kt          # UseCase単体テスト
│       └── src/androidTest/java/com/shg25/limimeshi/feature/favorites/
│           └── FavoriteIntegrationTest.kt            # お気に入り統合テスト
│
└── core/                                             # コアモジュール群
    ├── ui/                                           # 共通UIコンポーネント
    │   └── src/main/java/com/shg25/limimeshi/core/ui/
    │       ├── theme/
    │       │   └── Theme.kt                          # Material 3テーマ（002で作成済み）
    │       └── component/
    │           ├── FavoriteButton.kt                 # お気に入りボタンコンポーネント
    │           └── FavoriteCount.kt                  # お気に入り登録数表示コンポーネント
    │
    ├── model/                                        # ドメインモデル
    │   └── src/main/java/com/shg25/limimeshi/core/model/
    │       ├── Chain.kt                              # 002で作成済み
    │       ├── Campaign.kt                           # 002で作成済み
    │       └── Favorite.kt                           # お気に入りデータクラス
    │
    ├── domain/                                       # ドメイン層（UseCase）
    │   └── src/main/java/com/shg25/limimeshi/core/domain/
    │       └── ToggleFavoriteUseCase.kt              # お気に入り登録・解除UseCase
    │
    ├── data/                                         # データ層（Repository）
    │   └── src/main/java/com/shg25/limimeshi/core/data/
    │       └── repository/
    │           ├── ChainRepository.kt                # 002で作成済み
    │           └── FavoritesRepository.kt            # お気に入りリポジトリ
    │
    ├── network/                                      # ネットワーク層（Firebase）
    │   └── src/main/java/com/shg25/limimeshi/core/network/
    │       └── FirestoreDataSource.kt                # Firestore DataSource（002で作成済み）
    │
    └── database/                                     # ローカルデータベース（Room）
        └── src/main/java/com/shg25/limimeshi/core/database/
            ├── dao/
            │   └── FavoriteDao.kt                    # お気に入りDAO
            └── entity/
                └── FavoriteEntity.kt                 # お気に入りEntity
```

**Structure Decision**: 公式ドキュメント「Guide to Android app modularization」準拠（ADR-002参照）
- **core/ui/component/**: FavoriteButton、FavoriteCount（共通コンポーネント）
- **core/model/**: Favorite（ドメインモデル）
- **core/domain/**: ToggleFavoriteUseCase
- **core/data/repository/**: FavoritesRepository
- **core/network/**: FirestoreDataSource（Firebase呼び出し）
- **core/database/**: FavoriteDao、FavoriteEntity（Room）
- **feature/chainlist/**: お気に入りボタンの統合（002と共有）

## Implementation Phases

### Phase 0: Research（研究）✅

**成果物**: `research.md`

**調査項目**:
1. Firebase Authentication（ログイン状態の管理）
2. Firestore（お気に入りデータの永続化）
3. Firestore Transactions（お気に入り登録数の集約データ更新）
4. Jetpack Compose UI（お気に入りボタン）

### Phase 1: Design & Contracts（設計）✅

**成果物**:
- ✅ `contracts/firestore-schema.md`: Firestoreスキーマ（読み書き可能）
- ✅ `quickstart.md`: 開発環境構築、実装例、テスト例

### Phase 2: Implementation（tasks.md生成）

**Phase 2は `/speckit-tasks` コマンドで tasks.md 生成を実行**

## Dependencies

### 前提条件
- **002-chain-list**: チェーン店一覧画面が実装済み（お気に入りフィルタは003完了後に追加）
- **001-admin-panel**: チェーン店マスタが登録済み（/chainsコレクション）

### 提供データ
- **003 → 002**: お気に入りデータ（/users/{userId}/favorites）を002のフィルタ機能で使用

## Success Metrics

### Phase2完了時の基準
- ✅ お気に入り登録・解除操作が1秒以内に完了する（モバイル4G環境）
- ✅ お気に入り登録・解除操作が正常に完了する確率が99%以上である
- ✅ お気に入り登録状態が複数デバイス間で5秒以内に同期される（アプリ再起動で）
- ✅ 未ログインユーザーがお気に入り登録を試みた場合、100%の確率で拒否される（ボタン無効化）
- ✅ お気に入り登録数の表示が登録・解除操作後1秒以内に更新される
- ✅ 単体テストカバレッジ70%以上
- ✅ UIテストが全て合格

## Notes

- **読み書き可能**: 003-favoritesはお気に入りデータの読み書きを行う
- **002との連携**: 002-chain-listはお気に入りデータを読み取り専用で使用
- **001との整合性**: /chainsのスキーマは001-admin-panelと完全一致（favoriteCountフィールドを追加）
- **Transaction使用**: お気に入り登録・解除時はTransactionを使用してデータ整合性を保証
- **集約データ**: favoriteCountは集約データとして管理（リアルタイムカウントではない）
- **Phase2**: get()による初回読み込みのみ、リアルタイムリスナーはPhase3以降
- **Android優先**: Phase2ではAndroidアプリを優先（Webアプリ対応はPhase3以降）
