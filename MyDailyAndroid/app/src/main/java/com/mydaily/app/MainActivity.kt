package com.my.daily

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pendingBackupContent: String? = null
    private var pendingBackupName: String = "my-daily-backup.json"

    companion object {
        private const val REQUEST_PICK_BACKUP = 2101
        private const val REQUEST_CREATE_BACKUP = 2102
    }

    private inner class AndroidBridge {
        @JavascriptInterface
        fun saveBackup(fileName: String, content: String) {
            runOnUiThread {
                pendingBackupContent = content
                pendingBackupName = fileName.ifBlank { "my-daily-backup.json" }

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, pendingBackupName)
                }

                try {
                    startActivityForResult(intent, REQUEST_CREATE_BACKUP)
                } catch (error: Exception) {
                    pendingBackupContent = null
                    Toast.makeText(this@MainActivity, "无法打开文件保存窗口", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @Suppress("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Do not draw the WebView underneath Android system bars.
        // The OS reserves the status/navigation-bar areas, so the app header
        // starts cleanly below the notification/status bar on different phones.
        val systemBackground = android.graphics.Color.rgb(243, 245, 251)
        window.statusBarColor = systemBackground
        window.navigationBarColor = systemBackground
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = FrameLayout(this).apply {
            setBackgroundColor(systemBackground)
        }

        webView = WebView(this)
        webView.setBackgroundColor(android.graphics.Color.rgb(243, 245, 251))
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/json", "text/plain"))
                }

                return try {
                    startActivityForResult(intent, REQUEST_PICK_BACKUP)
                    true
                } catch (error: Exception) {
                    filePathCallback?.onReceiveValue(null)
                    filePathCallback = null
                    Toast.makeText(this@MainActivity, "无法打开文件选择窗口", Toast.LENGTH_LONG).show()
                    false
                }
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        root.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // The root view already fits the system windows; no manual inset margins.
        setContentView(root)
        root.requestApplyInsets()
        webView.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        when (requestCode) {
            REQUEST_PICK_BACKUP -> {
                val callback = filePathCallback
                filePathCallback = null
                val uri = if (resultCode == RESULT_OK) resultData?.data else null
                callback?.onReceiveValue(if (uri != null) arrayOf(uri) else null)
            }

            REQUEST_CREATE_BACKUP -> {
                val uri = if (resultCode == RESULT_OK) resultData?.data else null
                val content = pendingBackupContent
                pendingBackupContent = null

                if (uri != null && content != null) {
                    try {
                        contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                            it.write(content)
                        } ?: throw IllegalStateException("Cannot open selected file")
                        Toast.makeText(this, "备份文件已保存", Toast.LENGTH_SHORT).show()
                    } catch (error: Exception) {
                        Toast.makeText(this, "保存失败，请重新尝试", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.removeJavascriptInterface("AndroidBridge")
            webView.destroy()
        }
        super.onDestroy()
    }
}
