package com.shg25.limimeshi.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber

/**
 * Firebase Analyticsのラッパー
 * アプリ全体で統一されたイベント送信を提供
 */
object AnalyticsHelper {

    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }

    /**
     * 画面表示イベントを送信
     * @param screenName 画面名
     * @param screenClass 画面クラス名（省略可）
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
        Timber.d("Analytics: screen_view - $screenName")
    }

    /**
     * カスタムイベントを送信
     * @param eventName イベント名
     * @param params パラメータ（省略可）
     */
    fun logEvent(eventName: String, params: Bundle? = null) {
        analytics.logEvent(eventName, params)
        Timber.d("Analytics: $eventName")
    }

    /**
     * お気に入り追加イベント
     * @param chainId チェーン店ID
     * @param chainName チェーン店名
     */
    fun logAddFavorite(chainId: String, chainName: String) {
        val params = Bundle().apply {
            putString("chain_id", chainId)
            putString("chain_name", chainName)
        }
        analytics.logEvent("add_favorite", params)
        Timber.d("Analytics: add_favorite - $chainName")
    }

    /**
     * お気に入り解除イベント
     * @param chainId チェーン店ID
     * @param chainName チェーン店名
     */
    fun logRemoveFavorite(chainId: String, chainName: String) {
        val params = Bundle().apply {
            putString("chain_id", chainId)
            putString("chain_name", chainName)
        }
        analytics.logEvent("remove_favorite", params)
        Timber.d("Analytics: remove_favorite - $chainName")
    }

    /**
     * キャンペーン詳細表示イベント
     * @param campaignId キャンペーンID
     * @param campaignName キャンペーン名
     * @param chainName チェーン店名
     */
    fun logViewCampaign(campaignId: String, campaignName: String, chainName: String) {
        val params = Bundle().apply {
            putString("campaign_id", campaignId)
            putString("campaign_name", campaignName)
            putString("chain_name", chainName)
        }
        analytics.logEvent("view_campaign", params)
        Timber.d("Analytics: view_campaign - $campaignName")
    }

    /**
     * ソート順変更イベント
     * @param sortType ソート種別（new/kana）
     */
    fun logChangeSortOrder(sortType: String) {
        val params = Bundle().apply {
            putString("sort_type", sortType)
        }
        analytics.logEvent("change_sort_order", params)
        Timber.d("Analytics: change_sort_order - $sortType")
    }

    /**
     * お気に入りフィルタ切り替えイベント
     * @param enabled 有効/無効
     */
    fun logToggleFavoriteFilter(enabled: Boolean) {
        val params = Bundle().apply {
            putBoolean("enabled", enabled)
        }
        analytics.logEvent("toggle_favorite_filter", params)
        Timber.d("Analytics: toggle_favorite_filter - $enabled")
    }
}
