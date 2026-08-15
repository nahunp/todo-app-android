package com.nahunp.todoapp.presentation.auth.register

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nahunp.todoapp.BuildConfig

/**
 * Loads the web repo's frontend/public/mobile-captcha.html in a WebView —
 * there's no Turnstile SDK for Android, so this is the actual verification
 * mechanism, not a placeholder. That page renders the real Turnstile
 * widget using the frontend's already-configured site key and calls back
 * through window.AndroidCaptchaBridge.onToken(token), which is exactly
 * the interface name this file injects — the two are a matched pair, see
 * that HTML file's own comment for the full reasoning (why a WebView
 * instead of a native attestation approach, why this exact bridge name).
 *
 * WebView's JavascriptInterface callbacks run on a background thread, not
 * the main/Compose thread — onToken below has to post back explicitly
 * (webView.post) before touching anything Compose-observable.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TurnstileCaptchaView(onToken: (String) -> Unit) {
    val context = LocalContext.current
    val currentOnToken = rememberUpdatedState(onToken)

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onToken(token: String) {
                            post { currentOnToken.value(token) }
                        }
                    },
                    "AndroidCaptchaBridge",
                )
                loadUrl(BuildConfig.CAPTCHA_PAGE_URL)
            }
        },
    )
}
