package com.example.utrend

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.web_activity.*

class WebActivity : AppCompatActivity() {
    companion object {
        const val MAX_PROGRESS = 100
    }

    private lateinit var pageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_activity)

        val videoTitle = intent.getStringExtra(CustomViewHolder.VIDEO_TITLE)

        supportActionBar?.title = videoTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        pageUrl = intent.getStringExtra(CustomViewHolder.PAGE_URL)
            ?: throw IllegalStateException("failed PAGE_URL missing in Intent")

        initWebview()
        setWebClient()
        loadUrl(pageUrl)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebview() {
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun loadUrl(pageUrl: String) {
        webView.loadUrl(pageUrl)
    }

    private fun setWebClient() {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress < MAX_PROGRESS && progressBar.visibility == ProgressBar.GONE) {
                    progressBar.visibility = ProgressBar.VISIBLE
                }

                if (newProgress == MAX_PROGRESS) {
                    progressBar.visibility = ProgressBar.GONE
                }
            }
        }
    }
}