# limimeshi-android ロードマップ

**元ドキュメント**: [limimeshi-docs/roadmap.md](https://github.com/shg25/limimeshi-docs/blob/main/roadmap.md)

このドキュメントはlimimeshi-docsのロードマップからAndroid関連のタスクを抽出したもの。
プロジェクト全体のロードマップは上記リンクを参照。

---

## 現在のステータス

| フェーズ | ステータス |
|---------|-----------|
| Phase1：設計・仕様策定 | ✅ 完了 |
| Phase0：CI/CD基盤構築 | 🚧 進行中 |
| Phase2：MVP実装 | ⏳ 待機中 |

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

#### Phase0-2：CI/CD構築
- [x] GitHub Actions設定（lint/test/build）
- [x] Firebase App Distribution配信（devRelease）
- [x] Google Play内部テスト配信（prodRelease）※status: draft
- [ ] JaCoCo カバレッジ計測

#### Phase0-3：品質基盤（予定）
- [ ] Lint設定
- [ ] Detekt設定
- [ ] Hilt基本設定
- [ ] JUnit5/MockK/Turbine導入

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
- [ ] **Android技術選定の再確認**（詳細は下記参照）

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
| テスト | JUnit 5 + MockK + Turbine | ※要再確認（公式はJUnit 4 + Mockito） |
| UIテスト | Compose Testing | Android公式 |

### 着手時の確認タスク

- [ ] [Android Developers - Testing](https://developer.android.com/training/testing)で推奨ライブラリを確認
- [ ] [Android Developers - App architecture](https://developer.android.com/topic/architecture)で最新パターンを確認
- [ ] 就活求人で求められているライブラリを調査・反映
- [ ] `specs/002-chain-list/research.md`を最新化
- [ ] `specs/003-favorites/research.md`を最新化（002と整合性を取る）
- [ ] 技術選定の決定をADRとして記録（`docs/adr/`）

### 参考リンク

- [Android Developers](https://developer.android.com/)
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3 for Android](https://m3.material.io/)

---

## MVP開発（002-chain-list、003-favorites）

### 対象機能

1. **002-chain-list（チェーン店一覧）**
   - チェーン一覧表示
   - キャンペーン一覧表示（チェーン別）
   - X Post埋め込み表示
   - ソート順選択（新着順/ふりがな順）
   - お気に入りフィルタ（003と連携）

2. **003-favorites（お気に入り登録）**
   - チェーン店お気に入り登録・解除
   - お気に入り登録数の表示
   - Firestoreへの永続化

### 技術スタック

- Kotlin + Jetpack Compose + Firebase
- 詳細は`.specify/specs/`配下の各spec.md、research.md参照

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

- 2025/12/14：Phase0-1完了、Phase0-1.5→Phase0-5に移動（MVP後）
- 2025/12/11：Phase0-1.5（Firebaseセキュリティ対策）を追加
- 2025/12/10：Phase0（CI/CD基盤構築）を追加、方針変更（ポートフォリオ戦略）を記載
- 2025/12/05：共有ファイルをシンボリックリンク方式に移行、README.md作成、CLAUDE.mdに前提条件・ガバナンス構成を追加
- 2025/12/03：limimeshi-docsからAndroid関連タスクを抽出して作成
