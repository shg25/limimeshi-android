# Architecture Decision Records（ADR）

limimeshi-android固有の技術選定記録を格納

## ADRとは

Architecture Decision Records（ADR）は、アーキテクチャに関する重要な決定とその理由を記録するドキュメント

### 特徴

- **Context**: なぜこの決定が必要か
- **Decision**: 何を選択したか
- **Consequences**: この決定がもたらす影響（良い面も悪い面も）

**出典**: Michael Nygard "[Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)" (2011)

## limimeshi-docsとの関係

### ADR配置方針

ADRは影響範囲に応じて配置場所を分ける

| 配置場所 | 対象 | 例 |
|----------|------|-----|
| limimeshi-docs/adr/ | 複数リポジトリに影響する決定 | Firebase選定、マルチリポジトリ構成 |
| limimeshi-android/docs/adr/ | Android固有の決定 | Jetpack Compose選定、アーキテクチャパターン |

### 共通ADR

複数リポジトリに影響するADRは [limimeshi-docs/adr/](https://github.com/shg25/limimeshi-docs/tree/main/adr) を参照

主な共通ADR:
- ADR-001: Use Firebase for backend
- ADR-002: Adopt multi-repository structure
- ADR-004: Use manual data entry for Phase 2
- ADR-005: Deploy using Firebase Hosting multi-site

### 番号の独立性

- limimeshi-docs/adr/ の連番とこのリポジトリの連番は独立
- 例: limimeshi-docs/adr/005-xxx.md と docs/adr/001-xxx.md は共存可能

## 作成済み（Android固有ADR）

（未作成）

## 命名規則

`NNN-short-title.md`形式で命名

**例**: `001-use-jetpack-compose-for-ui.md`

### タイトル命名規則

**形式**: Present tense imperative verb phrase（現在形の命令形動詞句）

**典型的なパターン**:
- `Use [technology] for [purpose]`
- `Adopt [approach]`
- `Choose [option]`

**出典**: [architecture-decision-record](https://github.com/joelparkerhenderson/architecture-decision-record) (Joel Parker Henderson)
