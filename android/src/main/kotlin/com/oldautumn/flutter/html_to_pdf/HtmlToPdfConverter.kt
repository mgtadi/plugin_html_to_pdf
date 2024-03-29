package com.oldautumn.flutter.html_to_pdf

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.print.PdfPrinter
import android.print.PrintAttributes
import android.webkit.WebView
import android.webkit.WebViewClient

import java.io.File


class HtmlToPdfConverter {

    interface Callback {
        fun onSuccess(filePath: String)
        fun onFailure()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun convert(
        filePath: String,
        applicationContext: Context,
        printSize: String,
        orientation: String,
        callback: Callback
    ) {
        val webView = WebView(applicationContext)
        val htmlContent = File(filePath).readText(Charsets.UTF_8)
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.allowFileAccess = true
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                createPdfFromWebView(webView, applicationContext, printSize, orientation, callback)
            }
        }
    }

    fun createPdfFromWebView(
        webView: WebView,
        applicationContext: Context,
        printSize: String,
        orientation: String,
        callback: Callback
    ) {
        val path = applicationContext.filesDir
        var mediaSize = PrintAttributes.MediaSize.ISO_A4

        when (printSize) {
            "A0" -> mediaSize = PrintAttributes.MediaSize.ISO_A0
            "A1" -> mediaSize = PrintAttributes.MediaSize.ISO_A1
            "A2" -> mediaSize = PrintAttributes.MediaSize.ISO_A2
            "A3" -> mediaSize = PrintAttributes.MediaSize.ISO_A3
            "A4" -> mediaSize = PrintAttributes.MediaSize.ISO_A4
            "A5" -> mediaSize = PrintAttributes.MediaSize.ISO_A5
            "A6" -> mediaSize = PrintAttributes.MediaSize.ISO_A6
            "A7" -> mediaSize = PrintAttributes.MediaSize.ISO_A7
            "A8" -> mediaSize = PrintAttributes.MediaSize.ISO_A8
            "A9" -> mediaSize = PrintAttributes.MediaSize.ISO_A9
            "A10" -> mediaSize = PrintAttributes.MediaSize.ISO_A10
        }

        when (orientation) {
            "LANDSCAPE" -> mediaSize = mediaSize.asLandscape()
            "PORTRAIT" -> mediaSize = mediaSize.asPortrait()
        }
        var margins = PrintAttributes.Margins(
                            Double.valueOf(5 * 1000.0).intValue(),
                            Double.valueOf(5 * 1000.0 / 72.0).intValue(),
                            Double.valueOf(5 * 1000.0 / 72.0).intValue(),
                            Double.valueOf(5 * 1000.0 / 72.0).intValue());
        if (printSize == "mm80") {
            mediaSize = PrintAttributes.MediaSize("flutter_printing", "Provided size",
                        Double.valueOf(227 * 1000.0 / 72.0).intValue(),
                        Double.valueOf(105 * 1000.0 / 72.0).intValue());
        }
        
        val attributes = PrintAttributes.Builder()
            .setMediaSize(mediaSize)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(margins).build()

        val printer = PdfPrinter(attributes)
        var adapter = webView.createPrintDocumentAdapter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            adapter = webView.createPrintDocumentAdapter(temporaryDocumentName)
        }
        printer.print(adapter, path, temporaryFileName, object : PdfPrinter.Callback {
            override fun onSuccess(filePath: String) {
                callback.onSuccess(filePath)
            }

            override fun onFailure() {
                callback.onFailure()
            }
        })

    }

    companion object {
        const val temporaryDocumentName = "TemporaryDocumentName"
        const val temporaryFileName = "TemporaryDocumentFile.pdf"
    }
}
