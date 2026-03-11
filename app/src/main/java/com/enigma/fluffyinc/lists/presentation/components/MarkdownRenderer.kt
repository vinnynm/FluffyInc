package com.notesapp.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Lightweight Markdown renderer supporting:
 *  # H1  ## H2  ### H3
 *  **bold**  *italic*  ~~strikethrough~~  `code`
 *  - bullet lists
 *  > blockquotes
 *  --- horizontal rule (rendered as visual divider)
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    baseColor: Color = Color.Unspecified
) {
    val lines = markdown.split("\n")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            when {
                line.startsWith("### ") -> MarkdownLine(line.removePrefix("### "), isBold = true, fontSize = 16, baseColor = baseColor)
                line.startsWith("## ") -> MarkdownLine(line.removePrefix("## "), isBold = true, fontSize = 20, baseColor = baseColor)
                line.startsWith("# ") -> MarkdownLine(line.removePrefix("# "), isBold = true, fontSize = 26, baseColor = baseColor)
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row(modifier = Modifier.padding(start = 8.dp)) {
                        BasicText("• ", style = TextStyle(fontSize = 14.sp, color = if (baseColor != Color.Unspecified) baseColor else Color.DarkGray))
                        MarkdownLine(line.drop(2), baseColor = baseColor)
                    }
                }
                line.startsWith("> ") -> {
                    Row(modifier = Modifier.padding(start = 4.dp)) {
                        Box(modifier = Modifier.width(4.dp).height(20.dp)
                            .padding(end = 0.dp)
                            .also { })
                        Spacer(Modifier.width(8.dp))
                        MarkdownLine(line.removePrefix("> "), baseColor = Color.Gray, italicAll = true)
                    }
                }
                line.trim() == "---" -> {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 4.dp))
                }
                else -> MarkdownLine(line, baseColor = baseColor)
            }
        }
    }
}

@Composable
fun MarkdownLine(
    text: String,
    isBold: Boolean = false,
    fontSize: Int = 14,
    baseColor: Color = Color.Unspecified,
    italicAll: Boolean = false
) {
    val annotated = buildAnnotatedMarkdown(text, isBold, fontSize, baseColor, italicAll)
    BasicText(
        text = annotated,
        style = TextStyle(fontSize = fontSize.sp)
    )
}

fun buildAnnotatedMarkdown(
    text: String,
    forceBold: Boolean = false,
    fontSize: Int = 14,
    baseColor: Color = Color.Unspecified,
    forceItalic: Boolean = false
): AnnotatedString = buildAnnotatedString {
    val defaultColor = if (baseColor != Color.Unspecified) baseColor else Color.Unspecified
    var i = 0
    while (i < text.length) {
        when {
            // Bold+Italic ***text***
            text.startsWith("***", i) -> {
                val end = text.indexOf("***", i + 3)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = defaultColor)) {
                        append(text.substring(i + 3, end))
                    }
                    i = end + 3
                } else { append(text[i]); i++ }
            }
            // Bold **text**
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else { append(text[i]); i++ }
            }
            // Italic *text*
            text.startsWith("*", i) && !text.startsWith("**", i) -> {
                val end = text.indexOf("*", i + 1)
                if (end != -1 && !text.startsWith("**", end)) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            // Strikethrough ~~text~~
            text.startsWith("~~", i) -> {
                val end = text.indexOf("~~", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = Color.Gray)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else { append(text[i]); i++ }
            }
            // Inline code `text`
            text.startsWith("`", i) -> {
                val end = text.indexOf("`", i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        background = Color(0xFFEEEEEE),
                        color = Color(0xFFE53935),
                        fontSize = (fontSize - 1).sp
                    )) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            else -> {
                withStyle(SpanStyle(
                    fontWeight = if (forceBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (forceItalic) FontStyle.Italic else FontStyle.Normal,
                    color = defaultColor
                )) { append(text[i]) }
                i++
            }
        }
    }
}
