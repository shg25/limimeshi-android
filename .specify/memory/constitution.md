# 期間限定めし（リミメシ）Android Constitution

Androidネイティブアプリ開発における原則・制約・品質基準を定める

limimeshi全体の憲法を継承しつつ、本リポジトリではAndroid開発に特化した方針を追加定義する

## 開発原則（Core Principles）

開発原則は3つのレイヤーで構成
- 基礎原則（Fundamental）
- Android固有の制約（Android Constraints）
- 品質基準（Quality Standards）

### 基礎原則（Fundamental）

#### I. Spec-Driven + Vertical Slice（仕様駆動 × 縦割り）

**出典**：[GitHub Spec Kit公式](https://github.com/github/spec-kit) / docs/spec配下

仕様駆動開発を実践
- Spec Kitのワークフロー（`/speckit-specify` → `/speckit-clarify` → `/speckit-plan` → `/speckit-tasks`）を基本とする
- 全ての機能はspec（`docs/spec/`）から開始
- 実装はVertical Slice（UI → ViewModel → UseCase → Repository → DataSource）単位で進める
- 「仕様で何を証明したいか」を先に決めてからコードを書く

#### II. Test-First（リスクベース）

**出典**：GitHub Spec Kit公式例をlimimeshi-android向けに調整

テスト駆動は重要だが、Androidアプリ特性を踏まえた現実的な運用とする
- すべての変更に対して厳密なTDDを要求しない
- ドメインロジックや不具合が致命的な箇所はテストファーストを優先
- UIやglue codeのテストは後追いでもよいが、主要フローのスモークテストは必ず用意
- Gradle / CI上でテストが常に通る状態を維持

#### III. Modern Android Alignment（公式推奨への準拠）

**出典**：Android Developersアーキテクチャガイド

Android Developersが推奨するモダンアーキテクチャに沿う
- Jetpack Composeを用いた宣言的UI
- MVVM + 一方向データフロー（UDF）
- Clean Architecture風のレイヤー分離（presentation / domain / data）
- Repositoryパターン / Coroutines / Flowを標準とする
- 無理に独自フレームワークは作らず、公式のコンポーネントを優先

#### IV. Simplicity（シンプルさ優先）

**出典**：GitHub Spec Kit公式例

シンプルな設計を最優先
- YAGNI原則：必要になるまで実装しない
- 過度な抽象化を避ける（Anti-Abstraction）
- 「将来のかもしれない要件」より「今の仕様」を優先
- 複雑性を追加する場合は、その理由をADRかコメントで明示

#### V. AI-Assisted, Human-Owned（AI支援 × 人間責任）

**出典**：docs/preparation/technical_vision.md

Claude Code / ChatGPTを積極的に活用するが、責任は常に人間が持つ
- 仕様・アーキテクチャ・公開インターフェースは人間が決定
- AIが生成したコードは必ずレビュー
- 秘密情報（鍵・トークン）はプロンプトに含めない
- AI利用方針の詳細は`docs/preparation/technical_vision.md`に記載

### Android固有の制約（Android Constraints）

#### VI. Firebase Integration（モバイル × Firebase連携）

**出典**：limimeshi-infra / limimeshi-docsの技術スタック

モバイルアプリはFirebaseを前提とした構成とする
- Firestore：データ読み取り（チェーン店・キャンペーン情報）
- Crashlytics：クラッシュ検知・分析
- Analytics：最小限のイベント計測
- Remote Config：将来的なfeature flag用途として余地を残す
- Firebase設定・プロジェクト構成はlimimeshi-infraと整合させる

#### VII. Legal Risk Zero（法的リスクゼロ）

**出典**：全体憲法の「Legal Risk Zero」

法的リスクをゼロにすることを最優先
- データ取得は手動または公式APIを前提とし、スクレイピング等は行わない
- 各プラットフォーム（Google Play等）の利用規約に従う
- 外部サービスの権利関係に関わる機能追加は必ずADRで検討

#### VIII. Cost Awareness（コスト意識）

**出典**：全体憲法の「Cost Awareness」

コスト爆発を防ぐ設計
- Firebase利用料・CI実行時間を意識
- 不要なビルド・テストジョブを増やさない
- 長時間・高負荷な処理はローカル開発とCIを分けて設計

### 品質基準（Quality Standards）

#### IX. Mobile & Performance First（モバイル最優先）

**出典**：全体憲法の品質基準をAndroid向けに調整

モバイル体験とパフォーマンスを最優先
- 直近のAndroidメジャーバージョン3世代を優先サポート対象とする
- 不要な再コンポーズを避け、Composeのパフォーマンスを意識した設計を行う
- ネットワーク不安定な環境でも最低限使えるよう、オフラインキャッシュを検討
- アプリ起動時間・画面遷移の体感速度を常に意識

#### X. Security & Privacy First

**出典**：全体憲法の非機能要件

セキュリティとプライバシーを最優先
- **セキュリティ**
  - Firestoreセキュリティルールの厳格な設定
  - 認証必須機能の明確化
  - 定期的なセキュリティレビュー
- **プライバシー**
  - 個人情報の最小化（メールアドレスのみ収集）
  - GDPR・個人情報保護法の遵守
  - ユーザーデータの透明性確保

#### XI. Observability（可観測性）

**出典**：全体憲法の「Observability」

システムの状態を常に把握可能にする
- Crashlyticsによるクラッシュ検知を必須とする
- ログは共通Logger（Timber等）経由で出力
- 重要なイベントはFirebase Analytics等で計測し、挙動を可視化

#### XII. CI / Test Coverage

GitHub Actionsでlint / test / buildを自動実行
- JaCoCoによりカバレッジを計測し、主要ドメインロジックのカバー率を確保
- テストが赤の状態でmain/developにマージしない

## 技術選定方針

### モバイルアプリ

- 言語：Kotlin
- UI：Jetpack Compose（Material3 / Navigation）
- アーキテクチャ：MVVM + Clean Architecture風レイヤー
- 非同期：Kotlin Coroutines / Flow
- DI：Hilt
- データ
  - Remote：Retrofit + OkHttp
  - Local：Room / DataStore
- グラフ描画：Compose Charts（予定）

### デプロイ・CI/CD

- CI：GitHub Actions
- カバレッジ：JaCoCo
- 配布：Firebase App Distribution（段階的導入）
- 本番配信：Google Play Console（自動デプロイは将来的に検討）

詳細は`docs/design/architecture.md`および`docs/design/module_structure.md`を参照

## ドキュメント管理

### 記述ルール

- docs-style-guide.mdに従う（※共通ルール）
- 「。」は注意書き・免責事項のみ
- 箇条書き・説明文は「〜する」で統一
- 太字は必要最小限（サービス名、技術用語の初出、重要なルール）
- 項目名は見出しを使う（「**項目名**：」ではなく「#### 項目名」）

### Spec / ADR

- 機能仕様は`docs/spec/`にSpec Kitの形式で記録
- アーキテクチャ・技術選定の重要な決定はADRで管理
  - Context（背景）
  - Decision（決定）
  - Consequences（結果）

## ガバナンス

### 憲法の優先順位

- 本ファイル（Android憲法）はlimimeshi全体憲法のAndroid版拡張とする
- 全体憲法とAndroid憲法が矛盾する場合、本リポジトリではAndroid憲法を優先
- 重大な原則変更にはドキュメント化・レビュー・移行計画が必要

### レビュー・承認

- 全てのPRは、この憲法および`docs/preparation/technical_vision.md`への準拠を確認
- アーキテクチャやモジュール構成に関わる変更は、可能な限りADRを伴う
- 開発時のAI利用ガイドはCLAUDE.mdを参照

### 憲法の更新

- 軽微な修正：typo修正、文言の明確化
- 重大な修正：原則の追加・削除、技術選定の変更（ADR必須）

---

**Version**: 1.0.0 (Android Edition) | **Ratified**: 2025/12/07 | **Last Amended**: 2025/12/07
