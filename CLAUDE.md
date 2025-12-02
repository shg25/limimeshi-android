# AI Agent Context: 期間限定めし（リミメシ）Androidアプリ

このファイルはClaude CodeなどのAIエージェントがプロジェクトを効率的に理解するための情報です。

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

## 関連リポジトリ

- **limimeshi-docs**: 企画・設計ドキュメント（ガバナンス）
- **limimeshi-admin**: 管理画面（React Admin）

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

全ての実装は [limimeshi-docs/memory/constitution.md](https://github.com/h-shigetsugu/limimeshi-docs/blob/main/memory/constitution.md) に準拠

---

**最終更新**: 2025-12-03
