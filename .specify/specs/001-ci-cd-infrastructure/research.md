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

## 8. Android Lint

### 選定理由
- Android SDK標準の静的解析ツール
- 追加設定なしで利用可能
- Android固有の問題を検出

### カスタマイズ
- `lint.xml`: ルールの有効/無効、重大度変更
- `app/build.gradle.kts`: `lintOptions`設定

### 主要ルール

| カテゴリ | 例 |
|---------|-----|
| Correctness | 非推奨API使用、リソース参照エラー |
| Performance | ViewHolderパターン未使用、過剰な描画 |
| Security | ハードコードされた秘密情報 |
| Accessibility | コンテンツ説明欠如 |

---

## 9. Detekt

### 選定理由
- Kotlin専用の静的解析ツール
- コードスメル検出に特化
- Android Lintと補完関係

### バージョン
- detekt: 1.23.x（最新安定版）

### 設定ファイル
- `config/detekt/detekt.yml`

### 主要ルールセット

| ルールセット | 内容 |
|-------------|------|
| complexity | 循環的複雑度、メソッド長 |
| naming | 命名規則（camelCase等） |
| style | コーディングスタイル |
| potential-bugs | バグになりやすいパターン |

---

## 10. Hilt

### 選定理由
- Android公式推奨のDIフレームワーク
- Dagger2をAndroid向けに簡略化
- Jetpack統合（ViewModel、WorkManager等）

### バージョン
- hilt: 2.51.x（最新安定版）

### 主要アノテーション

| アノテーション | 用途 |
|---------------|------|
| @HiltAndroidApp | Applicationクラス |
| @AndroidEntryPoint | Activity/Fragment |
| @Inject | コンストラクタインジェクション |
| @Module | 依存関係定義モジュール |
| @Provides | インスタンス提供メソッド |
| @Singleton | シングルトンスコープ |

### セットアップ

```kotlin
// Application
@HiltAndroidApp
class LimimeshiApplication : Application()

// Activity
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

---

## 11. JUnit5

### 選定理由
- モダンなテストフレームワーク
- パラメータ化テスト、ネストテスト対応
- より柔軟なライフサイクル

### バージョン
- junit-jupiter: 5.10.x

### JUnit4との違い

| 機能 | JUnit4 | JUnit5 |
|------|--------|--------|
| パッケージ | org.junit | org.junit.jupiter |
| アサーション | Assert.assertEquals | Assertions.assertEquals |
| ライフサイクル | @Before/@After | @BeforeEach/@AfterEach |
| パラメータ化 | @Parameterized | @ParameterizedTest |

### Android対応

```kotlin
// build.gradle.kts
tasks.withType<Test> {
    useJUnitPlatform()
}
```

---

## 12. MockK

### 選定理由
- Kotlin専用モックライブラリ
- コルーチン対応
- DSLによる可読性の高いモック定義

### バージョン
- mockk: 1.13.x

### 基本パターン

```kotlin
val repository = mockk<UserRepository>()

// スタブ設定
every { repository.getUser(any()) } returns User("test")

// コルーチン対応
coEvery { repository.fetchUser(any()) } returns User("test")

// 検証
verify { repository.getUser("123") }
```

### Mockito比較

| 機能 | Mockito | MockK |
|------|---------|-------|
| 構文 | Java風 | Kotlin DSL |
| final class | mockito-inline必要 | デフォルト対応 |
| コルーチン | 追加設定必要 | coEvery/coVerify |

---

## 13. Turbine

### 選定理由
- Flow/StateFlowテスト専用ライブラリ
- Cash App製（Square系）
- シンプルなAPI

### バージョン
- turbine: 1.1.x

### 基本パターン

```kotlin
@Test
fun `flow emits values in order`() = runTest {
    val flow = flowOf(1, 2, 3)

    flow.test {
        assertEquals(1, awaitItem())
        assertEquals(2, awaitItem())
        assertEquals(3, awaitItem())
        awaitComplete()
    }
}
```

### StateFlowテスト

```kotlin
@Test
fun `stateflow updates correctly`() = runTest {
    val viewModel = MyViewModel()

    viewModel.uiState.test {
        assertEquals(UiState.Loading, awaitItem())
        viewModel.loadData()
        assertEquals(UiState.Success, awaitItem())
    }
}
```

---

## 14. JaCoCo

### 選定理由
- Javaエコシステムで標準的なカバレッジツール
- Gradle統合が容易
- HTML/XMLレポート生成

### レポート種類

| 形式 | 用途 |
|------|------|
| HTML | ブラウザで視覚的に確認 |
| XML | CI/CDツール連携（Codecov等） |
| CSV | 外部ツールでの分析 |

### Gradle設定

```kotlin
plugins {
    id("jacoco")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
```

### 除外パターン
- 自動生成コード（Hilt、Compose）
- BuildConfig、R.class

---

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Firebase App Distribution](https://firebase.google.com/docs/app-distribution)
- [Google Play Developer API](https://developers.google.com/android-publisher)
- [Timber GitHub](https://github.com/JakeWharton/timber)
- [Firebase Analytics](https://firebase.google.com/docs/analytics)
- [Android Lint](https://developer.android.com/studio/write/lint)
- [Detekt](https://detekt.dev/)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [JUnit5](https://junit.org/junit5/)
- [MockK](https://mockk.io/)
- [Turbine](https://github.com/cashapp/turbine)
- [JaCoCo](https://www.jacoco.org/jacoco/)
