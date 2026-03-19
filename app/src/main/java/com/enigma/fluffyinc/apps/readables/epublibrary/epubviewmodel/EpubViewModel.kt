package com.enigma.fluffyinc.apps.readables.epublibrary.epubviewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.text.HtmlCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enigma.fluffyinc.apps.readables.epublibrary.data.Chapter
import com.enigma.fluffyinc.apps.readables.epublibrary.data.EpubContent
import com.enigma.fluffyinc.apps.readables.epublibrary.data.EpubFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

enum class ReaderTheme {
    LIGHT, DARK, SEPIA
}

data class EpubReaderUiState(
    val isLoading: Boolean = false,
    val isGridView: Boolean = true,
    val epubFiles: List<EpubFile> = emptyList(),
    val currentEpub: EpubContent? = null,
    val currentChapterIndex: Int = 0,
    val hasDirectoryAccess: Boolean = false,
    val fontSize: Int = 100, // percentage
    val theme: ReaderTheme = ReaderTheme.LIGHT,
    val selectedDirectoryUri: String? = null
)

class EpubReaderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EpubReaderUiState())
    val uiState = _uiState.asStateFlow()

    private val _errorChannel = Channel<String>()
    val errorFlow = _errorChannel.receiveAsFlow()

    private val PREFS_NAME = "epub_reader_prefs"
    private val KEY_DIR_URI = "last_directory_uri"
    private val KEY_FONT_SIZE = "font_size"
    private val KEY_THEME = "reader_theme"

    fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUri = prefs.getString(KEY_DIR_URI, null)
        val savedFontSize = prefs.getInt(KEY_FONT_SIZE, 100)
        val savedThemeName = prefs.getString(KEY_THEME, ReaderTheme.LIGHT.name)
        val savedTheme = try { ReaderTheme.valueOf(savedThemeName!!) } catch (e: Exception) { ReaderTheme.LIGHT }

        _uiState.update { it.copy(
            selectedDirectoryUri = savedUri,
            fontSize = savedFontSize,
            theme = savedTheme
        ) }

        savedUri?.let {
            scanForEpubFiles(Uri.parse(it), context)
        }
    }

    private fun saveSetting(context: Context, key: String, value: Any) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                else -> {}
            }
            apply()
        }
    }

    fun setFontSize(size: Int, context: Context) {
        _uiState.update { it.copy(fontSize = size) }
        saveSetting(context, KEY_FONT_SIZE, size)
    }

    fun setTheme(theme: ReaderTheme, context: Context) {
        _uiState.update { it.copy(theme = theme) }
        saveSetting(context, KEY_THEME, theme.name)
    }

    fun setDirectoryAccess(hasAccess: Boolean) {
        _uiState.update { it.copy(hasDirectoryAccess = hasAccess) }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun scanForEpubFiles(directoryUri: Uri, context: Context) {
        // Save the directory URI for persistence
        _uiState.update { it.copy(selectedDirectoryUri = directoryUri.toString()) }
        saveSetting(context, KEY_DIR_URI, directoryUri.toString())

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val documentTree = DocumentFile.fromTreeUri(context, directoryUri)
                val epubFilesList = mutableListOf<EpubFile>()

                documentTree?.let { tree ->
                    scanDirectoryRecursively(tree, epubFilesList, context)
                }

                _uiState.update { it.copy(epubFiles = epubFilesList, isLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorChannel.send("Failed to scan directory: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun scanDirectoryRecursively(
        directory: DocumentFile,
        epubList: MutableList<EpubFile>,
        context: Context
    ) {
        withContext(Dispatchers.IO) {
            try {
                for (file in directory.listFiles()) {
                    when {
                        file.isFile && file.name?.endsWith(".epub", ignoreCase = true) == true -> {
                            context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                                val metadata = extractEpubMetadata(inputStream)
                                val coverImage = extractCoverImageFromUri(file.uri, context)

                                epubList.add(
                                    EpubFile(
                                        uri = file.uri,
                                        title = metadata.first,
                                        author = metadata.second,
                                        coverImage = coverImage,
                                        fileName = file.name ?: "Unknown"
                                    )
                                )
                            }
                        }
                        file.isDirectory -> {
                            scanDirectoryRecursively(file, epubList, context)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openEpub(epubFile: EpubFile, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                context.contentResolver.openInputStream(epubFile.uri)?.use { inputStream ->
                    val content: EpubContent = parseEpubContent(inputStream)
                    _uiState.update { it.copy(
                        currentEpub = content,
                        currentChapterIndex = 0,
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorChannel.send("Failed to open EPUB: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun closeEpub() {
        _uiState.update { it.copy(currentEpub = null, currentChapterIndex = 0) }
    }

    fun nextChapter() {
        _uiState.value.currentEpub?.let { epub ->
            if (_uiState.value.currentChapterIndex < epub.chapters.size - 1) {
                _uiState.update { it.copy(currentChapterIndex = it.currentChapterIndex + 1) }
            }
        }
    }

    fun previousChapter() {
        if (_uiState.value.currentChapterIndex > 0) {
            _uiState.update { it.copy(currentChapterIndex = it.currentChapterIndex - 1) }
        }
    }

    fun goToChapter(index: Int) {
        _uiState.value.currentEpub?.let { epub ->
            if (index in 0 until epub.chapters.size) {
                _uiState.update { it.copy(currentChapterIndex = index) }
            }
        }
    }

    private suspend fun extractEpubMetadata(inputStream: InputStream): Pair<String, String> = withContext(Dispatchers.IO) {
        var title = "Unknown Title"
        var author = "Unknown Author"

        try {
            val zipData = inputStream.readBytes()
            ZipInputStream(ByteArrayInputStream(zipData)).use { zipStream ->
                var entry = zipStream.nextEntry
                while (entry != null) {
                    if (entry.name.endsWith("content.opf", ignoreCase = true) ||
                        entry.name.endsWith(".opf", ignoreCase = true)) {

                        val opfContent = zipStream.readBytes()
                        val doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(ByteArrayInputStream(opfContent))
                        doc.documentElement.normalize()

                        val titleElements = doc.getElementsByTagName("dc:title")
                        if (titleElements.length > 0) {
                            title = titleElements.item(0).textContent.trim()
                        }

                        val authorElements = doc.getElementsByTagName("dc:creator")
                        if (authorElements.length > 0) {
                            author = authorElements.item(0).textContent.trim()
                        }
                        break
                    }
                    entry = zipStream.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Pair(title, author)
    }

    private suspend fun extractCoverImageFromUri(uri: Uri, context: Context): ImageBitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val zipData = inputStream.readBytes()
                var coverHref: String? = null
                var opfPath = ""
                val fileContents = mutableMapOf<String, ByteArray>()

                ZipInputStream(ByteArrayInputStream(zipData)).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            fileContents[entry.name] = zip.readBytes()
                        }
                        entry = zip.nextEntry
                    }
                }

                val opfEntry = fileContents.entries.find { it.key.endsWith(".opf", ignoreCase = true) }
                if (opfEntry != null) {
                    opfPath = opfEntry.key.substringBeforeLast('/', "")
                    val doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(ByteArrayInputStream(opfEntry.value))
                    doc.documentElement.normalize()

                    var coverId: String? = null
                    val metaElements = doc.getElementsByTagName("meta")
                    for (i in 0 until metaElements.length) {
                        val meta = metaElements.item(i) as Element
                        if (meta.getAttribute("name") == "cover") {
                            coverId = meta.getAttribute("content")
                            break
                        }
                    }

                    val itemElements = doc.getElementsByTagName("item")
                    for (i in 0 until itemElements.length) {
                        val item = itemElements.item(i) as Element
                        val id = item.getAttribute("id")
                        val href = item.getAttribute("href")
                        val mediaType = item.getAttribute("media-type")

                        if ((coverId != null && id == coverId) ||
                            item.getAttribute("properties") == "cover-image" ||
                            (mediaType.startsWith("image/") && (href.contains("cover", ignoreCase = true) || id.contains("cover", ignoreCase = true)))) {
                            coverHref = href
                            break
                        }
                    }
                }

                if (coverHref != null) {
                    val fullPath = if (opfPath.isNotEmpty()) "$opfPath/$coverHref" else coverHref
                    val imageBytes = fileContents[fullPath]
                    if (imageBytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        return@withContext bitmap?.asImageBitmap()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    private suspend fun parseEpubContent(inputStream: InputStream): EpubContent = withContext(Dispatchers.IO) {
        val chapters = mutableListOf<Chapter>()
        var bookTitle = "Unknown Title"

        try {
            val zipData = inputStream.readBytes()
            val fileContents = mutableMapOf<String, ByteArray>()

            ZipInputStream(ByteArrayInputStream(zipData)).use { zipStream ->
                var entry = zipStream.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        fileContents[entry.name] = zipStream.readBytes()
                    }
                    entry = zipStream.nextEntry
                }
            }

            val opfFileEntry = fileContents.entries.find { it.key.endsWith(".opf", ignoreCase = true) }
            if (opfFileEntry != null) {
                val opfDoc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(ByteArrayInputStream(opfFileEntry.value))
                opfDoc.documentElement.normalize()

                val titleElements = opfDoc.getElementsByTagName("dc:title")
                if (titleElements.length > 0) {
                    bookTitle = titleElements.item(0).textContent.trim()
                }

                val manifestItems = mutableMapOf<String, String>()
                val itemElements = opfDoc.getElementsByTagName("item")
                for (i in 0 until itemElements.length) {
                    val item = itemElements.item(i) as Element
                    manifestItems[item.getAttribute("id")] = item.getAttribute("href")
                }

                val spineElements = opfDoc.getElementsByTagName("itemref")
                val opfPath = opfFileEntry.key.substringBeforeLast('/', "")

                for (i in 0 until spineElements.length) {
                    val itemRef = spineElements.item(i) as Element
                    val idref = itemRef.getAttribute("idref")
                    val href = manifestItems[idref]

                    if (href != null) {
                        val fullPath = if (opfPath.isNotEmpty()) "$opfPath/$href" else href
                        val contentBytes = fileContents[fullPath]
                        if (contentBytes != null) {
                            val htmlContent = contentBytes.toString(Charsets.UTF_8)
                            val chapterTitle = extractTitleFromHtml(htmlContent) ?: "Chapter ${i + 1}"
                            chapters.add(Chapter(chapterTitle, cleanHtmlContent(htmlContent), i))
                        }
                    }
                }
            } else {
                fileContents.filter {
                    it.key.endsWith(".html", true) || it.key.endsWith(".xhtml", true)
                }.toList().sortedBy { it.first }.forEachIndexed { index, (_, contentBytes) ->
                    val htmlContent = contentBytes.toString(Charsets.UTF_8)
                    val chapterTitle = extractTitleFromHtml(htmlContent) ?: "Chapter ${index + 1}"
                    chapters.add(Chapter(chapterTitle, cleanHtmlContent(htmlContent), index))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        EpubContent(bookTitle, chapters.sortedBy { it.order })
    }

    private fun extractTitleFromHtml(html: String): String? {
        val titleRegex = "<title[^>]*>(.*?)</title>".toRegex(RegexOption.IGNORE_CASE)
        val h1Regex = "<h1[^>]*>(.*?)</h1>".toRegex(RegexOption.IGNORE_CASE)
        val h2Regex = "<h2[^>]*>(.*?)</h2>".toRegex(RegexOption.IGNORE_CASE)

        titleRegex.find(html)?.groupValues?.get(1)?.let { title ->
            if (title.isNotBlank()) {
                return HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
            }
        }

        h1Regex.find(html)?.groupValues?.get(1)?.let { h1 ->
            if (h1.isNotBlank()) {
                return HtmlCompat.fromHtml(h1, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
            }
        }

        h2Regex.find(html)?.groupValues?.get(1)?.let { h2 ->
            if (h2.isNotBlank()) {
                return HtmlCompat.fromHtml(h2, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
            }
        }

        return null
    }

    private fun cleanHtmlContent(html: String): String {
        return html
            .replace("<?xml[^>]*?>".toRegex(), "")
            .replace("<!DOCTYPE[^>]*>".toRegex(), "")
            .trim()
    }

    fun clearErrorMessage() {
        viewModelScope.launch {
            _errorChannel.send("")
        }
    }
}
