# ガバナンスルール

> **Note**: このファイルはlimimeshi-docsで管理。編集は [limimeshi-docs/template/setup-new-repo/docs/governance/README.md](https://github.com/shg25/limimeshi-docs/blob/main/template/setup-new-repo/docs/governance/README.md) で行い、`/sync-shared-rules` で同期

プロジェクト全体で共有するルール・スタイルガイド

## このディレクトリについて

このディレクトリのファイルは **limimeshi-docs/shared/** へのシンボリックリンク。
limimeshi-docsを更新すれば全リポジトリに自動反映される。

## ファイル一覧

| ファイル | 説明 |
|---------|------|
| `docs-style-guide.md` | ドキュメント記述ルール（句読点、文体、表記等） |
| `shared-rules.md` | 複数リポジトリ共通ルール（コミット規約等） |

## docs-style-guide.md

ドキュメント作成時に従うべきスタイルガイド

主なルール：
- **句読点**: 箇条書き・説明文では「。」を使わない
- **文体**: 「〜します」ではなく「〜する」
- **サービス名**: タイトル・正式表記は「期間限定めし（リミメシ）」
- **強調表記**: サービス名、技術用語の初出、重要なルールのみ
- **コロン**: 全角「：」を使用

## shared-rules.md

複数リポジトリで共通のルール

主な内容：
- コミットメッセージ規約（Conventional Commits）
- バージョニング（Semantic Versioning）
- CHANGELOG記載ルール（Keep a Changelog）

## 注意事項

- ファイルを直接編集しない（シンボリックリンクのため編集はlimimeshi-docsに反映される）
- ルールの変更はlimimeshi-docsリポジトリで行う
- limimeshi-docsが兄弟ディレクトリにない場合、シンボリックリンクが壊れる

## 関連

- [limimeshi-docs/shared/](https://github.com/shg25/limimeshi-docs/tree/main/shared) - マスターファイル
- [limimeshi-docs/docs/governance/](https://github.com/shg25/limimeshi-docs/tree/main/docs/governance) - 参照用
