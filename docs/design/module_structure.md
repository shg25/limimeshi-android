# Module Structure

limimeshi-androidは、高い拡張性と責務分離を実現するため、マルチモジュール構成を採用する

Android Developers推奨のアーキテクチャおよびClean Architectureの原則に沿って、以下の構成とする

# 1. モジュール階層の全体像

```
app/
core/
designsystem/
model/
domain/
data/
feature/
<feature-name>/
```

目的と思想は以下

- **app**
  - エントリポイント
  - DI・Navigation・依存定義のみ
  - ロジックは持たない

- **core**
  - 横断的なロジック・モデル・デザイン要素
  - 全featureから参照される共通基盤

- **feature**
  - 1画面または1機能を閉じた単位で構成
  - Vertical Sliceアプローチで実装

# 2. 各モジュール詳細

## 2.1 appモジュール

### 役割
- アプリのエントリポイント（Application / Activity）
- HiltのDIセットアップ
- Navigationグラフの定義
- Executionの接着剤としての役割のみを持つ

### 含めないもの
- ビジネスロジック
- データアクセス
- 状態管理
- UIロジック

すべてfeature / coreに分離する

## 2.2 coreモジュール群

### ● core:designsystem/

UIコンポーネント・テーマ・カラー・Typographyをまとめるモジュール

含む要素
- Material3のテーマ拡張
- 共通コンポーネント（Button / Dialog / AppBar / Loading / Error UIなど）
- スタイル共通化

### ● core:model/

アプリ全体で利用される**純粋なデータモデル**を定義する

- domain model（非フレームワーク依存）
- DTOではなくアプリ内部の「意味構造」を表す

※ dataモジュールのDTOはここには置かない

### ● core:domain/

ドメインロジック（UseCase）をまとめる

- `interface`で定義されるRepositoryに依存
- Compose / Android Frameworkに一切依存しない
- 単体テストの主戦場になる

例：
```
GetLimitedMenuListUseCase
ToggleFavoriteUseCase
GetMenuStatisticsUseCase
```

### ● core:data/

Repository実装をまとめる

- Retrofit / Room / DataStoreを扱う
- DTO・Entityを保持
- domainのrepository interfaceを実装

構造
```
repository/
datasource/
remote/
local/
mapper/
```

## 2.3 featureモジュール

featureは「1機能 = 1モジュール」とし、Vertical Slice（UI → VM → UseCase → Repository）で構成する

例
```
feature/
list/
detail/
analytics/
favorites/
```

featureモジュールの内部構造
```
ui/
screen/
components/
navigation/
presentation/
ListViewModel.kt
domain/
（必要ならUseCaseラッパー）
data/
（feature固有の簡易Repository / データ処理）
```

- 基本ロジックはcoreに寄せる
- ただしfeature専用の補助的ロジックはfeature内に閉じてよい

# 3. 依存関係ルール（重要）

矢印が依存方向
```
app
↓
feature:* → core:designsystem
↘
core:domain ← core:data
↑
core:model
```

原則
- **domainは何にも依存しない**
- **dataはdomainにだけ依存**
- **featureはcoreと必要最小限の依存のみ**
- **appはすべてのfeatureを束ねるだけ**

# 4. 今後追加する可能性のあるモジュール

- `core:common`
  - 汎用ユーティリティ、Result型、拡張関数

- `core:analytics`
  - Firebaseイベントを共通化する場合

- `feature:settings`
  - DataStoreをUIとつなげる場

- `feature:sync`（WorkManager）
  - バックグラウンド同期を単独機能に切り出す

# 5. この構成のメリット

- スケールさせやすい（機能追加が容易）
- 依存関係が明確でレビューしやすい
- テスト可能性が高い（domain/dataのisolation）
- AIコーディングとの相性が良い
  - Claude Codeに「このモジュール内だけコード生成させる」ような依頼が可能
- 応募先企業に見せた時に、**「プロダクトレベルのモジュール設計ができる」**と判断されやすい
