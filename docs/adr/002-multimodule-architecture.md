# ADR-002: Multi-Module Architecture

- **Date**: 2025-12-14
- **Status**: Accepted
- **Deciders**: shg25

## Context

ADR-001でマルチモジュール構成の採用を決定。本ADRでは、具体的なモジュール構成とその根拠、および今後の拡張指針を記録する。

## References

### 公式ドキュメント（Primary）

1. **Guide to Android app modularization** - Android Developers公式
   - https://developer.android.com/topic/modularization
   - モジュール化の原則、モジュールタイプ、ベストプラクティス
   - **本プロジェクトの主要な参照元**

2. **Guide to app architecture** - Android Developers公式
   - https://developer.android.com/topic/architecture
   - UI Layer、Domain Layer、Data Layerの設計指針

### 参考実装（Secondary）

3. **Now in Android (NIA)** - Google公式サンプルアプリ
   - https://github.com/android/nowinandroid
   - 公式ドキュメントの実装例として参考
   - ただし、本プロジェクトは公式ドキュメントの構成を優先

## Decision

### モジュール構成

公式ドキュメント「Guide to Android app modularization」に準拠した構成を採用。

```
limimeshi-android/
├── app/                    # アプリケーションモジュール
│
├── feature/                # 機能モジュール群
│   ├── chainlist/          # チェーン店一覧機能（002）
│   └── favorites/          # お気に入り機能（003）
│
└── core/                   # コアモジュール群
    ├── ui/                 # 共通UIコンポーネント
    ├── data/               # データ層（Repository、DataSource）
    ├── model/              # ドメインモデル
    ├── domain/             # ドメイン層（UseCase）
    ├── network/            # ネットワーク層（Firebase/API）
    ├── database/           # ローカルデータベース（Room）
    └── common/             # 共通ユーティリティ
```

### 公式ドキュメントとの対応

| 公式ドキュメントのモジュールタイプ | 本プロジェクトのモジュール |
|----------------------------------|---------------------------|
| app module | app/ |
| feature modules | feature/chainlist/, feature/favorites/ |
| core modules | core/ui/, core/data/, core/model/, etc. |

### 各モジュールの責務

| モジュール | 責務 | 公式ドキュメントの分類 |
|-----------|------|----------------------|
| app | DI統合、Navigation、MainActivity | app module |
| feature:chainlist | チェーン店一覧画面、ViewModel | feature module |
| feature:favorites | お気に入り機能、ViewModel | feature module |
| core:ui | Theme、共通Composable | core module (UI) |
| core:data | Repository実装 | core module (Data) |
| core:model | ドメインモデル（data class） | core module (Model) |
| core:domain | UseCase、ビジネスロジック | core module (Domain) |
| core:network | Firebase DataSource | core module (Network) |
| core:database | Room Database、DAO | core module (Database) |
| core:common | ユーティリティ、拡張関数 | core module (Common) |

### 依存関係

公式ドキュメント「Dependency flow」に準拠：

```
┌─────────────────────────────────────────────────────────┐
│                         app                              │
│  (すべてのモジュールを統合、DIの設定)                      │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    feature modules                       │
│  chainlist, favorites                                    │
│  (画面、ViewModel、UIロジック)                           │
└─────────────────────────────────────────────────────────┘
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  core:domain │  │   core:ui    │  │  core:model  │
│  (UseCase)   │  │ (Theme,共通) │  │ (データクラス)│
└──────────────┘  └──────────────┘  └──────────────┘
          │
          ▼
┌──────────────┐
│  core:data   │
│ (Repository) │
└──────────────┘
          │
    ┌─────┴─────┐
    ▼           ▼
┌────────┐  ┌──────────┐
│network │  │ database │
│(Firebase)│ │  (Room)  │
└────────┘  └──────────┘
          │
          ▼
┌──────────────┐
│ core:common  │
│ (ユーティリティ)│
└──────────────┘
```

### 依存関係のルール（公式ドキュメント準拠）

1. **feature → core**: featureモジュールはcoreモジュールに依存
2. **feature ↛ feature**: featureモジュール同士は依存しない
3. **core → core**: coreモジュール同士は依存可能（ただし循環依存は禁止）
4. **app → all**: appモジュールはすべてのモジュールに依存

## Extension Guidelines

### 新しい機能を追加する場合

公式ドキュメント「Feature modules」セクションに準拠。

1. **feature/モジュールを作成**
   ```
   feature/newfeature/
   ├── src/main/java/com/shg25/limimeshi/feature/newfeature/
   │   ├── NewFeatureScreen.kt          # UI（Composable）
   │   ├── NewFeatureViewModel.kt       # ViewModel
   │   ├── NewFeatureUiState.kt         # UI State
   │   └── navigation/
   │       └── NewFeatureNavigation.kt  # Navigation定義
   ├── src/test/java/...
   │   └── NewFeatureViewModelTest.kt   # 単体テスト
   ├── src/androidTest/java/...
   │   └── NewFeatureScreenTest.kt      # UIテスト
   └── build.gradle.kts
   ```

2. **settings.gradle.ktsに追加**
   ```kotlin
   include(":feature:newfeature")
   ```

3. **app/build.gradle.ktsに依存関係を追加**
   ```kotlin
   implementation(project(":feature:newfeature"))
   ```

4. **app/.../navigation/LimimeshiNavHost.ktにナビゲーションを追加**

### 新しいリモートデータソースを追加する場合

公式ドキュメント「Data layer」セクションに準拠。

1. **core/network/に追加**
   ```
   core/network/src/main/java/com/shg25/limimeshi/core/network/
   ├── FirestoreDataSource.kt       # 既存
   └── NewApiDataSource.kt          # 新規追加
   ```

2. **core/data/repository/にRepositoryを追加または更新**
   ```
   core/data/src/main/java/com/shg25/limimeshi/core/data/repository/
   └── NewRepository.kt
   ```

3. **必要に応じてcore/domain/にUseCaseを追加**

### ローカルデータベースを追加する場合

1. **core/database/に追加**
   ```
   core/database/src/main/java/com/shg25/limimeshi/core/database/
   ├── LimimeshiDatabase.kt         # Database定義
   ├── dao/
   │   ├── ChainDao.kt              # 既存
   │   └── NewDao.kt                # 新規追加
   └── entity/
       ├── ChainEntity.kt           # 既存
       └── NewEntity.kt             # 新規追加
   ```

### 共通UIコンポーネントを追加する場合

公式ドキュメント「Core modules」セクションに準拠。

1. **core/ui/に追加**
   ```
   core/ui/src/main/java/com/shg25/limimeshi/core/ui/
   ├── theme/
   │   ├── Theme.kt
   │   ├── Color.kt
   │   └── Type.kt
   └── component/
       ├── FavoritesFilter.kt       # 既存
       └── NewComponent.kt          # 新規追加
   ```

2. **判断基準**:
   - 複数のfeatureで使用 → core:ui に配置
   - 単一のfeatureでのみ使用 → そのfeature内に配置

### 新しいドメインモデルを追加する場合

1. **core/model/に追加**
   ```
   core/model/src/main/java/com/shg25/limimeshi/core/model/
   ├── Chain.kt                     # 既存
   └── NewModel.kt                  # 新規追加
   ```

2. **Room Entityが必要な場合** → core/database/entity/に追加（core/modelとは別）

### UseCaseを追加する場合

公式ドキュメント「Domain layer」セクションに準拠。

1. **core/domain/に追加**
   ```
   core/domain/src/main/java/com/shg25/limimeshi/core/domain/
   ├── GetChainListUseCase.kt       # 既存
   └── NewUseCase.kt                # 新規追加
   ```

2. **命名規則**: `動詞 + 名詞 + UseCase`
   - 例: `GetChainListUseCase`, `ToggleFavoriteUseCase`, `SyncDataUseCase`

3. **UseCaseの責務**:
   - 単一のビジネスロジックをカプセル化
   - Repositoryを呼び出し、データを加工
   - ViewModelから呼び出される

## Consequences

### Positive

- **公式ドキュメント準拠**: 面接時に「Android公式のアーキテクチャガイドに準拠」と説明可能
- **明確な責務分離**: 各モジュールの役割が公式ドキュメントで定義されている
- **スケーラビリティ**: 新機能追加時のパターンが確立されている
- **テスタビリティ**: 各レイヤーが独立しているため、単体テストが容易
- **ビルド時間最適化**: 変更のあったモジュールのみ再ビルド

### Negative

- **初期セットアップの複雑さ**: モジュール数が多い
- **ボイラープレート**: 各モジュールにbuild.gradle.ktsが必要
- **学習コスト**: モジュール間の依存関係の理解が必要

### Risks

- **過度な分割**: 小規模プロジェクトでは過剰な可能性
  - 緩和策: 公式ドキュメントに準拠することで、設計判断の説明が容易

## Notes

- 公式ドキュメント更新時は本ADRを見直す
- NIAの更新も参考にするが、公式ドキュメントを優先する
- Phase2完了後、実装経験を踏まえて本ADRを更新する

## Appendix: Module Packages

各モジュールのパッケージ名：

| モジュール | パッケージ |
|-----------|-----------|
| app | com.shg25.limimeshi |
| feature:chainlist | com.shg25.limimeshi.feature.chainlist |
| feature:favorites | com.shg25.limimeshi.feature.favorites |
| core:ui | com.shg25.limimeshi.core.ui |
| core:data | com.shg25.limimeshi.core.data |
| core:model | com.shg25.limimeshi.core.model |
| core:domain | com.shg25.limimeshi.core.domain |
| core:network | com.shg25.limimeshi.core.network |
| core:database | com.shg25.limimeshi.core.database |
| core:common | com.shg25.limimeshi.core.common |
