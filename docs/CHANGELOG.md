# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/ja/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/lang/ja/).

## [Unreleased]

### Added
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
