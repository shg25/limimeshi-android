---
name: security-check
description: |
  公開リポジトリへのコミット前に機密情報が含まれていないかチェックする。
  ファイル編集後やコミット前に自動的に適用。

  チェック対象：
  - APIキー、シークレット、トークン
  - 環境変数ファイル（.env等）
  - Firebase認証情報
  - 個人情報
---

## Security Check Agent Skill

このAgent Skillは、公開リポジトリに機密情報がコミットされることを防ぐ。

### チェック項目

#### 絶対にコミットしてはいけないパターン

1. **APIキー・シークレット**
   - `API_KEY`、`SECRET`、`PASSWORD`、`TOKEN` を含む変数
   - 例：`FIREBASE_API_KEY=xxx`、`AWS_SECRET_ACCESS_KEY=xxx`

2. **認証情報ファイル**
   - `serviceAccountKey.json`
   - `firebase-adminsdk-*.json`
   - `.pem`、`.key` ファイル

3. **環境変数ファイル**
   - `.env`
   - `.env.local`
   - `.env.production`

4. **危険なパターン**
   - `-----BEGIN PRIVATE KEY-----`
   - `-----BEGIN RSA PRIVATE KEY-----`
   - Base64エンコードされた長い文字列（40文字以上の英数字連続）

### 検出時のアクション

機密情報を検出した場合：

1. **即座に警告**を出す
2. 該当ファイル・行を特定して報告
3. `.gitignore` への追加を提案
4. コミット前であれば、変更の取り消しを提案

### 許可されるケース

以下は機密情報ではないため許可：

- `firebaseConfig` オブジェクト（クライアントサイド公開用）
- プレースホルダー値（`YOUR_API_KEY_HERE`、`xxx`、`dummy`）
- ドキュメント内の例示

### 使用タイミング

- ファイル編集完了後
- `git add` 実行前
- コミット作成前
