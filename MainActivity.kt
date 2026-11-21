package com.coocaa.customlauncher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")
        webView.loadUrl("file:///android_asset/index.html")
    }

    override fun onBackPressed() {
        // Mencegah tombol Back keluar dari launcher utama
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val jsKey = when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> "ArrowUp"
                KeyEvent.KEYCODE_DPAD_DOWN -> "ArrowDown"
                KeyEvent.KEYCODE_DPAD_LEFT -> "ArrowLeft"
                KeyEvent.KEYCODE_DPAD_RIGHT -> "ArrowRight"
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> "Enter"
                else -> null
            }
            if (jsKey != null) {
                webView.evaluateJavascript(
                    "document.dispatchEvent(new KeyboardEvent('keydown', {key: '$jsKey'}));",
                    null
                )
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun launchApp(packageName: String) {
            runOnUiThread {
                val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(packageName)
                    ?: packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(this@MainActivity, "Aplikasi belum terpasang: $packageName", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @JavascriptInterface
        fun openSystem(action: String) {
            runOnUiThread {
                try {
                    when (action) {
                        "wifi" -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                        "settings" -> startActivity(Intent(Settings.ACTION_SETTINGS))
                        "input" -> {
                            val inputIntent = packageManager.getLaunchIntentForPackage("com.mediatek.tvinput")
                            if (inputIntent != null) startActivity(inputIntent)
                            else startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    }
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }
    }
}