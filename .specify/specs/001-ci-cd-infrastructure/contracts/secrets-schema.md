# Contracts: Secrets & Workflow Schema

このドキュメントはCI/CD基盤で使用するSecrets、ワークフロー構成、環境変数の仕様を定義します。

---

## 1. GitHub Secrets

### Firebase関連

| Secret名 | 説明 | エンコード | 取得元 |
|----------|------|----------|--------|
| `GOOGLE_SERVICES_JSON_DEV` | dev環境のFirebase設定 | Base64 | Firebase Console → プロジェクト設定 |
| `GOOGLE_SERVICES_JSON_PROD` | prod環境のFirebase設定 | Base64 | Firebase Console → プロジェクト設定 |
| `FIREBASE_APP_ID_DEV` | dev環境のApp ID | Plain | Firebase Console → プロジェクト設定 |
| `FIREBASE_SERVICE_ACCOUNT` | Firebaseサービスアカウント | Base64 | Google Cloud Console → IAM |

### 署名関連

| Secret名 | 説明 | エンコード | 取得元 |
|----------|------|----------|--------|
| `KEYSTORE_BASE64` | upload-keystore.jks | Base64 | ローカルで生成 |
| `KEYSTORE_PASSWORD` | キーストアパスワード | Plain | ローカルで設定 |
| `KEY_ALIAS` | 鍵エイリアス | Plain | キーストア生成時に設定 |
| `KEY_PASSWORD` | 鍵パスワード | Plain | キーストア生成時に設定 |

### Google Play関連

| Secret名 | 説明 | エンコード | 取得元 |
|----------|------|----------|--------|
| `PLAY_SERVICE_ACCOUNT` | Google Playサービスアカウント | Base64 | Google Cloud Console → IAM |

---

## 2. Base64エンコード手順

```bash
# エンコード
base64 -i input-file -o output-base64.txt

# デコード（ワークフロー内）
echo "$SECRET" | base64 --decode > output-file
```

---

## 3. Workflow構成

### ci.yml

```yaml
name: CI
on:
  pull_request:
    branches: [develop, main]
  push:
    branches: [develop]

jobs:
  lint:
    runs-on: ubuntu-latest
    timeout-minutes: 10
    # → ./gradlew lint

  test:
    runs-on: ubuntu-latest
    timeout-minutes: 10
    # → ./gradlew test

  build:
    runs-on: ubuntu-latest
    timeout-minutes: 15
    needs: [lint, test]
    # → ./gradlew assembleDevDebug assembleProdDebug
```

### cd-firebase.yml

```yaml
name: CD Firebase
on:
  push:
    branches: [release]

jobs:
  deploy:
    runs-on: ubuntu-latest
    timeout-minutes: 20
    # → ./gradlew assembleDevRelease
    # → Firebase App Distribution
```

### cd-play.yml

```yaml
name: CD Google Play
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    timeout-minutes: 20
    # → ./gradlew bundleProdRelease
    # → Google Play Internal Test
```

---

## 4. 環境変数

### ビルド時

| 変数名 | 用途 | 設定元 |
|--------|------|--------|
| `VERSION_CODE` | versionCode動的生成 | `github.run_number` |
| `KEYSTORE_PASSWORD` | 署名パスワード | Secrets |
| `KEY_ALIAS` | 鍵エイリアス | Secrets |
| `KEY_PASSWORD` | 鍵パスワード | Secrets |

### build.gradle.kts での参照

```kotlin
defaultConfig {
    versionCode = (System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1)
}

signingConfigs {
    create("release") {
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
        keyAlias = System.getenv("KEY_ALIAS") ?: ""
        keyPassword = System.getenv("KEY_PASSWORD") ?: ""
    }
}
```

---

## 5. サービスアカウント権限

### Firebase App Distribution

| 権限 | 説明 |
|------|------|
| Firebase App Distribution Admin SDK サービスエージェント | APKのアップロード、テスターへの配信 |

### Google Play

| 権限 | 説明 |
|------|------|
| リリースの管理 | 内部テストトラックへのアップロード |

---

## 6. 出力ファイルパス

### CI Artifacts

| ビルド | パス |
|--------|------|
| devDebug APK | `app/build/outputs/apk/dev/debug/app-dev-debug.apk` |
| prodDebug APK | `app/build/outputs/apk/prod/debug/app-prod-debug.apk` |

### CD成果物

| ビルド | パス | 配信先 |
|--------|------|--------|
| devRelease APK | `app/build/outputs/apk/dev/release/app-dev-release.apk` | Firebase App Distribution |
| prodRelease AAB | `app/build/outputs/bundle/prodRelease/app-prod-release.aab` | Google Play |

---

## 7. Concurrency設定

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

| 設定 | 値 | 説明 |
|------|-----|------|
| group | workflow-ref | 同一ワークフロー・同一ブランチをグループ化 |
| cancel-in-progress | true | 新しい実行が始まると古い実行をキャンセル |

---

## 8. タイムアウト設定

| ワークフロー | ジョブ | timeout-minutes |
|-------------|--------|-----------------|
| ci.yml | lint | 10 |
| ci.yml | test | 10 |
| ci.yml | build | 15 |
| cd-firebase.yml | deploy | 20 |
| cd-play.yml | deploy | 20 |

---

## 9. アーティファクト保持期間

```yaml
- uses: actions/upload-artifact@v4
  with:
    retention-days: 7
```

| 設定 | 値 | 説明 |
|------|-----|------|
| retention-days | 7 | 7日後に自動削除（デフォルト90日から短縮） |
