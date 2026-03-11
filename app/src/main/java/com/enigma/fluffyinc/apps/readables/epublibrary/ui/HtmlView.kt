package com.enigma.fluffyinc.apps.readables.epublibrary.ui




import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlView(htmlContent: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                // Optional: For better appearance matching the app theme
                setBackgroundColor(0x00000000) // Transparent
            }
        },
        update = { webView ->
            // Injects CSS to style the HTML content.
            // This example sets a text color and background that respects the system theme.
            // You can expand this to include font size, margins, etc.
            val styledHtml = """
                <html>
                    <head>
                        <style>
                            body {
                                color: #cccccc; /* Light gray text for dark themes */
                                background-color: #121212; /* Dark background */
                                padding: 8px;
                                font-family: sans-serif;
                                line-height: 1.6;
                            }
                            @media (prefers-color-scheme: light) {
                                body {
                                    color: #333333; /* Dark text for light themes */
                                    background-color: #ffffff; /* White background */
                                }
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