package com.enigma.fluffyinc.apps.readables.epublibrary.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.enigma.fluffyinc.apps.readables.epublibrary.epubviewmodel.ReaderTheme

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlView(
    htmlContent: String,
    fontSize: Int,
    theme: ReaderTheme,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (theme) {
        ReaderTheme.LIGHT -> "#ffffff"
        ReaderTheme.DARK -> "#121212"
        ReaderTheme.SEPIA -> "#f4ecd8"
    }

    val textColor = when (theme) {
        ReaderTheme.LIGHT -> "#000000"
        ReaderTheme.DARK -> "#cccccc"
        ReaderTheme.SEPIA -> "#5b4636"
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                setBackgroundColor(0x00000000) // Transparent
            }
        },
        update = { webView ->
            val styledHtml = """
                <html>
                    <head>
                        <style>
                            body {
                                color: $textColor;
                                background-color: $backgroundColor;
                                padding: 16px;
                                font-family: sans-serif;
                                line-height: 1.6;
                                font-size: ${fontSize}%;
                            }
                            img {
                                max-width: 100%;
                                height: auto;
                            }
                        </style>
                    </head>
                    <body>
                        $htmlContent
                    </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
        }
    )
}
