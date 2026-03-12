# limimeshi-android ロードマップ

**元ドキュメント**: [limimeshi-docs/roadmap.md](https://github.com/shg25/limimeshi-docs/blob/main/roadmap.md)

このドキュメントはlimimeshi-docsのロードマップからAndroid関連のタスクを抽出したもの。
プロジェクト全体のロードマップは上記リンクを参照。

---

## 現在のステータス

| フェーズ | ステータス |
|---------|-----------|
| Phase1：設計・仕様策定 | ✅ 完了 |
| Phase0：CI/CD基盤構築 | ✅ 完了（Phase0-4, 0-5はMVP後） |
| Phase2：MVP実装 | 🚧 進行中 |
| Phase2.5：追加技術デモ | ⏳ 待機中 |

---

## Phase0：CI/CD基盤構築

### 目的

- 本格的な機能開発の前に、CI/CD・品質基盤・Firebase連携を整備
- ポートフォリオとしてDevOps理解を示す

### タスク一覧

#### Phase0-1：Build Flavors・Firebase基盤
- [x] Build Flavors設定（dev/prod）
- [x] google-services.json配置（dev/prod）
- [x] Firebase SDK導入（Crashlytics、Analytics）
- [x] Crashlytics初期化・Timber連携
- [x] Analytics初期化・イベントヘルパー

#### Phase0-2：CI/CD構築 ✅
- [x] GitHub Actions設定（lint/test/build）
- [x] Firebase App Distribution配信（devRelease）
- [x] Google Play内部テスト配信（prodRelease）※status: draft

#### Phase0-3：品質基盤 ✅
- [x] Lint設定カスタマイズ（lint.xml）
- [x] Detekt設定（detekt.yml、CI統合）
- [x] Hilt基本設定（@HiltAndroidApp、AppModule）
- [x] JUnit5/MockK/Turbine導入
- [x] JaCoCoカバレッジ計測（CI統合）

#### Phase0-4：Google Play公開準備（MVP実装完了後）
- [ ] Google Play Consoleアプリセットアップ完了
  - [ ] プライバシーポリシー設定
  - [ ] ストア掲載情報（アイコン、スクリーンショット、説明文）
  - [ ] コンテンツのレーティング
  - [ ] その他Google審査要件
- [ ] cd-play.ymlの`status: draft`を`completed`に変更（自動公開化）

#### Phase0-5：Firebaseセキュリティ対策（MVP実装完了後）
- [ ] Firebase APIキーにAndroidアプリ制限を設定（Google Cloud Console）
- [ ] Firebase App Checkを有効化
- [ ] Firebase Security Rulesを適切に設定

### Build Variants

| Variant | パッケージ名 | アプリ名 | 用途 |
|---------|-------------|---------|------|
| devDebug | com.shg25.limimeshi.dev | リミDEV | 開発中のローカルテスト |
| devRelease | com.shg25.limimeshi.dev | リミDEV | Firebase App Distribution |
| prodDebug | com.shg25.limimeshi | リミメシ | 本番環境デバッグ |
| prodRelease | com.shg25.limimeshi | リミメシ | Google Play配信 |

---

## Phase2：MVP実装

### リポジトリ作成と仕様書移行

- [x] リポジトリ作成（GitHub、public）
- [x] `.specify/`ディレクトリ構成作成
- [x] `specs/002-chain-list/`を移行（limimeshi-docsから）
- [x] `specs/003-favorites/`を移行（limimeshi-docsから）
- [x] `memory/constitution.md`をコピー配置
- [x] templates/、.claude/commands/をコピー
- [x] **Android技術選定の再確認**（2025/12/14完了、ADR-001参照）

---

## Android技術選定（実装着手前に再確認）

### 背景

Phase1（2025/11）時点で`specs/002-chain-list/research.md`に技術選定を記録済み。ただし、Android実装はlimimeshi-admin完了後となるため、着手時点で再確認が必要。

### 再確認が必要な理由

1. **Androidエコシステムの変化**: Jetpackライブラリは頻繁に更新される
2. **就活での市場調査**: 求人で求められているライブラリを把握してから選定
3. **公式推奨の最新化**: Android Developersサイトの推奨が変わる可能性

### 現時点の選定（2025/11、Phase1時点）

| カテゴリ | 選定技術 | 選定根拠 |
|---------|---------|---------|
| 言語 | Kotlin | Android公式推奨 |
| UIフレームワーク | Jetpack Compose + Material 3 | Android公式推奨 |
| データ読み取り | Firebase Android SDK | Firebase公式ドキュメント |
| 認証 | Firebase Authentication | Firebase公式ドキュメント |
| 設定永続化 | DataStore Preferences | Android公式推奨（SharedPreferencesの後継） |
| アーキテクチャ | MVVM + Clean Architecture | Android Architecture Guide |
| 状態管理 | StateFlow + Compose State | Kotlin公式 + Android公式 |
| DI | Hilt | Android公式推奨 |
| テスト | JUnit 5 + MockK + Turbine | コミュニティ推奨（公式はJUnit 4だが問題なし） |
| UIテスト | Compose Testing | Android公式 |

### 着手時の確認タスク ✅

- [x] [Android Developers - Testing](https://developer.android.com/training/testing)で推奨ライブラリを確認（2025/12/14）
- [x] [Android Developers - App architecture](https://developer.android.com/topic/architecture)で最新パターンを確認（2025/12/14）
- [x] 就活求人で求められているライブラリを調査・反映（ADR-001に反映済み）
- [x] `specs/002-chain-list/research.md`を最新化（2025/12/14）
- [x] `specs/003-favorites/research.md`を最新化（2025/12/14）
- [x] 技術選定の決定をADRとして記録（`docs/adr/001-adopt-portfolio-driven-tech-stack.md`）

### 参考リンク

- [Android Developers](https://developer.android.com/)
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3 for Android](https://m3.material.io/)

---

## MVP開発（002-chain-list、003-favorites）

### 進捗状況

| 機能 | ステータス | 備考 |
|------|-----------|------|
| 002-chain-list | ✅ 完了 | チェーン店一覧、ソート、Pull-to-refresh |
| 003-favorites US1 | ✅ 完了 | お気に入り登録・解除、Googleログイン |
| 003-favorites US2 | ✅ 完了 | お気に入り登録数の表示、Optimistic UI |
| 002 お気に入りフィルタ | ⏳ 待機中 | 003-favoritesと連携 |

### 対象機能

1. **002-chain-list（チェーン店一覧）** ✅
   - チェーン一覧表示
   - キャンペーン一覧表示（チェーン別）
   - X Post埋め込み表示
   - ソート順選択（新着順/ふりがな順）
   - お気に入りフィルタ（003と連携）→ 未実装
   - **Pull-to-refresh**（ローカルキャッシュのクリア・再同期）

2. **003-favorites（お気に入り登録）** ✅
   - チェーン店お気に入り登録・解除 ✅ US1完了
   - お気に入り登録数の表示 ✅ US2完了（FavoriteCountコンポーネント、Optimistic UI）
   - Firestoreへの永続化 ✅
   - **Googleログイン機能** ✅ 追加実装（Credential Manager API）

### 技術スタック

- Kotlin + Jetpack Compose + Firebase
- 詳細は`.specify/specs/`配下の各spec.md、research.md参照

---

## Phase2.5：追加技術デモ（MVP直後）

### 目的

ポートフォリオとしての技術幅を証明するため、MVP完了後に追加の技術要素を導入する

### 対象機能

#### 004-chain-detail（チェーン店詳細画面）

002-chain-listの拡張機能として、チェーン店詳細画面を追加

- **目的**: Navigation引数、SavedStateHandle、process death対応のデモ
- **技術要素**:
  - Navigation Compose（Type-safe arguments）
  - SavedStateHandle（画面回転・復元時の状態保持）
  - **Process death対応**（メモリ不足でアプリ強制終了後の状態復元）
  - rememberSaveable（Compose UI状態の保持）
- **画面内容**:
  - チェーン店詳細情報（ロゴ、説明、公式URL）
  - 過去のキャンペーン履歴
  - お気に入り登録ボタン
- **Process death対応の実装ポイント**:
  - ViewModelの状態をSavedStateHandleで保存
  - Navigation引数をSavedStateHandleから復元
  - UIスクロール位置をrememberSaveableで保持
  - 面接での説明ポイント：「なぜ通常のviewModelScopeでは不十分か」

#### 005-background-sync（バックグラウンド同期）

WorkManagerを使用したバックグラウンドデータ同期

- **目的**: WorkManagerの使用経験を証明
- **技術要素**:
  - WorkManager（PeriodicWorkRequest）
  - Room Database連携
  - ネットワーク状態監視
- **機能**:
  - 定期的なFirestore→Room同期（1日1回）
  - Wi-Fi接続時のみ実行
  - 同期完了通知（オプション）

#### 006-statistics（統計画面）

Compose Chartsを使用したグラフ描画

- **目的**: UI応用力（グラフ描画）の証明
- **技術要素**:
  - Compose Charts（折れ線グラフ）
  - Canvas / CustomLayout
  - アニメーション
- **画面内容**:
  - チェーン店お気に入り数の推移グラフ
  - 期間選択（週/月/全期間）
  - データポイントのインタラクション

### 優先順位

| 順位 | 機能 | 理由 |
|-----|------|------|
| 1 | 004-chain-detail | Navigation/SavedStateHandleは必須スキル |
| 2 | 006-statistics | グラフ描画は求人要件で頻出 |
| 3 | 005-background-sync | WorkManagerは実用性が高い |

### 技術スタック（追加）

| 機能 | ライブラリ | 公式ドキュメント |
|-----|-----------|-----------------|
| 詳細画面 | Navigation Compose | [Navigation](https://developer.android.com/guide/navigation) |
| 状態保持 | SavedStateHandle | [ViewModel SavedState](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate) |
| バックグラウンド同期 | WorkManager | [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) |
| グラフ描画 | Compose Charts | [vico](https://github.com/patrykandpatrick/vico) または自作 |

---

## 重要な方針変更（2025/11/27）

### データモデルの変更：「メニュー」→「キャンペーン」単位

| 項目 | 変更前 | 変更後 |
|------|--------|--------|
| 管理単位 | 個別メニュー | キャンペーン |
| お気に入り対象 | メニュー | チェーン店 |
| 画面構成 | メニュー一覧（フィルタ） | チェーン一覧 → キャンペーン |

### 開発優先順位の変更：Android開発を優先

| 順序 | 変更前 | 変更後 |
|------|--------|--------|
| Phase2-1 | 管理画面 | 管理画面（変更なし） |
| Phase2-2 | Webアプリ | **Androidアプリ** |
| Phase3以降 | - | Webアプリ（延期） |

### 理由

1. **運用負荷の軽減**：キャンペーン単位の方が手動運用に適している
2. **就活目的**：Jetpack Compose + 最新Jetpackライブラリの実践経験が必要
3. **Firebase親和性**：FirebaseはAndroidとの統合が最も充実している

---

## 方針変更（2025/12/07）

### ポートフォリオ戦略としてのYAGNI緩和

就活ポートフォリオとして技術幅を見せるため、以下の方針を採用

| 原則 | 通常 | 本プロジェクト |
|------|------|---------------|
| YAGNI | 必要になるまで実装しない | 技術要素を網羅的に導入 |
| モジュール構成 | 必要最小限から段階的に | マルチモジュールを早期に構築 |
| グラフ描画 | 必要に応じて | 求人要件に含まれるため積極導入 |

### Spec-Driven + Test-Firstは維持

- Spec Kitワークフローに従う
- Test-Firstはリスクベースで実施（Android版constitution.mdに準拠）

### CI/CD First

機能開発の前にCI/CD基盤を構築（Phase0として実施）

---

## 更新履歴

- 2026/03/12：003-favorites US2（お気に入り登録数の表示）完了、Optimistic UI実装
- 2025/12/16：003-favorites US1（お気に入り登録・解除）完了、Googleログイン機能追加
- 2025/12/15：Phase2.5（追加技術デモ）を追加、詳細画面/WorkManager/グラフ描画を計画
- 2025/12/14：マルチモジュール構成をAndroid公式ドキュメント準拠に更新、ADR-002作成
- 2025/12/14：Android技術選定の再確認完了、ADR-001作成、research.md更新
- 2025/12/14：Phase0-3完了（Lint/Detekt/Hilt/JUnit5/MockK/Turbine/JaCoCo）
- 2025/12/14：Phase0-2完了、JaCoCoをPhase0-3に移動、ci.ymlのbuildジョブ軽量化
- 2025/12/14：Phase0-1完了、Phase0-1.5→Phase0-5に移動（MVP後）
- 2025/12/11：Phase0-1.5（Firebaseセキュリティ対策）を追加
- 2025/12/10：Phase0（CI/CD基盤構築）を追加、方針変更（ポートフォリオ戦略）を記載
- 2025/12/05：共有ファイルをシンボリックリンク方式に移行、README.md作成、CLAUDE.mdに前提条件・ガバナンス構成を追加
- 2025/12/03：limimeshi-docsからAndroid関連タスクを抽出して作成
