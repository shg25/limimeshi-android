# ドキュメントディレクトリ

> **Note**: このファイルはlimimeshi-docsで管理。編集は [limimeshi-docs/template/setup-new-repo/docs/README.md](https://github.com/shg25/limimeshi-docs/blob/main/template/setup-new-repo/docs/README.md) で行い、`/sync-shared-rules` で同期

このリポジトリのドキュメントを管理

## ディレクトリ構成

```
docs/
├── README.md           # このファイル
├── roadmap.md          # ロードマップ（実体）
├── CHANGELOG.md        # 変更履歴（実体）
├── adr/                # Architecture Decision Records
│   └── README.md
└── governance/         # ガバナンスルール（→ limimeshi-docs/shared/）
    └── README.md
```

## ファイル説明

### roadmap.md

リポジトリ固有のタスク・進捗を管理

- 現在のフェーズ
- タスク一覧（進行中・完了）
- マイルストーン

プロジェクト全体のロードマップは [limimeshi-docs/docs/roadmap.md](https://github.com/shg25/limimeshi-docs/blob/main/docs/roadmap.md) を参照

### CHANGELOG.md

このリポジトリの変更履歴を記録

**採用規約**:
- [Keep a Changelog](https://keepachangelog.com/ja/1.1.0/) - 変更履歴の記録形式
- [Semantic Versioning](https://semver.org/lang/ja/) - バージョン番号体系

**カテゴリ**: Added, Changed, Deprecated, Removed, Fixed, Security

### adr/

Architecture Decision Records - 技術選定の記録

詳細は [adr/README.md](./adr/README.md) を参照

### governance/

ガバナンスルール - limimeshi-docs/shared/へのシンボリックリンク

詳細は [governance/README.md](./governance/README.md) を参照

## 関連

- [limimeshi-docs](https://github.com/shg25/limimeshi-docs) - ガバナンスリポジトリ
