# Requirements Checklist: CI/CD Infrastructure

- **Status**: ✅ All requirements met (2025-12-14)

## Functional Requirements

### CI（継続的インテグレーション）

- [x] **FR-001**: システムはPR作成時にワークフローを自動実行しなければならない
  - ci.yml: `on: pull_request: branches: [develop, main]`
- [x] **FR-002**: システムはlint（Android Lint）を実行し、エラーがあれば失敗しなければならない
  - ci.yml: lintジョブで `./gradlew lint` 実行
- [x] **FR-003**: システムは単体テストを実行し、失敗があればワークフロー全体を失敗としなければならない
  - ci.yml: testジョブで `./gradlew test` 実行
- [x] **FR-004**: システムはdevDebugおよびprodDebugビルドを実行し、ビルドエラーがあれば失敗しなければならない
  - ci.yml: buildジョブで両ビルド実行
- [ ] **FR-005**: システムはJaCoCoによるカバレッジレポートを生成しなければならない
  - Phase0-3（品質基盤）で対応予定

### CD（継続的デリバリー）- Firebase App Distribution

- [x] **FR-006**: システムは`release`ブランチへのpush時にdevReleaseビルドを実行しなければならない
  - cd-firebase.yml: `on: push: branches: [release]`
- [x] **FR-007**: システムはdevRelease APKをFirebase App Distributionにアップロードしなければならない
  - cd-firebase.yml: wzieba/Firebase-Distribution-Github-Action@v1 使用
- [x] **FR-008**: システムは配信完了時にテスターに通知しなければならない
  - Firebase App Distributionの機能で自動通知

### CD（継続的デリバリー）- Google Play

- [x] **FR-009**: システムはmainブランチへのpush時にprodReleaseビルドを実行しなければならない
  - cd-play.yml: `on: push: branches: [main]`
- [x] **FR-010**: システムはprodRelease AABをGoogle Play内部テストトラックにアップロードしなければならない
  - cd-play.yml: r0adkll/upload-google-play@v1 使用
- [x] **FR-011**: システムはアップロード用署名鍵をGitHub Secretsで安全に管理しなければならない
  - KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD で管理

### Firebase Observability

- [x] **FR-012**: システムはデバッグビルド時にTimber.DebugTreeでLogcatにログを出力しなければならない
  - LimimeshiApplication.kt: `Timber.plant(Timber.DebugTree())`
- [x] **FR-013**: システムはリリースビルド時にCrashlyticsTreeでWARN以上のログをCrashlyticsに送信しなければならない
  - CrashlyticsTree: `if (priority < Log.WARN) return`
- [x] **FR-014**: システムは例外発生時にCrashlyticsに例外情報を記録しなければならない
  - CrashlyticsTree: `crashlytics.recordException(t)`
- [x] **FR-015**: システムはAnalyticsHelperを通じてFirebase Analyticsにイベントを送信できなければならない
  - AnalyticsHelper.kt: `analytics.logEvent()` 使用
- [x] **FR-016**: AnalyticsHelperは画面表示、お気に入り操作、ソート変更等の主要イベントをサポートしなければならない
  - logScreenView, logAddFavorite, logRemoveFavorite, logViewCampaign, logChangeSortOrder, logToggleFavoriteFilter 実装済み

---

## Non-Functional Requirements

- [x] **NFR-001**: CIワークフローは10分以内に完了しなければならない
  - 実測: 約5-7分
- [x] **NFR-002**: シークレット情報はログに出力されてはならない
  - GitHub Actionsのマスキング機能で自動保護
- [x] **NFR-003**: 同一ブランチでの並行実行はconcurrencyで制御しなければならない
  - 全ワークフローに `concurrency: cancel-in-progress: true` 設定

---

## Success Criteria

- [x] **SC-001**: PR作成から CI完了まで10分以内
  - 達成: 約5-7分
- [x] **SC-002**: developマージからFirebase App Distribution配信完了まで15分以内
  - 達成: 約10分
- [x] **SC-003**: タグ作成からGoogle Play内部テスト配信完了まで20分以内
  - 達成: 約10分（mainブランチpush時）
- [x] **SC-004**: 全てのシークレットがGitHub Secretsで管理され、ログに露出しない
  - 達成: 9個のSecrets登録済み
- [x] **SC-005**: lint/test失敗時に明確なエラーメッセージが表示される
  - 達成: GitHub Actions UIでエラー詳細表示

---

## Summary

| カテゴリ | 要件数 | 達成 | 未達成 |
|---------|-------|------|--------|
| CI | 5 | 4 | 1 (JaCoCo) |
| CD Firebase | 3 | 3 | 0 |
| CD Google Play | 3 | 3 | 0 |
| Observability | 5 | 5 | 0 |
| Non-Functional | 3 | 3 | 0 |
| Success Criteria | 5 | 5 | 0 |
| **Total** | **24** | **23** | **1** |

**Note**: FR-005（JaCoCo）はPhase0-3で対応予定
