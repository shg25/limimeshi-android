# ADR-001: Adopt Portfolio-Driven Tech Stack

- **Date**: 2025-12-14
- **Status**: Accepted
- **Deciders**: shg25

## Context

limimeshi-androidプロジェクトの目的が「Androidエンジニアとしての専門性・技術力の証明」に変更された（2025/12/07）。

従来のYAGNI原則（必要になるまで実装しない）ではなく、ポートフォリオとして技術幅を証明するため、多くの技術要素を網羅的に導入する方針に変更。

## Decision

以下の技術スタックを採用する：

### アーキテクチャ

| 項目 | 選定 | 理由 |
|------|------|------|
| パターン | MVVM + Clean Architecture | Android公式推奨、テスタビリティ |
| モジュール構成 | マルチモジュール | 設計力の証明、責務分離 |
| 状態管理 | StateFlow + Compose State | Kotlin標準、Compose統合 |
| DI | Hilt | Android公式推奨、67%以上の採用率 |

### UI/プレゼンテーション

| 項目 | 選定 | 理由 |
|------|------|------|
| UIフレームワーク | Jetpack Compose | Android公式推奨、宣言的UI |
| デザインシステム | Material 3 | 最新ガイドライン、Dynamic Color |
| ナビゲーション | Navigation Compose | 公式推奨 |

### データ層

| 項目 | 選定 | 理由 |
|------|------|------|
| リモートデータ | Firebase SDK (Firestore) | プロジェクト要件、Kotlin対応 |
| ローカルキャッシュ | Room Database | オフライン対応、Single Source of Truth |
| 設定永続化 | DataStore Preferences | SharedPreferences後継 |

### 非同期処理

| 項目 | 選定 | 理由 |
|------|------|------|
| 並行処理 | Kotlin Coroutines | Kotlin標準、構造化並行 |
| データストリーム | Kotlin Flow | リアクティブ、Compose統合 |

### テスト

| 項目 | 選定 | 理由 |
|------|------|------|
| 単体テスト | JUnit 5 | モダン（公式はJUnit 4だがコミュニティ推奨） |
| モック | MockK | Kotlin専用、コルーチン対応 |
| Flow テスト | Turbine | Flow専用テストユーティリティ |
| UIテスト | Compose Testing | 公式 |

### 品質基盤

| 項目 | 選定 | 理由 |
|------|------|------|
| 静的解析（Lint） | Android Lint | 公式 |
| 静的解析（Kotlin） | Detekt | Kotlin専用 |
| カバレッジ | JaCoCo | 業界標準 |

### CI/CD

| 項目 | 選定 | 理由 |
|------|------|------|
| CI | GitHub Actions | プロジェクト標準 |
| テスト配信 | Firebase App Distribution | Firebase統合 |
| 本番配信 | Google Play Console | 標準 |

### マルチモジュール構成

```
app/                    # アプリケーションエントリポイント
core/
  ├── designsystem/     # 共通UI（Theme、Components）
  ├── model/            # ドメインモデル
  ├── domain/           # UseCase
  ├── data/             # Repository実装、DataSource
  └── common/           # 共通ユーティリティ
feature/
  ├── chainlist/        # チェーン店一覧機能
  └── favorites/        # お気に入り機能
```

## Consequences

### Positive

- **技術幅の証明**: 多くのライブラリ・パターンの使用経験を示せる
- **設計力の証明**: マルチモジュール構成で大規模開発への対応力を示せる
- **運用力の証明**: CI/CD、テスト、品質基盤で非機能要件への対応力を示せる
- **公式推奨準拠**: Android Developersの推奨に沿った選定で信頼性が高い

### Negative

- **実装コスト増**: シンプルなMVPより実装工数が増加
- **学習コスト**: 一部の技術（JUnit 5等）は公式採用前のため、追加の学習が必要
- **複雑性**: マルチモジュール構成は初期セットアップが複雑

### Risks

- **JUnit 5**: 公式採用前だが、mannodermausプラグインで安定稼働
- **過剰設計**: MVPとしては過剰だが、ポートフォリオ目的のため許容

## References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Guide to Android app modularization](https://developer.android.com/topic/modularization)
- [docs/preparation/technical_vision.md](../preparation/technical_vision.md)
- [.specify/specs/002-chain-list/research.md](../../.specify/specs/002-chain-list/research.md)

## Verification (2025/12/14)

Android Developers公式ドキュメントを確認し、上記技術選定が2025年12月時点でも公式推奨に沿っていることを確認。
