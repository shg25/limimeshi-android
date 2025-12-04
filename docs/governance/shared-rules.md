# 複数リポジトリ共通ルール

全リポジトリで適用する共通チェックルール

---

## 1. 公開リポジトリ安全性チェック

### 目的

公開リポジトリに機密情報をアップロードしないことを確認する

### チェック対象

#### 絶対にコミットしてはいけないもの

| カテゴリ | 例 |
|----------|-----|
| APIキー・シークレット | `FIREBASE_API_KEY`、`AWS_SECRET_ACCESS_KEY`、`OPENAI_API_KEY` |
| 認証情報 | パスワード、トークン、秘密鍵（`.pem`、`.key`） |
| 環境変数ファイル | `.env`、`.env.local`、`.env.production` |
| Firebase設定（本番） | `serviceAccountKey.json`、`firebase-adminsdk-*.json` |
| 個人情報 | メールアドレスリスト、ユーザーデータ |

#### 注意が必要なもの

| カテゴリ | 対応 |
|----------|------|
| Firebase設定（公開可） | `firebaseConfig`オブジェクトは公開可（クライアントサイド用） |
| テストデータ | 本番データを含まないことを確認 |
| ログファイル | 機密情報を含まないことを確認 |

### チェック方法

1. コミット前に `git diff --staged` で変更内容を確認
2. 以下のパターンがないか確認：
   - `API_KEY`、`SECRET`、`PASSWORD`、`TOKEN` を含む文字列
   - Base64エンコードされた長い文字列
   - `-----BEGIN PRIVATE KEY-----` などの証明書ヘッダー

### .gitignoreに含めるべきファイル

```gitignore
# 環境変数
.env
.env.local
.env.*.local

# Firebase
**/serviceAccountKey.json
**/firebase-adminsdk-*.json

# IDE
.idea/
.vscode/settings.json

# OS
.DS_Store
Thumbs.db

# ログ
*.log
npm-debug.log*

# 依存関係
node_modules/
```

---

## 2. ドキュメントスタイルガイド準拠

### 目的

全リポジトリでドキュメントの記述スタイルを統一する

### 参照先

**マスター**: [governance/docs-style-guide.md](./docs-style-guide.md)

### 主要ルール（抜粋）

#### 句読点

- 箇条書き・説明文では「。」を使わない
- 「。」は注意書き・免責事項のみ

#### 文体

- 「〜します」ではなく「〜する」
- 体言止めを積極的に使用

#### サービス名表記

- タイトル・正式表記：「期間限定めし（リミメシ）」
- 文中：「リミメシ」も許容

#### Phase表記

- スペースなし：`Phase0`、`Phase1`、`Phase2`
- コロンは全角：`Phase1：設計`

#### 強調表記（太字）

使用する場面：
- サービス名の初出
- 技術用語の初出
- 重要なルール・警告

使用しない場面：
- 一般的な説明文
- 既出の用語

### チェック方法

1. 新規ドキュメント作成時、docs-style-guide.mdを参照
2. 以下を確認：
   - [ ] 箇条書きに「。」がない
   - [ ] Phase表記がスペースなし
   - [ ] 全角コロン「：」を使用
   - [ ] 太字の使用が適切

---

## 3. コミットメッセージ規約

### 形式

```
<type>: <subject>

[optional body]

[optional footer]
```

### type一覧

| type | 用途 |
|------|------|
| `feat` | 新機能 |
| `fix` | バグ修正 |
| `docs` | ドキュメントのみの変更 |
| `style` | コードの意味に影響しない変更（空白、フォーマット等） |
| `refactor` | バグ修正でも機能追加でもないコード変更 |
| `test` | テストの追加・修正 |
| `chore` | ビルドプロセスやツールの変更 |

### 例

```
feat: チェーン店一覧にソート機能を追加

- 新着順とふりがな順の切り替えを実装
- ユーザー設定を永続化

Closes #123
```

---

## 4. 変更履歴（CHANGELOG）運用

### 採用規約

| 規約 | 用途 | 公式リンク |
|------|------|-----------|
| **Keep a Changelog** | 変更履歴の記録形式 | [公式](https://keepachangelog.com/ja/1.1.0/) |
| **Conventional Commits** | コミットメッセージ規約 | [公式](https://www.conventionalcommits.org/ja/v1.0.0/) |
| **Semantic Versioning** | バージョン番号体系 | [公式](https://semver.org/lang/ja/) |

### CHANGELOG.md形式

```markdown
# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]
### Added
- 新機能の説明

## [1.0.0] - 2025-XX-XX
### Added
- 初期リリース機能
### Changed
- 変更内容
### Fixed
- バグ修正
```

### カテゴリ

| カテゴリ | 用途 |
|---------|------|
| `Added` | 新機能 |
| `Changed` | 既存機能の変更 |
| `Deprecated` | 将来削除予定の機能 |
| `Removed` | 削除された機能 |
| `Fixed` | バグ修正 |
| `Security` | セキュリティ修正 |

### 運用ルール

- 変更は `[Unreleased]` セクションに随時追記
- リリース時に `[Unreleased]` → `[X.X.X] - YYYY-MM-DD` に変換
- バージョン番号はSemantic Versioningに従う
  - MAJOR：後方互換性のない変更
  - MINOR：後方互換性のある機能追加
  - PATCH：後方互換性のあるバグ修正

---

## 5. 適用方法

### 各リポジトリでの参照

各リポジトリのCLAUDE.mdに以下を追記：

```markdown
## 共通ルール

本プロジェクトは以下の共通ルールに従う：

**参照**: [limimeshi-docs/governance/shared-rules.md](https://github.com/shg25/limimeshi-docs/blob/main/governance/shared-rules.md)

- 公開リポジトリ安全性チェック
- ドキュメントスタイルガイド準拠
- コミットメッセージ規約
- 変更履歴（CHANGELOG）運用
```

### Claude Code Hooks / Agent Skills 連携

このルールは以下のClaude Code機能と連携予定：

- **Agent Skills**: `security-check.md`、`style-guide-check.md`
- **Claude Code Hooks**: PostToolUseで自動チェック

---

**Version**: 1.0.0 | **Created**: 2025/12/03
