package com.shg25.limimeshi.core.ui.component

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private const val TAG = "XPostEmbed"
private const val DEFAULT_HEIGHT_DP = 300
private const val MIN_HEIGHT_DP = 100

/**
 * X (Twitter) Post 埋め込みコンポーネント
 *
 * @param xPostUrl X Post URL (null または空の場合は何も表示しない)
 * @param modifier Modifier
 */
@Composable
fun XPostEmbed(
    xPostUrl: String?,
    modifier: Modifier = Modifier
) {
    // URLがnullまたは空の場合は何も表示しない
    if (xPostUrl.isNullOrBlank()) return

    val tweetId = extractTweetId(xPostUrl)
    if (tweetId == null) {
        Log.w(TAG, "Could not extract tweet ID from URL: $xPostUrl")
        return
    }

    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var contentHeightPx by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val heightDp = remember(contentHeightPx) {
        if (contentHeightPx > 0) {
            with(density) { contentHeightPx.toDp() }
        } else {
            DEFAULT_HEIGHT_DP.dp
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp),
        contentAlignment = Alignment.Center
    ) {
        if (hasError) {
            Text(
                text = "投稿を読み込めませんでした",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            XPostWebView(
                tweetId = tweetId,
                onLoadingStateChanged = { loading -> isLoading = loading },
                onError = { hasError = true },
                onContentHeightChanged = { heightPx ->
                    if (heightPx > with(density) { MIN_HEIGHT_DP.dp.toPx() }.toInt()) {
                        contentHeightPx = heightPx
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * Tweet IDを抽出
 * 対応形式:
 * - https://x.com/username/status/123456789
 * - https://twitter.com/username/status/123456789
 */
private fun extractTweetId(url: String): String? {
    val regex = """(?:twitter\.com|x\.com)/\w+/status/(\d+)""".toRegex()
    return regex.find(url)?.groupValues?.getOrNull(1)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun XPostWebView(
    tweetId: String,
    onLoadingStateChanged: (Boolean) -> Unit,
    onError: () -> Unit,
    onContentHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val embedUrl = "https://platform.twitter.com/embed/Tweet.html?id=$tweetId&dnt=true&theme=light"

    Log.d(TAG, "Loading embed URL: $embedUrl")

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    allowContentAccess = true
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
                }

                // JavaScript Interface for height communication
                addJavascriptInterface(
                    HeightInterface(onContentHeightChanged),
                    "AndroidInterface"
                )

                Log.d(TAG, "WebView created")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "Page finished loading: $url")
                        onLoadingStateChanged(false)

                        // Get content height after a short delay to allow rendering
                        view?.postDelayed({
                            view.evaluateJavascript(
                                """
                                (function() {
                                    var height = document.body.scrollHeight;
                                    AndroidInterface.onHeightChanged(height);
                                    return height;
                                })();
                                """.trimIndent()
                            ) { result ->
                                Log.d(TAG, "Content height from JS: $result")
                            }
                        }, 500)
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        Log.d(TAG, "Page started loading: $url")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        Log.e(TAG, "WebView error: ${error?.description} for ${request?.url}")
                        if (request?.isForMainFrame == true) {
                            onError()
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d(TAG, "Console: ${consoleMessage?.message()}")
                        return super.onConsoleMessage(consoleMessage)
                    }
                }

                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { webView ->
            webView.loadUrl(embedUrl)
        },
        modifier = modifier
    )
}

/**
 * JavaScript Interface for receiving height from WebView
 */
private class HeightInterface(
    private val onHeightChanged: (Int) -> Unit
) {
    @JavascriptInterface
    fun onHeightChanged(height: Int) {
        Log.d(TAG, "Height received from JS: $height")
        onHeightChanged.invoke(height)
    }
}
