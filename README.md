# 期間限定めし（リミメシ）Androidアプリ

チェーン店の期間限定キャンペーン情報を閲覧するAndroidアプリ

## 前提条件

このリポジトリはlimimeshi-docsと同じ親ディレクトリに配置する必要がある
```
parent-directory/
├── limimeshi-docs/    ← 必須
└── limimeshi-android/
```

## ブランチ戦略

このリポジトリは**git-flow**を採用

詳細は [CONTRIBUTING.md](https://github.com/shg25/limimeshi-docs/blob/main/CONTRIBUTING.md) を参照

| ブランチ | 用途 | CI/CD |
|---------|------|-------|
| `main` | 本番環境 | → Google Play内部テスト |
| `develop` | 開発環境（通常はここから作業） | CI実行 |
| `feature/*` | 機能開発 | PR時にCI実行 |
| `release` | リリース準備 | → Firebase App Distribution |
| `hotfix/*` | 緊急修正 | main経由でGoogle Play |

### CI/CDワークフロー図

```
┌─────────────────────────────────────────────────────────────────┐
│                        CI (ci.yml)                              │
│   トリガー: PR to develop/main, push to develop                 │
│                                                                 │
│   ┌────────┐    ┌───────────┐    ┌─────────────┐               │
│   │  Lint  │───▶│ Unit Test │───▶│ Build Check │               │
│   └────────┘    └───────────┘    └─────────────┘               │
│   (並列実行)     (並列実行)       (コンパイル検証のみ)           │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│              CD - Firebase (cd-firebase.yml)                    │
│   トリガー: push to release                                     │
│                                                                 │
│   ┌────────────┐    ┌──────────────────────────────┐           │
│   │   Build    │───▶│ Firebase App Distribution    │           │
│   │ devRelease │    │ (テスター向け配信)            │           │
│   └────────────┘    └──────────────────────────────┘           │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│              CD - Google Play (cd-play.yml)                     │
│   トリガー: push to main                                        │
│                                                                 │
│   ┌─────────────┐    ┌──────────────────────────────┐          │
│   │    Build    │───▶│ Google Play 内部テスト       │          │
│   │ prodRelease │    │ (status: draft)              │          │
│   └─────────────┘    └──────────────────────────────┘          │
└─────────────────────────────────────────────────────────────────┘

開発フロー:
  feature/* ──PR──▶ develop ──merge──▶ release ──push──▶ Firebase App Distribution
                                           │
                                       (テスト後)
                                           │
                                           ▼
                                         main ──push──▶ Google Play 内部テスト
```

## プロジェクト概要

`limimeshi-android` は以下を目的として構築

- Androidネイティブアプリの設計力・実装力・運用力を総合的に示す
- Modern Android Development（MAD）の推奨構成に準拠したアーキテクチャを採用
- Jetpack Compose / MVVM / Clean Architecture / Coroutines / Flowを実践
- マルチモジュール構成による責務分離・拡張性の高い基盤を提示
- CI/CDやFirebase Crashlyticsなど運用面も含めた総合的なプロジェクト例を示す
- AIコーディング（Claude Code / ChatGPT）と人間の判断を組み合わせた開発フローの実験・記録

詳細：`docs/preparation/technical_vision.md`

## 技術スタック

- Kotlin
- Jetpack Compose（Material3 / Navigation）
- MVVM + Clean Architecture
- Kotlin Coroutines（structured concurrency）
- Kotlin Flow（UI state / data stream）
- Hilt（DI）
- Retrofit / OkHttp
- Room / DataStore
- Firebase Crashlytics / Analytics
- GitHub Actions（CI / JaCoCo / lint / test）
- Compose Charts（グラフ描画）

## アーキテクチャ方針

本プロジェクトはAndroid Developersの推奨構成に準拠しつつ、Clean Architecture風のレイヤー分割を行う

- **presentation**：Jetpack Compose + ViewModel + UI State
- **domain**：UseCase（アプリ固有のビジネスルール）
- **data**：Repository / DataSource（API / DB / キャッシュ）

詳細：`docs/design/architecture.md`

## モジュール構成（予定）

```
app/
core/
designsystem/
domain/
data/
model/
feature/
<feature-name>/
```

モジュール設計の検討ログ：`docs/design/module_structure.md`

## 仕様（Spec / Vertical Slice）

実際の機能実装は小さなVertical Slice単位で進める

例：

- 一覧 → 詳細 → 保存（お気に入り）
- フィルタリング
- グラフ画面（統計表示）

初期スライスの仕様：`docs/spec/first_slice.md`

## テスト方針

- UseCase、ViewModel、Repositoryを中心に単体テストを追加
- Turbineを用いてFlowのストリーム検証
- JaCoCoによるカバレッジレポート生成
- Lint / Ktlint / Detektでスタイル・静的解析
- UIテストは後半フェーズで追加予定

## CI/CD

GitHub Actionsにより以下を自動実行

- Lint
- Unit Test
- Build（Debug）
- JaCoCoレポート生成
- Firebase App Distribution（段階導入）

### GitHub Secrets

CI/CDで使用するSecrets一覧

| Secret名 | 内容 | 使用ワークフロー |
|----------|------|------------------|
| `GOOGLE_SERVICES_JSON_DEV` | Firebase設定（dev環境、Base64） | ci.yml, cd-firebase.yml |
| `GOOGLE_SERVICES_JSON_PROD` | Firebase設定（prod環境、Base64） | ci.yml, cd-play.yml |
| `KEYSTORE_BASE64` | 署名鍵（Base64） | cd-firebase.yml, cd-play.yml |
| `KEYSTORE_PASSWORD` | キーストアパスワード | cd-firebase.yml, cd-play.yml |
| `KEY_ALIAS` | 鍵エイリアス | cd-firebase.yml, cd-play.yml |
| `KEY_PASSWORD` | 鍵パスワード | cd-firebase.yml, cd-play.yml |
| `FIREBASE_APP_ID_DEV` | Firebase App ID（dev） | cd-firebase.yml |
| `FIREBASE_SERVICE_ACCOUNT` | Firebaseサービスアカウント | cd-firebase.yml |
| `PLAY_SERVICE_ACCOUNT` | Google Playサービスアカウント | cd-play.yml |

## AIコーディング利用について

本プロジェクトでは以下のようにAIを活用

- Claude Codeによるスキャフォールド生成・設計補助
- ChatGPTによる補助的なコード提案・リファクタ指示
- AIが生成するコードの責務は厳密にレビューし、人間が最終判断

AI開発方針の詳細：`docs/preparation/technical_vision.md`

## 今後の予定

- Compose Navigationの複雑化（multi-backstack対応）
- Remote Configによるfeature flag
- グラフ画面の拡張
- UIテスト追加
- WorkManagerによる定期同期処理
- Firebase App Distributionの本導入
