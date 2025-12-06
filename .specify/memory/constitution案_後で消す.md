# 期間限定めし（リミメシ）Constitution

## 開発原則（Core Principles）

開発原則は3つのレイヤーで構成
- 基礎原則（Fundamental）
- プロジェクト固有の制約（Project Constraints）
- 品質基準（Quality Standards）

### 基礎原則（Fundamental）

#### I. Spec-Driven（仕様駆動開発）

**出典**：[GitHub Spec Kit公式](https://github.com/github/spec-kit)

仕様駆動開発を実践
- GitHub Spec Kit公式ワークフロー（`/speckit-specify` → `/speckit-clarify` → `/speckit-plan` → `/speckit-tasks`）に従う
- 仕様が王様、コードは従者
- 全ての機能は仕様書（spec.md、plan.md、tasks.md）から開始

#### II. Test-First（テスト駆動）※非交渉原則

**出典**：GitHub Spec Kit公式例

テスト駆動開発（TDD）は絶対原則
- テストを先に書く → ユーザー承認 → テストが失敗することを確認 → 実装
- Red-Green-Refactorサイクルを厳守
- 全ての機能追加・変更はテストから開始

#### III. Simplicity（シンプルさ優先）

**出典**：GitHub Spec Kit公式例

シンプルな設計を最優先
- YAGNI原則：必要になるまで実装しない
- 過度な抽象化を避ける（Anti-Abstraction）
- フレームワークの機能を直接使用し、不要なラッパーを作らない
- 複雑性の追加には正当な理由が必要

### プロジェクト固有の制約（Project Constraints）

#### IV. Firebase-First

**出典**：planning/first-idea.mdの技術スタック

Firebase優先のアーキテクチャ（個人開発での運用効率とコスト管理のため）
- 認証：Firebase Authentication
- データベース：Firestore
- ホスティング：Firebase Hosting
- サーバーレス関数：Cloud Functions
- 他の技術を採用する場合は、Firebaseとの統合を考慮し、ADRで記録

#### V. Legal Risk Zero（法的リスクゼロ）

**出典**：planning/first-idea.mdのスクレイピング禁止方針

法的リスクをゼロにすることを最優先
- **Phase2**：完全手動運用でスタート
  - データ登録・更新は完全手動
  - スクレイピング・自動クローリングは一切行わない
  - チェーン店の利用規約を完全遵守
- **Phase3以降**：公式提携・API連携を検討
  - チェーン店との公式提携後のみ自動化を許可
  - 相手側の明示的な許可を得てから実施

### 品質基準（Quality Standards）

#### VI. Mobile & Performance First

**出典**：planning/first-idea.mdのターゲットユーザーと品質基準

モバイル体験とパフォーマンスを最優先
- **モバイル対応**
  - レスポンシブデザイン必須
  - タッチ操作に最適化されたUI
  - iOS/Android直近3世代のサポート
- **パフォーマンス**
  - ページ読み込み時間：3秒以内（モバイル4G環境）
  - API応答時間：1秒以内
  - Firestoreクエリ：インデックスの適切な設計

#### VII. Cost Awareness（コスト意識）

**出典**：planning/first-idea.mdの予算管理（月0〜5,000円）

コスト爆発を防ぐ設計
- Firebase予算上限の設定（月0〜5,000円）
- Cloud Functionsの実行回数・メモリ使用量の監視
- Firestoreの読み書き回数の最適化
- 悪意あるユーザー対策（レート制限、不正検知）
- 負荷テストを必ず実施（Phase2完了前）

#### VIII. Security & Privacy First

**出典**：planning/first-idea.mdの非機能要件

セキュリティとプライバシーを最優先
- **セキュリティ**
  - Firestoreセキュリティルールの厳格な設定
  - 認証必須機能の明確化
  - 定期的なセキュリティレビュー
- **プライバシー**
  - 個人情報の最小化（メールアドレスのみ収集）
  - GDPR・個人情報保護法の遵守
  - ユーザーデータの透明性確保

#### IX. Observability（可観測性）

**出典**：planning/first-idea.mdのモニタリング・分析

システムの状態を常に把握可能にする
- **エラー監視**
  - Firebase Console + Sentry検討
  - エラーは24時間以内に検知
- **コスト監視**
  - 予算アラート設定
  - 異常なコスト増加を自動検知
- **KPI監視**
  - MAU、DAU、レビュー投稿数、広告収益
  - Google Analytics4（GA4）で計測

## 技術選定方針

### フロントエンド

- React：ユーザー向けWebアプリ
- React Admin：管理画面
- TypeScript：型安全性の確保

**注記**：ビルドツール（Vite、Webpack等）の選定はPhase1-3のADR作成時に決定する

### バックエンド

- Firestore：NoSQLデータベース
- Cloud Functions：サーバーレス関数
- Firebase Authentication：認証

### デプロイ・CI/CD

- Firebase Hosting：Webアプリホスティング
- GitHub Actions：CI/CDパイプライン
- 自動テスト・デプロイの完全自動化

## ドキュメント管理

### 記述ルール

- WRITING_STYLE_GUIDE.mdに従う
- 「。」は注意書き・免責事項のみ
- 箇条書き・説明文は「〜する」で統一
- 太字は必要最小限（サービス名、技術用語の初出、重要なルール）
- 項目名は見出しを使う（「**項目名**：」ではなく「#### 項目名」）

### ADR（Architecture Decision Records）

- 技術選定・設計決定は必ずADRに記録
- Context（背景）→ Decision（決定）→ Consequences（結果）の形式
- adr/ディレクトリに番号付きで管理

## ガバナンス

### 憲法の優先順位

- この憲法は全ての開発プラクティスに優先する
- 憲法の修正には、ドキュメント化・承認・移行計画が必要

### レビュー・承認

- 全てのPR・レビューは憲法への準拠を確認
- 複雑性の追加には正当な理由が必要
- 開発時の実行ガイダンスは CLAUDE.md を参照

### 憲法の更新

- 軽微な修正：typo修正、文言の明確化
- 重大な修正：原則の追加・削除、技術選定の変更（ADR必須）

---

**Version**: 2.0.0 | **Ratified**: 2025/10/27 | **Last Amended**: 2025/10/27
