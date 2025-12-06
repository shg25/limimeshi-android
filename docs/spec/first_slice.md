# First Vertical Slice: Limited Menu List → Detail → Favorite

本サンプルアプリにおける最初の縦割り機能実装

UI → Presentation → Domain → Data → DB/APIまで、ひとつの通しフローを構築する

# 1. 機能概要

- 期間限定メニューの一覧を表示する
- メニューをタップすると詳細画面へ遷移する
- ハートボタンで「お気に入り登録／解除」ができる
- 一覧画面でもお気に入り状態を反映する
- オフライン時はローカルキャッシュ（Room）から読み込む
- 一覧データは一定間隔で同期（後続でWorkManager追加）

# 2. ユースケース

## UC-01：メニュー一覧を表示する

1. アプリ起動時、一覧画面を表示
2. ローカルキャッシュを即座に表示（Room）
3. バックグラウンドでAPI取得 → diff更新
4. UIに更新結果を反映
5. ローディング／エラーも表示する

→ **Flow / Repository / Room / Retrofit / UiStateのすべてを使う**

## UC-02：メニュー詳細を表示する

1. メニューをタップすると詳細画面へ遷移
2. 詳細情報をdomainモデルで表示
3. お気に入り状態を反映

→ **Navigation Compose / argument / SavedStateHandleの利用**

## UC-03：お気に入り登録/解除

1. 詳細画面と一覧画面の両方からお気に入り切り替え可能
2. Roomに反映され、Flow経由で一覧UIも更新される
3. お気に入り一覧（後続機能）で利用

→ **Repository → Room → Flow → UIの一方向更新を見せられる**

# 3. 画面仕様

### 一覧画面
- カード表示（画像・タイトル・店舗名・日付）
- ハートアイコンでお気に入り状態表示
- ローディング・エラーUI
- Pull-to-refresh（余裕があれば）

### 詳細画面
- 画像
- 説明
- 有効期間
- お気に入りボタン

# 4. ViewModelの責務

### ListViewModel

- UI state（loading / success / error）
- お気に入り状態の更新
- 初回ロードと再取得
- Flowの購読とcombine

### DetailViewModel

- SavedStateHandleのIDを利用
- Repositoryからデータ取得
- お気に入り切り替え

# 5. Domain（UseCases）

- `GetLimitedMenuListUseCase`
- `GetLimitedMenuDetailUseCase`
- `ToggleFavoriteUseCase`

単純なビジネスルールでも「UseCase層がある」ことが重要

# 6. Repository

```
LimitedMenuRepository {
  fun getList(): Flow<List<LimitedMenu>>
  fun getDetail(id: String): Flow<LimitedMenu?>
  suspend fun toggleFavorite(id: String)
}
```

内部構造（core:data）：

- RemoteDataSource（Retrofit）
- LocalDataSource（Room）
- Merge（最新優先・offline firstの基礎）

# 7. DataStore（後続）

お気に入り同期やソート条件の保存などの用途のため、利用余地を残す

# 8. テスト

### ListViewModelTest

- 初期ロードでLoading → Successに遷移する
- RepositoryのFlowがemitされたらUI stateが更新される
- エラー時はError stateを返す

### UseCaseTest

- FakeRepositoryを使ったシンプルな検証

# 9. このスライスで証明できる技術

- Compose × MVVM × Clean Architecture
- Flowによる非同期UI更新
- Room + Retrofitのオフライン戦略
- SavedStateHandleの活用
- Repositoryパターン
- 単体テストの追加
- Vertical Sliceの構築力
