# ADR-002: Multi-Module Architecture

- **Date**: 2025-12-14
- **Status**: Accepted
- **Deciders**: shg25

## Context

ADR-001でマルチモジュール構成の採用を決定。本ADRでは、具体的なモジュール構成とその根拠、および今後の拡張指針を記録する。

## References

### 公式リファレンス

1. **Now in Android (NIA)** - Google公式サンプルアプリ
   - https://github.com/android/nowinandroid
   - モダンAndroidアプリのリファレンス実装
   - マルチモジュール構成のベストプラクティス

2. **Guide to Android app modularization** - Android Developers公式
   - https://developer.android.com/topic/modularization
   - モジュール化の原則とパターン

### NIAとの差異

本プロジェクトはNIAを参考にしつつ、規模に合わせて簡略化：

| 項目 | NIA | 本プロジェクト |
|------|-----|---------------|
| core/network | あり | なし（Firebaseで代替） |
| core/database | あり | core/data内に統合 |
| core/datastore | あり | core/data内に統合 |
| sync | あり | なし（Phase2では不要） |
| テストモジュール | 複数 | 各モジュール内に配置 |

## Decision

### モジュール構成

```
limimeshi-android/
├── app/                    # アプリケーションエントリポイント
├── core/
│   ├── designsystem/       # 共通UI（Theme、Components）
│   ├── model/              # ドメインモデル
│   ├── domain/             # UseCase
│   ├── data/               # Repository実装、DataSource
│   └── common/             # 共通ユーティリティ
└── feature/
    ├── chainlist/          # チェーン店一覧機能（002）
    └── favorites/          # お気に入り機能（003）
```

### 各モジュールの責務

| モジュール | 責務 | 依存先 |
|-----------|------|--------|
| app | DI統合、Navigation、エントリポイント | 全モジュール |
| core:designsystem | Theme、共通Composable | core:model |
| core:model | ドメインモデル（data class） | なし |
| core:domain | UseCase、ビジネスロジック | core:model, core:data |
| core:data | Repository、DataSource、Room | core:model |
| core:common | ユーティリティ、拡張関数 | なし |
| feature:* | 画面、ViewModel | core:* |

### 依存関係の原則

```
feature → core:domain → core:data → core:model
              ↓
        core:designsystem
              ↓
          core:common
```

- **feature**はcore:domainを通じてデータにアクセス（直接core:dataに依存しない）
- **core:domain**はインターフェースを定義し、core:dataが実装
- **core:model**は他のモジュールに依存しない（純粋なデータクラス）

## Extension Guidelines

### 新しい機能を追加する場合

1. **feature/モジュールを作成**
   ```
   feature/newfeature/
   ├── src/main/java/.../feature/newfeature/
   │   ├── NewFeatureScreen.kt
   │   ├── NewFeatureViewModel.kt
   │   └── navigation/
   │       └── NewFeatureNavigation.kt
   ├── src/test/
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

4. **LimimeshiNavHost.ktにナビゲーションを追加**

### 新しいデータソースを追加する場合

1. **core/data/datasource/に追加**
   ```
   core/data/src/main/java/.../datasource/
   ├── remote/
   │   └── NewApiDataSource.kt    # 新規追加
   └── local/
       └── NewDao.kt              # 新規追加
   ```

2. **core/data/repository/にRepositoryを追加または更新**

3. **必要に応じてcore/domain/にUseCaseを追加**

### 共通UIコンポーネントを追加する場合

1. **core/designsystem/component/に追加**
   ```
   core/designsystem/src/main/java/.../component/
   └── NewComponent.kt
   ```

2. **複数のfeatureで使用する場合のみcore:designsystemに配置**
3. **単一featureでのみ使用する場合はfeature内に配置**

### 新しいドメインモデルを追加する場合

1. **core/model/に追加**
   ```
   core/model/src/main/java/.../
   └── NewModel.kt
   ```

2. **Room Entityが必要な場合はcore/data/datasource/local/entity/に追加**

### UseCaseを追加する場合

1. **core/domain/に追加**
   ```
   core/domain/src/main/java/.../
   └── NewUseCase.kt
   ```

2. **命名規則**: `動詞 + 名詞 + UseCase`
   - 例: `GetChainListUseCase`, `ToggleFavoriteUseCase`

## Consequences

### Positive

- **関心の分離**: 各モジュールが明確な責務を持つ
- **ビルド時間の最適化**: 変更のあったモジュールのみ再ビルド
- **テスタビリティ**: モジュール単位でのテストが容易
- **スケーラビリティ**: 新機能の追加が容易

### Negative

- **初期セットアップの複雑さ**: settings.gradle.kts、依存関係の管理
- **ボイラープレート**: 各モジュールにbuild.gradle.ktsが必要
- **学習コスト**: モジュール間の依存関係の理解が必要

### Risks

- **過度な分割**: 小規模プロジェクトでは過剰な可能性
  - 緩和策: Phase2では最小限のモジュール構成から開始

## Notes

- Phase2完了後、必要に応じてモジュール構成を見直す
- NIAの更新に追従し、ベストプラクティスを取り入れる
- 新しいAndroid公式ガイダンスが出た場合は本ADRを更新する
