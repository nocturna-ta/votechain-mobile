//package com.nocturna.votechain.ui.activities
//
//import android.annotation.SuppressLint
//import android.os.Bundle
//import android.util.Log
//import android.view.MenuItem
//import android.view.View
//import android.webkit.WebChromeClient
//import android.webkit.WebResourceRequest
//import android.webkit.WebSettings
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import android.widget.ProgressBar
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.nocturna.votechain.R
//
///**
// * Activity for displaying authenticated PDF documents in a WebView
// * This approach ensures proper handling of authorization headers
// */
//class PDFWebViewActivity : AppCompatActivity() {
//
//    private lateinit var webView: WebView
//    private lateinit var progressBar: ProgressBar
//    private var pdfUrl: String? = null
//    private var authToken: String? = null
//
//    @SuppressLint("SetJavaScriptEnabled")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_pdf_webview)
//
//        // Get URL and auth token from intent
//        pdfUrl = intent.getStringExtra("PDF_URL")
//        authToken = intent.getStringExtra("AUTH_TOKEN")
//
//        if (pdfUrl == null) {
//            Toast.makeText(this, "PDF URL not provided", Toast.LENGTH_SHORT).show()
//            finish()
//            return
//        }
//
//        // Set up action bar with back button
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//            title = "Program Documentation"
//        }
//
//        // Initialize views
//        webView = findViewById(R.id.pdfWebView)
//        progressBar = findViewById(R.id.progressBar)
//
//        // Configure WebView
//        webView.settings.apply {
//            javaScriptEnabled = true
//            builtInZoomControls = true
//            displayZoomControls = false
//            loadWithOverviewMode = true
//            useWideViewPort = true
//            cacheMode = WebSettings.LOAD_NO_CACHE
//            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//        }
//
//        // Set up WebViewClient for intercepting requests and adding headers
//        webView.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                return false
//            }
//
//            override fun onPageFinished(view: WebView?, url: String?) {
//                progressBar.visibility = View.GONE
//                super.onPageFinished(view, url)
//            }
//        }
//
//        // Set up WebChromeClient for progress tracking
//        webView.webChromeClient = object : WebChromeClient() {
//            override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                progressBar.progress = newProgress
//                if (newProgress == 100) {
//                    progressBar.visibility = View.GONE
//                }
//                super.onProgressChanged(view, newProgress)
//            }
//        }
//
//        loadPDF()
//    }
//
//    private fun loadPDF() {
//        progressBar.visibility = View.VISIBLE
//
//        try {
//            // There are two approaches for displaying PDFs with authorization
//
//            // Approach 1: Use Google PDF viewer with the PDF URL
//            // This works for public PDFs or when the auth token is part of the URL
//            // val googleDocsUrl = "https://docs.google.com/viewer?url=${URLEncoder.encode(pdfUrl, "UTF-8")}&embedded=true"
//            // webView.loadUrl(googleDocsUrl)
//
//            // Approach 2: Load the PDF directly with custom headers
//            // This is better for authenticated PDFs as we can add the auth header
//            if (authToken != null) {
//                val headers = mapOf("Authorization" to authToken!!)
//                webView.loadUrl(pdfUrl!!, headers)
//                Log.d("PDFWebViewActivity", "Loading PDF with auth headers")
//            } else {
//                webView.loadUrl(pdfUrl!!)
//                Log.d("PDFWebViewActivity", "Loading PDF without auth headers")
//            }
//        } catch (e: Exception) {
//            Log.e("PDFWebViewActivity", "Error loading PDF: ${e.message}", e)
//            Toast.makeText(this, "Error loading PDF: ${e.message}", Toast.LENGTH_SHORT).show()
//            progressBar.visibility = View.GONE
//        }
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == android.R.id.home) {
//            finish()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    override fun onBackPressed() {
//        if (webView.canGoBack()) {
//            webView.goBack()
//        } else {
//            super.onBackPressed()
//        }
//    }
//}
