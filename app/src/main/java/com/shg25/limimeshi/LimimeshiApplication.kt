package com.shg25.limimeshi

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class LimimeshiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            // デバッグビルド: Logcatに出力
            Timber.plant(Timber.DebugTree())
        } else {
            // リリースビルド: Crashlyticsにログを送信
            Timber.plant(CrashlyticsTree())
        }
    }
}

/**
 * リリースビルド用のTimber.Tree
 * WARN以上のログをCrashlyticsに送信
 */
class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // WARN以上のログのみCrashlyticsに送信
        if (priority < android.util.Log.WARN) return

        val crashlytics = FirebaseCrashlytics.getInstance()

        // タグがあれば付与
        tag?.let { crashlytics.setCustomKey("tag", it) }

        // ログメッセージを記録
        crashlytics.log(message)

        // 例外があれば記録
        t?.let { crashlytics.recordException(it) }
    }
}
