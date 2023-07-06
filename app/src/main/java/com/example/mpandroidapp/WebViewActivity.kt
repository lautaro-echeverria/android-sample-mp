package com.example.mpandroidapp

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.mparticle.MPEvent
import com.mparticle.MParticle


class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myWebView = WebView(this)
        setContentView(myWebView)
        val webSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        MParticle.getInstance()!!.registerWebView(myWebView,"testWebViewLautaro")
        myWebView.loadUrl("https://webview-sample-mp.netlify.app/")
    }
}