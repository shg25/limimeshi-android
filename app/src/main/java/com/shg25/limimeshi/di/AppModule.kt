package com.shg25.limimeshi.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * アプリケーション全体で使用する依存関係を提供するモジュール
 *
 * 使用例（MVP実装時）:
 * - FirebaseFirestore
 * - FirebaseAuth
 * - Repository
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // MVP実装時に依存関係を追加
    // 例:
    // @Provides
    // @Singleton
    // fun provideFirebaseFirestore(): FirebaseFirestore {
    //     return FirebaseFirestore.getInstance()
    // }
}
