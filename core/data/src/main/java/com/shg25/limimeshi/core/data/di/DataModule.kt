package com.shg25.limimeshi.core.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    // ChainRepository は @Inject constructor で自動的にHiltに登録される
    // 将来的に追加の設定が必要な場合はここに記述
}
