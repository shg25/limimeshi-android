# Feature Specification: CI/CD Infrastructure

**Feature Branch**: `feature/phase0-ci-cd-firebase`<br>
**Created**: 2025-12-10<br>
**Updated**: 2025-12-10<br>
**Status**: Draft<br>
**Input**: Phase0としてCI/CD基盤を構築。GitHub Actionsによるlint/test/build自動化、Firebase App Distributionへのdev版配信、Google Play内部テストへのprod版配信を実現する。ポートフォリオとしてDevOps理解を示す。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 自動ビルド・テスト (Priority: P1)

開発者がPRを作成した際、自動でlint/test/buildが実行され、品質を担保できる。

**Why this priority**: CI/CDの基本機能。これがないとコード品質の自動チェックができない。

**Independent Test**: PR作成時にGitHub Actionsが起動し、lint/test/buildが成功すれば価値を提供できる。

**Acceptance Scenarios**:

1. **Given** 開発者がPRを作成, **When** GitHub Actionsが起動, **Then** lint → test → buildが順番に実行される
2. **Given** lintエラーがあるコード, **When** CIが実行, **Then** lintステップで失敗しワークフロー全体が失敗扱いになる
3. **Given** テストが失敗するコード, **When** CIが実行, **Then** testステップで失敗しワークフロー全体が失敗扱いになる
4. **Given** PRが作成された, **When** CIが実行, **Then** PRのステータスチェックとして結果が表示される
5. **Given** 全てのステップが成功, **When** CIが完了, **Then** 緑のチェックマークが表示される

---

### User Story 2 - Firebase App Distribution配信 (Priority: P1)

releaseブランチへのpush時、devRelease版APKが自動でFirebase App Distributionに配信され、テスターがすぐにインストールできる。

**Why this priority**: 内部テスト配信の自動化。手動配信の手間を省き、リリース前のテストを効率化する。

**Independent Test**: releaseブランチへのpush後、Firebase App DistributionにAPKがアップロードされ、テスターに通知が届けば価値を提供できる。

**Acceptance Scenarios**:

1. **Given** releaseブランチにpush, **When** CDワークフローが起動, **Then** devReleaseビルドが実行される
2. **Given** devReleaseビルドが成功, **When** 配信ステップが実行, **Then** APKがFirebase App Distributionにアップロードされる
3. **Given** APKがアップロード完了, **When** 配信が成功, **Then** 登録済みテスターにメール通知が届く
4. **Given** テスターが通知を受信, **When** リンクをクリック, **Then** 最新版APKをインストールできる

---

### User Story 3 - Google Play内部テスト配信 (Priority: P2)

mainブランチへのpush時、prodRelease版AABが自動でGoogle Play内部テストトラックに配信される。

**Why this priority**: 本番配信フローの自動化。手動アップロードの手間を省く。ただしFirebase App Distributionより優先度は低い。

**Independent Test**: mainブランチへのpush後、Google Play内部テストトラックにAABがアップロードされ、内部テスターがインストールできれば価値を提供できる。

**Acceptance Scenarios**:

1. **Given** mainブランチにpush（releaseブランチからのマージ）, **When** CDワークフローが起動, **Then** prodReleaseビルドが実行される
2. **Given** prodReleaseビルドが成功, **When** 配信ステップが実行, **Then** AABがGoogle Play内部テストトラックにアップロードされる
3. **Given** AABがアップロード完了, **When** 審査なしで公開, **Then** 内部テスターがPlayストアから最新版をインストールできる
4. **Given** mainブランチでバグ発見, **When** hotfix/*ブランチを作成して修正, **Then** mainにマージ後、自動で配信される

---

### Edge Cases

- **シークレット未設定**: 必要なシークレット（署名鍵、サービスアカウント）が未設定の場合、明確なエラーメッセージで失敗
- **ビルド失敗**: ビルドが失敗した場合、配信ステップはスキップされる
- **並行実行**: 同じブランチで複数のワークフローが同時実行された場合、concurrencyで制御
- **手動トリガー**: 必要に応じてworkflow_dispatchで手動実行も可能

## Requirements *(mandatory)*

### Functional Requirements

#### CI（継続的インテグレーション）
- **FR-001**: システムはPR作成時にワークフローを自動実行しなければならない
- **FR-002**: システムはlint（Android Lint）を実行し、エラーがあれば失敗しなければならない
- **FR-003**: システムは単体テストを実行し、失敗があればワークフロー全体を失敗としなければならない
- **FR-004**: システムはdevDebugおよびprodDebugビルドを実行し、ビルドエラーがあれば失敗しなければならない
- **FR-005**: システムはJaCoCoによるカバレッジレポートを生成しなければならない

#### CD（継続的デリバリー）- Firebase App Distribution
- **FR-006**: システムは`release`ブランチへのpush時にdevReleaseビルドを実行しなければならない
- **FR-007**: システムはdevRelease APKをFirebase App Distributionにアップロードしなければならない
- **FR-008**: システムは配信完了時にテスターに通知しなければならない

#### CD（継続的デリバリー）- Google Play
- **FR-009**: システムはmainブランチへのpush時にprodReleaseビルドを実行しなければならない
- **FR-010**: システムはprodRelease AABをGoogle Play内部テストトラックにアップロードしなければならない
- **FR-011**: システムはアップロード用署名鍵をGitHub Secretsで安全に管理しなければならない

### Non-Functional Requirements

- **NFR-001**: CIワークフローは10分以内に完了しなければならない
- **NFR-002**: シークレット情報はログに出力されてはならない
- **NFR-003**: 同一ブランチでの並行実行はconcurrencyで制御しなければならない

### Key Entities

- **Workflow**: GitHub Actionsのワークフロー定義（.github/workflows/*.yml）
- **Build Variant**: devDebug, devRelease, prodDebug, prodReleaseの4種類
- **Secret**: 署名鍵、Firebaseサービスアカウント、Google Playサービスアカウント

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: PR作成から CI完了まで10分以内
- **SC-002**: developマージからFirebase App Distribution配信完了まで15分以内
- **SC-003**: タグ作成からGoogle Play内部テスト配信完了まで20分以内
- **SC-004**: 全てのシークレットがGitHub Secretsで管理され、ログに露出しない
- **SC-005**: lint/test失敗時に明確なエラーメッセージが表示される

## Assumptions *(optional)*

- GitHub Actionsの無料枠（2000分/月）で運用可能
- Firebase App Distributionは無料枠で運用可能
- Google Play Consoleのデベロッパーアカウントは取得済み
- 署名鍵（upload keystore）は開発者が作成・管理
- Google Playアプリ署名を使用（Googleが署名鍵を管理）

## Out of Scope *(optional)*

以下はPhase0の対象外：

- **本番リリース自動化**: Google Play本番トラックへの自動配信（手動審査・承認が必要）
- **UIテスト自動化**: Espresso/Compose UIテストのCI実行（Phase2以降）
- **パフォーマンステスト**: Firebase Test Labでのパフォーマンス計測
- **セキュリティスキャン**: 依存関係の脆弱性スキャン（将来的に検討）
- **複数環境デプロイ**: staging環境などの追加（dev/prodの2環境のみ）

## Technical Notes

### ワークフロー構成案

```
.github/workflows/
├── ci.yml           # PR/push時のlint/test/build
├── cd-firebase.yml  # developマージ時のFirebase配信
└── cd-play.yml      # タグ作成時のGoogle Play配信
```

### 必要なSecrets

| Secret名 | 用途 |
|----------|------|
| KEYSTORE_BASE64 | 署名鍵（Base64エンコード） |
| KEYSTORE_PASSWORD | キーストアパスワード |
| KEY_ALIAS | 鍵エイリアス |
| KEY_PASSWORD | 鍵パスワード |
| FIREBASE_APP_ID_DEV | Firebase App ID（dev） |
| FIREBASE_SERVICE_ACCOUNT | Firebaseサービスアカウント |
| PLAY_SERVICE_ACCOUNT | Google Playサービスアカウント |
