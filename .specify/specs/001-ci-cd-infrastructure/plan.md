# Implementation Plan: CI/CD Infrastructure

- **Branch**: `feature/phase0-ci-cd-firebase`
- **Date**: 2025-12-10
- **Spec**: [spec.md](./spec.md)
- **Input**: Phase0としてCI/CD基盤を構築
- **Status**: ✅ Implemented (2025-12-14)

## Summary

GitHub Actionsを使用したCI/CD基盤の構築。PR/push時の自動lint/test/build、Firebase App Distributionへのdev版自動配信、Google Play内部テストへのprod版自動配信を実現。加えてFirebase Observability（Crashlytics + Analytics）の初期化も含む。

**技術アプローチ**:
- GitHub Actions によるワークフロー自動化
- Firebase App Distribution でテスター向け配信
- Google Play Console API で内部テスト配信
- Timber + Crashlytics でログ管理
- Firebase Analytics でユーザー行動分析

## Technical Context

**CI/CD Platform**: GitHub Actions
**Build System**: Gradle 8.x, AGP 8.x
**Distribution**: Firebase App Distribution, Google Play Console
**Secrets Management**: GitHub Secrets (Base64エンコード)
**Observability**: Firebase Crashlytics, Firebase Analytics, Timber

## Constitution Check

### ✅ I. Spec-Driven（仕様駆動開発）
- **Status**: PASS
- **Evidence**: spec.md作成済み、実装後にplan.md/tasks.md追記

### ✅ II. Test-First（テスト駆動）
- **Status**: PARTIAL
- **Evidence**: CI/CDはインフラのため単体テスト対象外。動作確認は実環境で実施済み

### ✅ III. Simplicity（シンプルさ優先）
- **Status**: PASS
- **Evidence**: 標準的なGitHub Actionsワークフロー、複雑なカスタムアクション不使用

### ✅ IV. Firebase-First
- **Status**: PASS
- **Evidence**: Firebase App Distribution、Crashlytics、Analytics使用

## Implementation Phases

### Phase 1: CI構築
1. ci.yml作成（lint → test → build）
2. google-services.jsonのSecrets化
3. 動作確認

### Phase 2: CD構築（Firebase）
1. cd-firebase.yml作成
2. 署名鍵のSecrets化
3. Firebaseサービスアカウント設定
4. 動作確認

### Phase 3: CD構築（Google Play）
1. cd-play.yml作成
2. Google Playサービスアカウント設定
3. 動作確認（status: draft）

### Phase 4: Firebase Observability
1. Timber依存関係追加
2. LimimeshiApplication作成（Timber初期化）
3. CrashlyticsTree実装
4. AnalyticsHelper実装
5. 動作確認（devDebug/devRelease）

## Decisions Made

| 決定事項 | 選択 | 理由 |
|---------|------|------|
| CI実行タイミング | PR + develop push | PRで品質チェック、developで統合確認 |
| Firebase配信トリガー | releaseブランチ | git-flowに準拠 |
| Google Play配信トリガー | mainブランチ | 本番リリースフローに準拠 |
| Google Play status | draft | アプリ未公開のため。公開後にcompletedへ変更 |
| Crashlytics送信レベル | WARN以上 | DEBUGログでCrashlyticsを汚さない |
| versionCode生成 | github.run_number | 自動でユニークな値を生成 |

## Files Created/Modified

### Workflows
- `.github/workflows/ci.yml` - CI（lint/test/build）
- `.github/workflows/cd-firebase.yml` - Firebase App Distribution配信
- `.github/workflows/cd-play.yml` - Google Play内部テスト配信

### Application
- `app/src/main/java/com/shg25/limimeshi/LimimeshiApplication.kt` - Application + Timber初期化
- `app/src/main/java/com/shg25/limimeshi/util/AnalyticsHelper.kt` - Analytics wrapper

### Configuration
- `app/build.gradle.kts` - 署名設定、Timber依存関係、buildConfig有効化
- `gradle/libs.versions.toml` - Timber追加
- `AndroidManifest.xml` - Application登録
- `.gitignore` - .claude/settings.local.json追加
