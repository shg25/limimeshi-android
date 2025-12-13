# Tasks: CI/CD Infrastructure

- **Input**: Design documents from `/specs/001-ci-cd-infrastructure/`
- **Prerequisites**: plan.md (required), spec.md (required for user stories)
- **Status**: ✅ All tasks completed (2025-12-14)

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 並列実行可能（異なるファイル、依存関係なし）
- **[Story]**: このタスクが属するユーザーストーリー（US1-US4）

---

## Phase 1: CI構築（User Story 1）

**Purpose**: PR/push時の自動ビルド・テスト

- [x] T001 ci.ymlワークフロー作成（.github/workflows/ci.yml）
- [x] T002 lintジョブ設定（./gradlew lint）
- [x] T003 testジョブ設定（./gradlew test）
- [x] T004 buildジョブ設定（devDebug/prodDebug）
- [x] T005 google-services.jsonをGitHub Secretsに登録（GOOGLE_SERVICES_JSON_DEV/PROD）
- [x] T006 ci.ymlでgoogle-services.json復元ステップ追加
- [x] T007 concurrency設定（同一ブランチの重複実行キャンセル）
- [x] T008 artifact retention-days設定（7日）
- [x] T009 動作確認（PRでCIが実行されることを確認）

**Checkpoint**: CI基盤完成、PRマージ前の品質チェックが自動化

---

## Phase 2: CD構築 - Firebase App Distribution（User Story 2）

**Purpose**: releaseブランチへのpush時にdevRelease APKを自動配信

- [x] T010 cd-firebase.ymlワークフロー作成（.github/workflows/cd-firebase.yml）
- [x] T011 署名鍵をGitHub Secretsに登録（KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD）
- [x] T012 app/build.gradle.ktsに署名設定追加（環境変数から読み込み）
- [x] T013 Firebaseサービスアカウント作成（Google Cloud Console）
- [x] T014 FIREBASE_SERVICE_ACCOUNT, FIREBASE_APP_ID_DEVをSecretsに登録
- [x] T015 wzieba/Firebase-Distribution-Github-Actionでアップロード設定
- [x] T016 テスターグループ作成（Firebase Console）
- [x] T017 動作確認（releaseブランチpushでApp Distributionに配信）

**Checkpoint**: Firebase App Distribution配信自動化完了

---

## Phase 3: CD構築 - Google Play（User Story 3）

**Purpose**: mainブランチへのpush時にprodRelease AABを内部テストに配信

- [x] T018 cd-play.ymlワークフロー作成（.github/workflows/cd-play.yml）
- [x] T019 Google Playサービスアカウント作成（Google Cloud Console）
- [x] T020 Google Play Console APIを有効化
- [x] T021 PLAY_SERVICE_ACCOUNTをSecretsに登録
- [x] T022 r0adkll/upload-google-playでアップロード設定
- [x] T023 versionCode動的生成（github.run_number使用）
- [x] T024 status: draft設定（アプリ未公開のため）
- [x] T025 動作確認（mainブランチpushでGoogle Play内部テストにアップロード）

**Checkpoint**: Google Play内部テスト配信自動化完了

---

## Phase 4: Firebase Observability（User Story 4）

**Purpose**: Crashlytics/Analyticsの初期化とログ管理

- [x] T026 Timber依存関係追加（gradle/libs.versions.toml, app/build.gradle.kts）
- [x] T027 buildConfig = true設定（BuildConfig.DEBUG参照のため）
- [x] T028 LimimeshiApplication.kt作成（Timber初期化）
- [x] T029 CrashlyticsTree実装（WARN以上をCrashlyticsに送信）
- [x] T030 AndroidManifest.xmlにApplication登録
- [x] T031 AnalyticsHelper.kt作成（Firebase Analyticsラッパー）
- [x] T032 動作確認（devDebug: Logcat出力）
- [x] T033 動作確認（devRelease: Crashlytics送信）

**Checkpoint**: Firebase Observability完了

---

## Summary

| Phase | タスク数 | 完了 |
|-------|---------|------|
| Phase 1: CI構築 | 9 | 9 |
| Phase 2: CD Firebase | 8 | 8 |
| Phase 3: CD Google Play | 8 | 8 |
| Phase 4: Observability | 8 | 8 |
| **Total** | **33** | **33** |
