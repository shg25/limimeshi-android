# Research: CI/CD Infrastructure

- **Input**: spec.md requirements
- **Date**: 2025-12-14

## 1. GitHub Actions

### 選定理由
- GitHubリポジトリとのネイティブ統合
- 無料枠（2000分/月）で十分
- YAMLベースで学習コストが低い

### 主要概念

| 概念 | 説明 |
|------|------|
| Workflow | `.github/workflows/*.yml`で定義される自動化プロセス |
| Job | ワークフロー内の実行単位、並列/直列実行可能 |
| Step | ジョブ内の個々のタスク |
| Runner | ワークフローを実行する仮想マシン |
| Action | 再利用可能なワークフローコンポーネント |

### 使用Action

| Action | バージョン | 用途 |
|--------|----------|------|
| actions/checkout | v4 | リポジトリのチェックアウト |
| actions/setup-java | v4 | JDK 17セットアップ |
| gradle/actions/setup-gradle | v4 | Gradleセットアップ・キャッシュ |
| actions/upload-artifact | v4 | ビルド成果物のアップロード |

### トリガー設定

```yaml
on:
  pull_request:
    branches: [develop, main]  # PRをこれらのブランチに向けた時
  push:
    branches: [develop]        # developへの直接push時
```

### Concurrency

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

同一ブランチでの重複実行をキャンセルし、リソースを節約。

---

## 2. Firebase App Distribution

### 選定理由
- Firebase統合済み（Crashlytics、Analytics）
- テスターへのメール通知機能
- APKの直接配布（Playストア経由不要）

### 使用Action

**wzieba/Firebase-Distribution-Github-Action@v1**

コミュニティ製だが、500+スターの実績あり。Firebase公式のGitHub Actionは存在しない。

### 代替手段
```bash
# firebase-tools CLI直接実行
npm install -g firebase-tools
firebase appdistribution:distribute app.apk --app $APP_ID --groups testers
```

### サービスアカウント権限
- Firebase App Distribution Admin SDK サービスエージェント

---

## 3. Google Play Console API

### 選定理由
- 内部テストトラックへの自動アップロード
- 手動アップロードの手間削減

### 使用Action

**r0adkll/upload-google-play@v1**

コミュニティ製、Google Play公式ではないがデファクトスタンダード。

### サービスアカウント設定
1. Google Cloud Consoleでサービスアカウント作成
2. Google Play Console → API Access でサービスアカウントを招待
3. 権限付与：「リリースの管理」

### status パラメータ

| status | 説明 |
|--------|------|
| draft | アップロードのみ、手動で公開が必要 |
| completed | 自動で公開（アプリ公開後のみ使用可能） |

**注意**: アプリ未公開時は `status: draft` のみ使用可能

---

## 4. Timber

### 選定理由
- Android標準ロギングの改善版
- Tree構造で出力先を柔軟に切り替え
- タグの自動生成（クラス名）

### バージョン
- timber: 5.0.1

### Tree実装パターン

```kotlin
// デバッグビルド
Timber.plant(Timber.DebugTree())  // → Logcat

// リリースビルド
Timber.plant(CrashlyticsTree())   // → Crashlytics
```

### CrashlyticsTree設計

```kotlin
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return  // DEBUG/INFOは除外
        // Crashlyticsに送信
    }
}
```

---

## 5. Firebase Analytics

### 選定理由
- Firebase統合済み
- 無料で十分な機能
- Crashlyticsとの連携

### 主要イベント

| イベント | パラメータ | 用途 |
|---------|----------|------|
| screen_view | screen_name, screen_class | 画面表示 |
| add_favorite | chain_id, chain_name | お気に入り追加 |
| remove_favorite | chain_id, chain_name | お気に入り解除 |
| view_campaign | campaign_id, campaign_name | キャンペーン詳細 |
| change_sort_order | sort_type | ソート順変更 |

### AnalyticsHelper設計

シングルトンオブジェクトとして実装し、どこからでも呼び出し可能に。

---

## 6. 署名管理

### 署名の種類

| 署名 | 用途 | 管理場所 |
|------|------|---------|
| Debug署名 | ローカル開発 | Android SDKデフォルト |
| Upload署名 | ストアアップロード用 | GitHub Secrets |
| App署名 | 実際のアプリ署名 | Google管理（Play App Signing） |

### Secrets管理

| Secret | 内容 | エンコード |
|--------|------|----------|
| KEYSTORE_BASE64 | upload-keystore.jks | Base64 |
| KEYSTORE_PASSWORD | キーストアパスワード | プレーンテキスト |
| KEY_ALIAS | 鍵エイリアス | プレーンテキスト |
| KEY_PASSWORD | 鍵パスワード | プレーンテキスト |

### 環境変数からの読み込み

```kotlin
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("KEYSTORE_FILE") ?: "upload-keystore.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
        // ...
    }
}
```

---

## 7. Build Variants

### 構成

| Flavor | Build Type | 用途 |
|--------|-----------|------|
| dev | debug | ローカル開発 |
| dev | release | Firebase App Distribution |
| prod | debug | 本番環境デバッグ |
| prod | release | Google Play配信 |

### パッケージ名

- dev: `com.shg25.limimeshi.dev`
- prod: `com.shg25.limimeshi`

dev/prodは共存可能（同一端末に両方インストール可）

---

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Firebase App Distribution](https://firebase.google.com/docs/app-distribution)
- [Google Play Developer API](https://developers.google.com/android-publisher)
- [Timber GitHub](https://github.com/JakeWharton/timber)
- [Firebase Analytics](https://firebase.google.com/docs/analytics)
