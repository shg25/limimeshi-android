# limimeshi-android Technical Vision

Androidネイティブアプリ開発の専門性・設計力・実装力・運用力を総合的に示すためのサンプルアプリ

本プロジェクトは、要求水準の高いAndroidエンジニア像を念頭に、現場で求められる実践的な技術・設計・開発フローを包括的に提示することを目的とする

---

## 1. プロジェクトの目的

- Androidネイティブ開発における設計・実装・テスト・CI/CDまでを一貫した形で提示する
- 証跡として提出できる高品質なサンプルアプリを作成し、技術力の可視化に活用する
- limimeshi-docs / limimeshi-infra / limimeshi-adminと共通思想で構築することで、複数リポジトリ横断の設計力も示す
- AIコーディング（Claude Code / ChatGPT）と人間の判断を統合した、現代的な開発スタイルを示す

---

## 2. 想定する要求レベル

サンプルアプリは、以下のような「専門性の高いAndroidエンジニア」に求められる典型的能力を網羅するように設計する

- Jetpack Composeを中心とした宣言的UI設計・最適化・状態管理
- MVVM / MVIなどのモダンアーキテクチャパターンの理解
- Clean Architectureに基づくレイヤー分離・依存方向の統制
- Kotlin Coroutines / Flowを用いた構造化並行処理
- テスト自動化（特にUnit Test）
- マルチモジュール構成の設計・責務分割
- Androidフレームワークの深い理解（ライフサイクル・状態保持・WorkManager等）
- グラフ描画などUI応用実装の経験
- CI/CD（GitHub Actions）およびFirebase App Distributionの活用

※特定企業や年収レンジは目的に含めず、一般的な高度エンジニア像を対象とする。

---

## 3. 技術スタック（最終確定版）

### 言語
- Kotlin

### UI / Presentation

- Jetpack Compose（Material3）
- Navigation Compose
- UI state：ViewModel + UiState（data class）
- Single Source of Truth & Unidirectional Data Flow
- **UI設計・アニメーション・状態管理・画面遷移・UI最適化など、必要な調整が行えること**

### アーキテクチャ

- **MVVM（中心）**
- 適宜MVP / MVC / MVIとの比較説明が可能なレベルで理解
- **Clean Architecture（3レイヤー）**
  - presentation
  - domain（UseCase）
  - data（Repository / DataSource）
- 依存方向はdomainを中心に内向き
- interface / sealed class / result型による明示的な境界定義

### 非同期処理

- Kotlin Coroutines（structured concurrency）
- Kotlin Flow（state / event / data stream）

### データレイヤー

- Retrofit + OkHttp
- JSON（Moshi or Kotlin Serialization）
- Room（永続化）
- DataStore（設定値）

### モジュール構成（マルチモジュール）

- `app`
- `core:designsystem`
- `core:model`
- `core:domain`
- `core:data`
- `feature:<name>`
- （必要に応じて`core:common`）

### Firebase / 運用

- Firebase Crashlytics
- Firebase Analytics（最小限）
- Remote Config（将来的なfeature flag用途）

### 品質・テスト

- Unit Test（UseCase / ViewModel / Repository）
- Turbine（Flow検証）
- MockK
- JUnit5
- **JaCoCo（カバレッジ取得）**
- Lint / Detekt / Ktlint

### CI/CD

- GitHub Actions
  - lint / test / build
  - JaCoCoレポート出力
- Firebase App Distribution（初期段階で導入）
- 将来的にGoogle Play Publisherによる自動アップロードも想定

---

## 4. Androidフレームワークへの対応方針

- Activity / ViewModel / Composeのライフサイクル理解
- `SavedStateHandle`と`rememberSaveable`を活用した状態保持
- process death対応
- WorkManagerによるバックグラウンド処理
- Navigationのバックスタック設計
- オフラインキャッシュ戦略（Room + Repository）
- エラーハンドリングと再試行ポリシー

---

## 5. UI応用（グラフ描画）

- Compose Chartsを利用して折れ線グラフ/円グラフなどを1画面実装
- ダミーデータでも可だが、ユースケースとの整合を持たせる
- UI stateの変化に応じてグラフが再描画される構成にする

目的：UI実装力 × Compose応用 × 描画ロジック × 状態管理の証明

---

## 6. 開発スタイル

### 仕様と実装

- Spec Kitによる仕様駆動（コンパクトな仕様から実装へ）
- Vertical Slice（UI → VM → UseCase → Repository）で機能単位に完成させる

### テストの考え方

- TDD全面採用は目的とスケジュール上必須ではない
- リスクの高い箇所（ドメイン・Repository）はテストファースト
- UIテストは後半フェーズで対応

---

## 7. AIコーディング方針（Claude Code / ChatGPT）

### 使用目的

- 設計のドラフト生成
- モジュール構成の自動生成
- ViewModel / Repositoryのスキャフォールド生成
- テストコード生成補助
- リファクタ案の提示
- スタイルガイドに従ったコード整形

### 人が判断すること

- アーキテクチャの最終決定
- 公開API・インターフェース
- データ構造および責務分割
- エラーハンドリング方針
- セキュリティ関連

---

## 8. このサンプルで示したい能力

- Android Developers推奨アーキテクチャの実践
- Compose × MVVM × FlowによるUI / 状態管理の理解
- Clean Architectureによる依存性制御
- マルチモジュール構成の設計力
- コルーチン / Flowの高度な活用（state / event / cancellation）
- テスト自動化とCI/CD設計
- ログ・クラッシュレポート基盤整備
- グラフ描画等を含むUI応用力
- Android OS / ライフサイクルの深い理解
- AIと協調した開発ワークフローの確立

---

## 9. 将来的な拡張例

- Compose Desktop対応（実験）
- Wear OS対応
- 本番API実装
- Remote Configによるfeature flag
- Navigationの複雑化（multi-backstack）
