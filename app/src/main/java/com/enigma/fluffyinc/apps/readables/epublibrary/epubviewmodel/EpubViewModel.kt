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

data class EpubReaderUiState(
    val isLoading: Boolean = false,
    val isGridView: Boolean = true,
    val epubFiles: List<EpubFile> = emptyList(),
    val currentEpub: EpubContent? = null,
    val currentChapterIndex: Int = 0,
    val hasDirectoryAccess: Boolean = false
)

class EpubReaderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EpubReaderUiState())
    val uiState = _uiState.asStateFlow()

    private val _errorChannel = Channel<String>()
    val errorFlow = _errorChannel.receiveAsFlow()

    fun setDirectoryAccess(hasAccess: Boolean) {
        _uiState.update { it.copy(hasDirectoryAccess = hasAccess) }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun scanForEpubFiles(directoryUri: Uri, context: Context) {
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

    // Assuming EpubFile and other functions are defined elsewhere:
// data class EpubFile(val uri: Uri, val title: String, val author: String, val coverImage: ByteArray?, val fileName: String)
// fun extractEpubMetadata(inputStream: InputStream): Pair<String, String> { ... }
// fun extractCoverImageFromUri(uri: Uri, context: Context): ByteArray? { ... }

    /**
     * Recursively scans a directory for .epub files on a background thread.
     *
     * @param directory The starting DocumentFile directory to scan.
     * @param epubList The mutable list to add found EpubFile objects to.
     * @param context The application context to use for content resolving.
     */
    private suspend fun scanDirectoryRecursively(
        directory: DocumentFile,
        epubList: MutableList<EpubFile>,
        context: Context
    ) {
        // Switch to the IO dispatcher for safe file operations.
        // This is crucial to avoid blocking the main thread.
        withContext(Dispatchers.IO) {
            try {
                // Use a 'for' loop which allows suspend function calls inside it.
                for (file in directory.listFiles()) {
                    when {
                        // Case 1: The item is a file ending with .epub
                        file.isFile && file.name?.endsWith(".epub", ignoreCase = true) == true -> {
                            // Use the contentResolver to safely open an InputStream.
                            context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                                // Extract metadata and cover image (assuming these are defined elsewhere).
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
                        // Case 2: The item is a directory
                        file.isDirectory -> {
                            // Safely make the recursive call. Because this is inside a suspend function
                            // and a 'for' loop, this is now allowed.
                            scanDirectoryRecursively(file, epubList, context)
                        }
                    }
                }
            } catch (e: Exception) {
                // It's good practice to log errors during file operations.
                e.printStackTrace()
            }
        }
    }

    fun openEpub(epubFile: EpubFile, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                context.contentResolver.openInputStream(epubFile.uri)?.use { inputStream ->
                    val content:EpubContent = parseEpubContent(inputStream)
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

                // First pass: extract all files and find OPF
                ZipInputStream(ByteArrayInputStream(zipData)).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            fileContents[entry.name] = zip.readBytes()
                        }
                        entry = zip.nextEntry
                    }
                }

                // Find OPF and extract cover reference
                val opfEntry = fileContents.entries.find { it.key.endsWith(".opf", ignoreCase = true) }
                if (opfEntry != null) {
                    opfPath = opfEntry.key.substringBeforeLast('/', "")
                    val doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(ByteArrayInputStream(opfEntry.value))
                    doc.documentElement.normalize()

                    // Find cover ID from metadata
                    var coverId: String? = null
                    val metaElements = doc.getElementsByTagName("meta")
                    for (i in 0 until metaElements.length) {
                        val meta = metaElements.item(i) as Element
                        if (meta.getAttribute("name") == "cover") {
                            coverId = meta.getAttribute("content")
                            break
                        }
                    }

                    // Find cover href from manifest
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

                // Extract the cover image
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

            // Extract all files
            ZipInputStream(ByteArrayInputStream(zipData)).use { zipStream ->
                var entry = zipStream.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        fileContents[entry.name] = zipStream.readBytes()
                    }
                    entry = zipStream.nextEntry
                }
            }

            // Find and parse OPF file
            val opfFileEntry = fileContents.entries.find { it.key.endsWith(".opf", ignoreCase = true) }
            if (opfFileEntry != null) {
                val opfDoc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(ByteArrayInputStream(opfFileEntry.value))
                opfDoc.documentElement.normalize()

                // Get book title
                val titleElements = opfDoc.getElementsByTagName("dc:title")
                if (titleElements.length > 0) {
                    bookTitle = titleElements.item(0).textContent.trim()
                }

                // Build manifest map (id -> href)
                val manifestItems = mutableMapOf<String, String>()
                val itemElements = opfDoc.getElementsByTagName("item")
                for (i in 0 until itemElements.length) {
                    val item = itemElements.item(i) as Element
                    manifestItems[item.getAttribute("id")] = item.getAttribute("href")
                }

                // Get spine order
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
                // Fallback: find HTML files
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
            // Clear error by sending empty string
            _errorChannel.send("")
        }
    }
}
