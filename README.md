# 期間限定めし（リミメシ）Androidアプリ

チェーン店の期間限定キャンペーン情報を閲覧するAndroidアプリ

## 前提条件

このリポジトリは limimeshi-docs と同じ親ディレクトリに配置する必要がある：
```
parent-directory/
├── limimeshi-docs/    ← 必須
└── limimeshi-android/
```

## 技術スタック

- Kotlin 1.9+
- Jetpack Compose + Material 3
- Firebase (Firestore, Authentication)
- Hilt（DI）

## セットアップ

### 前提条件

- Android Studio Ladybug以上
- JDK 17以上
- Firebase プロジェクト（limimeshi-dev）へのアクセス権

### 1. Firebase設定

```bash
# google-services.jsonをapp/に配置
# Firebase Consoleからダウンロード
```

### 2. ビルド

```bash
./gradlew build
```

### 3. 実行

Android Studioから実行、またはエミュレータで起動

## 利用可能なスクリプト

| コマンド | 説明 |
|---------|------|
| `./gradlew build` | ビルド |
| `./gradlew test` | ユニットテスト実行 |
| `./gradlew connectedAndroidTest` | UIテスト実行 |
| `./gradlew assembleDebug` | デバッグAPK生成 |
| `./gradlew lint` | Lint実行 |

## 機能

### チェーン店一覧（002-chain-list）

- チェーン店をソート順（新着順/ふりがな順）で一覧表示
- 各チェーン店に紐づくキャンペーンを表示
- お気に入りフィルタ

### お気に入り登録（003-favorites）

- チェーン店をお気に入り登録・解除
- 複数デバイス間での同期（Firestore）

## 関連ドキュメント

- [チェーン店一覧機能仕様](.specify/specs/002-chain-list/spec.md)
- [お気に入り登録機能仕様](.specify/specs/003-favorites/spec.md)

## 関連リポジトリ

- [limimeshi-docs](https://github.com/shg25/limimeshi-docs): 企画・設計ドキュメント
- [limimeshi-admin](https://github.com/shg25/limimeshi-admin): 管理画面
