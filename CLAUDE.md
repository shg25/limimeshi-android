# AI Agent Context: 期間限定めし（リミメシ）Androidアプリ

このファイルはClaude CodeなどのAIエージェントがプロジェクトを効率的に理解するための情報です。

## 前提条件

このリポジトリは limimeshi-docs と同じ親ディレクトリに配置する必要がある：
```
parent-directory/
├── limimeshi-docs/    ← 必須
└── limimeshi-android/
```

## ブランチ戦略

このリポジトリは**git-flow**を採用

### ブランチ構成
| ブランチ | 用途 |
|---------|------|
| `main` | 本番環境 |
| `develop` | 開発環境 |
| `feature/*` | 機能開発 |
| `release/*` | リリース準備 |
| `hotfix/*` | 緊急修正 |

### 基本フロー
1. `develop`から`feature/xxx`ブランチを作成
2. 作業完了後、`develop`へPR
3. リリース時は`release/x.x`→`main`にマージ

詳細は [CONTRIBUTING.md](https://github.com/shg25/limimeshi-docs/blob/main/CONTRIBUTING.md) を参照

## ディレクトリ構成（ガバナンス関連）

本リポジトリは limimeshi-docs のガバナンスルールに従う

### docs/
- `roadmap.md`：ロードマップ（実体、リポジトリ固有）
- `CHANGELOG.md`：変更履歴（実体、リポジトリ固有）
- `README.md`：ディレクトリ説明（コピー、編集不可）

### docs/adr/
- リポジトリ固有のADR（実体）
- `README.md`：ADR説明（コピー、編集不可）

### docs/governance/
- `docs-style-guide.md`：ドキュメント記述ルール（シンボリックリンク）
- `shared-rules.md`：複数リポジトリ共通ルール（シンボリックリンク）
- `README.md`：ガバナンス説明（コピー、編集不可）

### .claude/
Claude Code設定（シンボリックリンク）：
- `settings.json`：Hooks設定
- `commands/suggest-claude-md.md`
- `skills/`：Agent Skills

### .specify/
GitHub Spec Kit（仕様駆動開発）：
- `memory/constitution.md`：プロジェクトの憲法（実体、カスタマイズ可）
- `specs/`：機能仕様書（実体、リポジトリ固有）
- `.claude/commands/`：speckit-*コマンド（シンボリックリンク）
- `templates/`：仕様書テンプレート（シンボリックリンク）
- `README.md`：Spec Kit説明（コピー、編集不可）

### 同期について
- **シンボリックリンク**: limimeshi-docs更新で自動反映
- **READMEコピー**: limimeshi-docsから`/sync-shared-rules [リポジトリ名]`で同期
- **リポジトリ固有**: constitution.md、roadmap.md、CHANGELOG.md、specs/、adr/

---

## プロジェクト概要

**正式名称**: 期間限定めし（リミメシ）Androidアプリ
**リポジトリ**: limimeshi-android
**役割**: 一般ユーザー向けのチェーン店・キャンペーン情報の閲覧（読み取り専用）

## 技術スタック

- **言語**: Kotlin 1.9+
- **UIフレームワーク**: Jetpack Compose + Material 3
- **Minimum SDK**: API 31 (Android 12)
- **Target SDK**: API 34 (Android 14)
- **バックエンド**: Firebase (Firestore, Authentication)
- **DI**: Hilt
- **状態管理**: StateFlow + Compose State
- **データ永続化**: DataStore Preferences
- **テスト**: JUnit 5, MockK, Turbine, Compose Testing

## ディレクトリ構造

```
limimeshi-android/
├── .specify/specs/              # GitHub Spec Kit仕様書
│   ├── 002-chain-list/          # チェーン店一覧機能
│   │   ├── spec.md              # 機能仕様
│   │   ├── plan.md              # 実装計画
│   │   ├── tasks.md             # タスクリスト
│   │   ├── research.md          # 技術調査
│   │   ├── quickstart.md        # クイックスタート
│   │   ├── contracts/           # 技術契約
│   │   └── checklists/          # チェックリスト
│   └── 003-favorites/           # お気に入り登録機能
│       └── ...
├── app/
│   ├── src/main/java/com/shg25/limimeshi/
│   │   ├── ui/                  # Composable関数、画面
│   │   │   ├── chain/           # チェーン店一覧画面
│   │   │   ├── components/      # 共通コンポーネント
│   │   │   └── theme/           # Material 3テーマ
│   │   ├── data/                # データ層
│   │   │   ├── repository/      # リポジトリ
│   │   │   └── model/           # データモデル
│   │   ├── util/                # ユーティリティ
│   │   └── di/                  # Hilt DI設定
│   ├── src/test/                # 単体テスト
│   └── src/androidTest/         # UIテスト
├── build.gradle.kts
├── app/build.gradle.kts
└── google-services.json         # Firebase設定（.gitignore）
```

## 重要なドキュメント

1. **[.specify/specs/002-chain-list/spec.md](.specify/specs/002-chain-list/spec.md)**: チェーン店一覧機能仕様
2. **[.specify/specs/003-favorites/spec.md](.specify/specs/003-favorites/spec.md)**: お気に入り登録機能仕様
3. **[.specify/specs/002-chain-list/tasks.md](.specify/specs/002-chain-list/tasks.md)**: チェーン店一覧タスクリスト
4. **[.specify/specs/003-favorites/tasks.md](.specify/specs/003-favorites/tasks.md)**: お気に入り登録タスクリスト

## 開発コマンド

```bash
# ビルド
./gradlew build

# テスト実行
./gradlew test

# UIテスト実行
./gradlew connectedAndroidTest

# APK生成
./gradlew assembleDebug

# Lint実行
./gradlew lint
```

## GitHub Spec Kit 運用ルール

このプロジェクトは **Spec-Driven Development** を採用

### 基本原則

- **仕様が王様、コードは従者**: spec.mdが唯一の信頼できる情報源
- **spec.mdは生きたドキュメント**: 常に現在の実装を反映

### 仕様変更時のワークフロー

```
1. spec.md を修正（要件を更新）
2. 実装を仕様に合わせて修正
3. コミット時に両方を含める
```

### 禁止事項

- spec.mdを更新せずに仕様変更を実装することは禁止
- spec.mdと実装の乖離を放置することは禁止

### 仕様ファイルの場所

- **002-chain-list**: `.specify/specs/002-chain-list/`
- **003-favorites**: `.specify/specs/003-favorites/`

## データの流れ

```
limimeshi-admin（管理画面）
    ↓ 登録・編集
  Firestore
    ↓ 読み取り
limimeshi-android（このアプリ）
```

- **このアプリは読み取り専用**: データの登録・編集は管理画面が担当
- **お気に入り機能のみ書き込み可能**: ユーザーごとのお気に入りデータはこのアプリで登録

## 機能概要

### 002-chain-list（チェーン店一覧）
- チェーン店をソート順（新着順/ふりがな順）で一覧表示
- 各チェーン店に紐づくキャンペーンを表示
- お気に入りフィルタ（003機能と連携）
- X Post埋め込み表示

### 003-favorites（お気に入り登録）
- チェーン店をお気に入り登録・解除
- お気に入り登録数の表示
- 複数デバイス間での同期（Firestore）

## AI向けの注意事項

### 実装時

1. **spec.mdを必ず参照**してから実装
2. **tasks.mdの順序に従って**実装
3. **TDD**（テストを先に書く）
4. **パッケージ名**: `com.shg25.limimeshi`

### 技術選定

- **MVVM + Clean Architecture（簡略版）**: Google推奨パターン
- **StateFlow + Compose State**: 状態管理
- **Firestore SDK for Android**: データ読み取り
- **WebView**: X Post埋め込み
- **DataStore Preferences**: フィルタ・ソート設定の永続化

### Constitution（憲法）遵守

全ての実装は [limimeshi-docs/governance/constitution.md](https://github.com/shg25/limimeshi-docs/blob/main/governance/constitution.md) に準拠
