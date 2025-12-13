# Quickstart: CI/CD Infrastructure

このドキュメントはCI/CD基盤の使い方と拡張方法を説明します。

## 1. CI/CDフロー概要

```
[開発者] → PR作成 → CI実行（lint/test/build）
                    ↓ マージ
[develop] → releaseブランチ作成 → CD Firebase実行 → App Distribution配信
                    ↓ マージ
[main] → CD Google Play実行 → 内部テスト配信
```

---

## 2. ワークフロー一覧

| ワークフロー | トリガー | 成果物 |
|-------------|---------|--------|
| ci.yml | PR / develop push | lint結果、テスト結果、APK |
| cd-firebase.yml | release push | devRelease APK → App Distribution |
| cd-play.yml | main push | prodRelease AAB → Google Play内部テスト |

---

## 3. CIの使い方

### PRを作成する

```bash
# featureブランチで作業
git checkout -b feature/xxx
# 作業...
git push -u origin feature/xxx
# GitHubでPR作成 → CIが自動実行
```

### CI結果の確認

1. PRページの「Checks」タブを確認
2. 失敗した場合は「Details」でログを確認
3. lint/testを修正してpush → 再実行

---

## 4. Firebase App Distribution配信

### 配信トリガー

```bash
# releaseブランチにマージ
git checkout release
git merge develop
git push
# → cd-firebase.ymlが自動実行
```

### 配信確認

1. GitHub Actions → cd-firebase のログを確認
2. Firebase Console → App Distribution → リリースを確認
3. テスターにメール通知が届く

### テスターの追加

1. Firebase Console → App Distribution → テスターとグループ
2. 「testers」グループにメールアドレスを追加

---

## 5. Google Play内部テスト配信

### 配信トリガー

```bash
# mainブランチにマージ
git checkout main
git merge release
git push
# → cd-play.ymlが自動実行
```

### 配信確認

1. GitHub Actions → cd-play のログを確認
2. Google Play Console → リリース → 内部テストを確認
3. status: draft のため手動で「公開開始」が必要

### status: completed への変更（アプリ公開後）

```yaml
# cd-play.yml
- name: Upload to Google Play Internal Test
  uses: r0adkll/upload-google-play@v1
  with:
    # ...
    status: completed  # ← draftからcompletedに変更
```

---

## 6. Timber/Crashlytics の使い方

### ログ出力

```kotlin
import timber.log.Timber

// デバッグログ（devDebugのみLogcat出力）
Timber.d("Debug message")

// 情報ログ（devDebugのみLogcat出力）
Timber.i("Info message")

// 警告ログ（Crashlyticsにも送信）
Timber.w("Warning message")

// エラーログ（Crashlyticsにも送信）
Timber.e("Error message")

// 例外付きエラーログ（スタックトレースがCrashlyticsに記録）
Timber.e(exception, "Error with exception")
```

### Crashlytics確認

1. Firebase Console → Crashlytics
2. 「イベントログ」でカスタムログを確認
3. 「問題」で例外を確認

---

## 7. Analytics の使い方

### 画面表示イベント

```kotlin
import com.shg25.limimeshi.util.AnalyticsHelper

// 画面表示を記録
AnalyticsHelper.logScreenView("ChainListScreen")
```

### お気に入りイベント

```kotlin
// お気に入り追加
AnalyticsHelper.logAddFavorite(
    chainId = "chain_001",
    chainName = "マクドナルド"
)

// お気に入り解除
AnalyticsHelper.logRemoveFavorite(
    chainId = "chain_001",
    chainName = "マクドナルド"
)
```

### その他のイベント

```kotlin
// キャンペーン詳細表示
AnalyticsHelper.logViewCampaign(
    campaignId = "campaign_001",
    campaignName = "期間限定バーガー",
    chainName = "マクドナルド"
)

// ソート順変更
AnalyticsHelper.logChangeSortOrder("new")  // or "kana"

// お気に入りフィルタ切り替え
AnalyticsHelper.logToggleFavoriteFilter(enabled = true)
```

### Analytics確認

1. Firebase Console → Analytics
2. 「イベント」で送信されたイベントを確認
3. リアルタイム確認は「DebugView」を使用

---

## 8. Secrets の追加・更新

### 新しいSecretの追加

1. GitHub → Settings → Secrets and variables → Actions
2. 「New repository secret」をクリック
3. Name と Value を入力して保存

### Base64エンコードが必要な場合

```bash
# ファイルをBase64エンコード
base64 -i input-file.json -o output-base64.txt
# output-base64.txtの内容をSecretsに登録
```

### 現在のSecrets一覧

| Secret | 用途 |
|--------|------|
| GOOGLE_SERVICES_JSON_DEV | dev環境のFirebase設定 |
| GOOGLE_SERVICES_JSON_PROD | prod環境のFirebase設定 |
| KEYSTORE_BASE64 | 署名鍵（Base64） |
| KEYSTORE_PASSWORD | キーストアパスワード |
| KEY_ALIAS | 鍵エイリアス |
| KEY_PASSWORD | 鍵パスワード |
| FIREBASE_APP_ID_DEV | Firebase App ID（dev） |
| FIREBASE_SERVICE_ACCOUNT | Firebaseサービスアカウント（Base64） |
| PLAY_SERVICE_ACCOUNT | Google Playサービスアカウント（Base64） |

---

## 9. トラブルシューティング

### CI失敗：google-services.json not found

**原因**: Secretsの設定漏れまたはBase64デコード失敗

**対処**:
1. GitHub Secrets で GOOGLE_SERVICES_JSON_DEV/PROD を確認
2. 正しくBase64エンコードされているか確認

### CD失敗：Signature mismatch

**原因**: 署名鍵が異なる

**対処**:
1. KEYSTORE_BASE64 が正しいファイルか確認
2. KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD が正しいか確認

### CD失敗：Version code already used

**原因**: 同じversionCodeでアップロード済み

**対処**:
- 通常は github.run_number で自動増加するため発生しない
- 手動でアップロードした場合は、そのversionCodeより大きい値が必要

### CD失敗：API not enabled

**原因**: Google Play Developer API が無効

**対処**:
1. Google Cloud Console → APIs & Services
2. 「Google Play Android Developer API」を有効化

---

## 10. 拡張方法

### 新しいジョブの追加

```yaml
jobs:
  existing-job:
    # ...

  new-job:
    runs-on: ubuntu-latest
    needs: [existing-job]  # 依存関係
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      # ...
```

### 手動トリガーの追加

```yaml
on:
  workflow_dispatch:  # 手動実行可能に
    inputs:
      environment:
        description: 'Deployment environment'
        required: true
        default: 'dev'
```

### マトリックスビルド

```yaml
jobs:
  build:
    strategy:
      matrix:
        flavor: [dev, prod]
        buildType: [debug, release]
    steps:
      - run: ./gradlew assemble${{ matrix.flavor }}${{ matrix.buildType }}
```
