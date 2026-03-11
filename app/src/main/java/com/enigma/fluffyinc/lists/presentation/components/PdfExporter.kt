package com.enigma.fluffyinc.lists.presentation.components

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.enigma.fluffyinc.lists.domain.model.Checklist
import com.enigma.fluffyinc.lists.domain.model.ChecklistItem
import com.enigma.fluffyinc.lists.domain.model.DiaryEntry
import com.enigma.fluffyinc.lists.domain.model.JournalEntry
import com.enigma.fluffyinc.lists.domain.model.ListItem
import com.enigma.fluffyinc.lists.domain.model.Note
import com.enigma.fluffyinc.lists.domain.model.SimpleList
import com.enigma.fluffyinc.lists.domain.model.TopicNode
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

private const val PAGE_WIDTH = 595   // A4 at 72dpi
private const val PAGE_HEIGHT = 842
private const val MARGIN = 48f
private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2

object PdfExporter {

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = Color.BLACK
    }
    private val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 16f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = Color.DKGRAY
    }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f; color = Color.BLACK
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f; color = Color.GRAY
    }
    private val linePaint = Paint().apply {
        color = Color.LTGRAY; strokeWidth = 1f
    }
    private val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f; color = Color.parseColor("#4CAF50")
    }

    // ─── Note export ───────────────────────────────────────────────────────────

    fun exportNote(context: Context, note: Note, topics: List<TopicNode>): Uri {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val doc = PdfDocument()
        val state = PageState(doc)

        state.newPage()
        state.drawText(note.title, titlePaint)
        state.y += 4f
        state.drawText("Note · Last updated: ${fmt.format(Date(note.updatedAt))}", subPaint)
        state.y += 4f
        state.drawLine()
        state.y += 12f

        fun drawTopics(nodes: List<TopicNode>, depth: Int = 0) {
            nodes.forEach { node ->
                val indent = depth * 20f
                val paint = if (depth == 0) headingPaint else bodyPaint
                state.drawTextIndented("${if (depth > 0) "›  " else ""}${node.title}", paint, indent)
                if (node.content.isNotBlank()) {
                    state.drawTextIndented(node.content, subPaint, indent + 12f, wrapWidth = (CONTENT_WIDTH - indent - 12f).toInt())
                }
                state.y += 6f
                drawTopics(node.children, depth + 1)
            }
        }
        drawTopics(topics)

        state.finish()
        return saveAndShare(context, doc, "note_${note.id.take(8)}.pdf")
    }

    // ─── Checklist export ─────────────────────────────────────────────────────

    fun exportChecklist(context: Context, checklist: Checklist, items: List<ChecklistItem>): Uri {
        val doc = PdfDocument()
        val state = PageState(doc)
        state.newPage()
        state.drawText(checklist.title, titlePaint)
        state.y += 4f
        val done = items.count { it.isChecked }
        state.drawText("Checklist · $done / ${items.size} completed", subPaint)
        state.y += 4f
        state.drawLine()
        state.y += 12f

        items.forEach { item ->
            val bullet = if (item.isChecked) "☑  " else "☐  "
            val paint = if (item.isChecked) {
                Paint(bodyPaint).also { it.color = Color.GRAY }
            } else bodyPaint
            state.drawText("$bullet${item.text}", paint)
        }

        state.finish()
        return saveAndShare(context, doc, "checklist_${checklist.id.take(8)}.pdf")
    }

    // ─── Simple List export ───────────────────────────────────────────────────

    fun exportSimpleList(context: Context, list: SimpleList, items: List<ListItem>): Uri {
        val doc = PdfDocument()
        val state = PageState(doc)
        state.newPage()
        state.drawText(list.title, titlePaint)
        state.y += 4f
        state.drawText("List · ${items.size} items", subPaint)
        state.y += 4f
        state.drawLine()
        state.y += 12f

        items.forEachIndexed { idx, item ->
            state.drawText("${idx + 1}.   ${item.text}", bodyPaint)
        }

        state.finish()
        return saveAndShare(context, doc, "list_${list.id.take(8)}.pdf")
    }

    // ─── Diary export ─────────────────────────────────────────────────────────

    fun exportDiaryEntry(context: Context, entry: DiaryEntry): Uri {
        val fmt = SimpleDateFormat("EEEE, MMMM d yyyy · HH:mm", Locale.getDefault())
        val doc = PdfDocument()
        val state = PageState(doc)
        state.newPage()
        state.drawText(entry.title, titlePaint)
        state.y += 4f
        state.drawText("Event: ${fmt.format(Date(entry.eventDate))}", subPaint)
        state.y += 4f
        state.drawLine()
        state.y += 12f
        state.drawWrappedText(entry.content, bodyPaint)

        state.finish()
        return saveAndShare(context, doc, "diary_${entry.id.take(8)}.pdf")
    }

    // ─── Journal export ───────────────────────────────────────────────────────

    fun exportJournalEntry(context: Context, entry: JournalEntry): Uri {
        val fmt = SimpleDateFormat("EEEE, MMMM d yyyy", Locale.getDefault())
        val doc = PdfDocument()
        val state = PageState(doc)

        val emotionColorInt = entry.emotion.color.toInt()
        val bgPaint = Paint().apply { color = blendWithWhite(emotionColorInt, 0.08f); style = Paint.Style.FILL }

        state.newPage()
        // Colored header banner
        state.canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 80f, bgPaint)
        state.canvas.drawText("${entry.emotion.icon}  ${entry.title}", MARGIN, 48f, titlePaint)
        state.canvas.drawText("${entry.emotion.label}  ·  ${fmt.format(Date(entry.entryDate))}", MARGIN, 68f, subPaint)
        state.y = 96f
        state.drawLine()
        state.y += 16f
        state.drawWrappedText(entry.content, bodyPaint)

        state.finish()
        return saveAndShare(context, doc, "journal_${entry.id.take(8)}.pdf")
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun blendWithWhite(color: Int, alpha: Float): Int {
        val r = (Color.red(color) * alpha + 255 * (1 - alpha)).toInt()
        val g = (Color.green(color) * alpha + 255 * (1 - alpha)).toInt()
        val b = (Color.blue(color) * alpha + 255 * (1 - alpha)).toInt()
        return Color.rgb(r, g, b)
    }

    private fun saveAndShare(context: Context, doc: PdfDocument, filename: String): Uri {
        val dir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val file = File(dir, filename)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private class PageState(val doc: PdfDocument) {
        private var pageInfo: PdfDocument.PageInfo? = null
        private var page: PdfDocument.Page? = null
        var canvas: Canvas = Canvas()
        var y: Float = MARGIN
        private var pageNum = 1

        fun newPage() {
            page?.let { doc.finishPage(it) }
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum++).create()
            page = doc.startPage(pageInfo)
            canvas = page!!.canvas
            canvas.drawColor(Color.WHITE)
            y = MARGIN
        }

        private fun ensureSpace(needed: Float) {
            if (y + needed > PAGE_HEIGHT - MARGIN) newPage()
        }

        fun drawText(text: String, paint: Paint) {
            ensureSpace(paint.textSize + 6f)
            canvas.drawText(text, MARGIN, y + paint.textSize, paint)
            y += paint.textSize + 6f
        }

        fun drawTextIndented(text: String, paint: Paint, indent: Float, wrapWidth: Int = CONTENT_WIDTH.toInt()) {
            val words = text.split(" ")
            val sb = StringBuilder()
            words.forEach { word ->
                val test = if (sb.isEmpty()) word else "$sb $word"
                if (paint.measureText(test) > wrapWidth) {
                    ensureSpace(paint.textSize + 4f)
                    canvas.drawText(sb.toString(), MARGIN + indent, y + paint.textSize, paint)
                    y += paint.textSize + 4f
                    sb.clear(); sb.append(word)
                } else { if (sb.isNotEmpty()) sb.append(" "); sb.append(word) }
            }
            if (sb.isNotEmpty()) {
                ensureSpace(paint.textSize + 4f)
                canvas.drawText(sb.toString(), MARGIN + indent, y + paint.textSize, paint)
                y += paint.textSize + 6f
            }
        }

        fun drawWrappedText(text: String, paint: Paint) {
            text.split("\n").forEach { line ->
                drawTextIndented(line.ifBlank { " " }, paint, 0f)
            }
        }

        fun drawLine() {
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 4f
        }

        fun finish() { page?.let { doc.finishPage(it) } }
    }
}

fun sharePdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, "Open PDF"))
}
