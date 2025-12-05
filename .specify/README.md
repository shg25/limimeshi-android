# GitHub Spec Kit（仕様駆動開発）

> **Note**: このファイルはlimimeshi-docsで管理。編集は [limimeshi-docs/template/setup-new-repo/.specify/README.md](https://github.com/shg25/limimeshi-docs/blob/main/template/setup-new-repo/.specify/README.md) で行い、`/sync-shared-rules` で同期

仕様書からコードを生成する開発手法を支援するディレクトリ

## Spec Kitとは

**Spec-Driven Development**（仕様駆動開発）を実践するためのフレームワーク

- 仕様が王様、コードは従者
- 仕様書を先に書き、それに基づいてコードを生成
- 仕様と実装のギャップをゼロにする

**参考**: [GitHub Spec Kit](https://github.com/github/spec-kit)

## ディレクトリ構成

```
.specify/
├── memory/
│   └── constitution.md    # プロジェクトの憲法（実体、カスタマイズ可）
├── specs/                 # 機能仕様書（実体、リポジトリ固有）
│   └── NNN-feature-name/
│       ├── spec.md        # 仕様書本体
│       ├── plan.md        # 実装計画
│       ├── tasks.md       # タスク一覧
│       └── checklist.md   # 完了チェックリスト
├── templates/             # テンプレート（→ limimeshi-docs/shared/）
└── .claude/commands/      # speckit-*コマンド（→ limimeshi-docs/shared/）
```

## 基本ワークフロー

### 1. 仕様作成

```
/speckit-specify [機能の説明文]
```

例：
```
/speckit-specify ユーザーがお気に入りのチェーン店を保存できる機能
```

AIが説明文から：
- 短い機能名を生成（例: `favorites`）
- 連番付きディレクトリを作成（例: `specs/003-favorites/`）
- **ほぼ完成形のspec.mdを自動生成**
- 不明点は最大3つまで`[NEEDS CLARIFICATION]`マーカーを付与

### 2. 不明点の解消（必要な場合のみ）

```
/speckit-clarify 003-favorites
```

`[NEEDS CLARIFICATION]`マーカーが残っている場合のみ実行。
選択肢形式で質問され、回答するとspec.mdが更新される。

### 3. 仕様分析（オプション）

```
/speckit-analyze 003-favorites
```

仕様の整合性チェックや追加の検討が必要な場合に実行

### 4. 実装計画

```
/speckit-plan 003-favorites
```

`plan.md`と`tasks.md`を作成

### 5. 実装

```
/speckit-implement 003-favorites
```

タスクに従って実装を進める

### 6. 完了確認

```
/speckit-checklist 003-favorites
```

`checklist.md`で完了状態を確認

## コマンド一覧

| コマンド | 引数 | 説明 |
|---------|------|------|
| `/speckit-specify` | 機能の説明文 | 説明文から仕様書を自動生成 |
| `/speckit-clarify` | 機能名 | 不明点を選択肢形式で解消 |
| `/speckit-analyze` | 機能名 | 仕様の整合性を分析 |
| `/speckit-plan` | 機能名 | 実装計画とタスク一覧を作成 |
| `/speckit-tasks` | 機能名 | タスク一覧を更新 |
| `/speckit-implement` | 機能名 | タスクに従って実装 |
| `/speckit-checklist` | 機能名 | 完了チェックリストを作成・確認 |
| `/speckit-constitution` | - | constitution.mdを参照・更新 |

## 仕様書の命名規則

`NNN-feature-name`形式で命名
- **NNN**: 連番（001, 002, 003...）
- **feature-name**: 機能名（kebab-case）

例: `001-admin-panel`, `002-chain-list`, `003-favorites`

## constitution.md

プロジェクトの憲法。全ての実装・設計はこれに準拠する。

主な内容：
- 開発原則（Test-First、Firebase-First等）
- 技術選定方針
- 品質基準
- 禁止事項

## 関連

- [GitHub Spec Kit](https://github.com/github/spec-kit)
- [limimeshi-docs](https://github.com/shg25/limimeshi-docs)
