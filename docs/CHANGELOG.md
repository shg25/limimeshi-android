# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/ja/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/lang/ja/).

## [Unreleased]

### Changed
- LoginViewModelの認証プロバイダ抽象化（PR #20）
  - AuthRepository（core:data）+ 認証UseCase群（core:domain）に分離
  - AuthUserモデル（core:model）追加でFirebase型のドメイン漏洩を防止
  - GoogleCredentialProviderでCredentialManagerをラップ（テスタビリティ向上）
  - 認証状態の単一ソース化（AuthRepositoryに集約、FavoritesRepositoryから委譲）
  - ログアウト時のローカルキャッシュクリア配線（SignOutUseCase）
- アーキテクチャレビュー指摘4件の修正（PR #19）
- useJUnitPlatform()をConvention Pluginに集約（DRY原則）

### Fixed
- Android Studioの「Clean and Assemble Project with Tests」デッドロック修正（PR #21）
  - build-logicのcomposite buildでclean/testClassesのスケジューラ競合が発生していた
  - build-logic導入時（b4a7f5b）から潜在していた問題

### Added
- 認証関連テスト19件追加（AuthRepository、認証UseCase×3、LoginViewModel、既存UseCase×3）
- お気に入り登録数の表示（003-favorites US2）
  - FavoriteCountコンポーネント（0件非表示、1件以上で「♥ {count}人がお気に入り登録」表示）
  - Optimistic UI（お気に入りトグル時にカウントを即座に±1）
- GitHub Actions CI構築（`.github/workflows/ci.yml`）- lint/test/build
- Firebase App Distribution CD構築（`.github/workflows/cd-firebase.yml`）- releaseブランチトリガー
- Google Play内部テスト CD構築（`.github/workflows/cd-play.yml`）- mainブランチトリガー
- Build Flavors設定（dev/prod）
- 署名設定（release signingConfig）
- versionCode動的生成（`github.run_number`使用）
- Claude Code設定を追加（`.claude/settings.json`、Agent Skills、Slash Commands）
- ガバナンスドキュメントを追加（`docs/governance/`）
- 変更履歴ファイルを追加（`docs/CHANGELOG.md`）
- プロジェクト初期セットアップ（Kotlin + Jetpack Compose）
- GitHub Spec Kit導入（`.specify/`）

### Security
- google-services.jsonをGitHub Secretsで管理（リポジトリから除外）
- 署名鍵・パスワードをGitHub Secretsで管理
- サービスアカウントキーをGitHub Secretsで管理
