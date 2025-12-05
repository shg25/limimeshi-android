# Architecture Decision Records（ADR）

> **Note**: このファイルはlimimeshi-docsで管理。編集は [limimeshi-docs/template/setup-new-repo/docs/adr/README.md](https://github.com/shg25/limimeshi-docs/blob/main/template/setup-new-repo/docs/adr/README.md) で行い、`/sync-shared-rules` で同期

このリポジトリ固有の技術選定を記録

## ADRとは

Architecture Decision Records（ADR）は、アーキテクチャに関する重要な決定とその理由を記録するドキュメント

**出典**: Michael Nygard "[Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)" (2011)

## 命名規則

`NNN-short-title.md`形式で命名
- **NNN**: 連番（001, 002, 003...）
- **short-title**: 短いタイトル（kebab-case）

**タイトルパターン**:
- `Use [technology] for [purpose]`
- `Adopt [approach]`
- `Choose [option]`

## 共通ADR

複数リポジトリに影響するADRは [limimeshi-docs/docs/adr/](https://github.com/shg25/limimeshi-docs/tree/main/docs/adr) を参照
