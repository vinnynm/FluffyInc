package com.enigma.fluffyinc.apps.readables.epublibrary.epubviewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
    val fontSize: Int = 100,
    val theme: ReaderTheme = ReaderTheme.LIGHT,
    val scannedFolders: Set<String> = emptySet()
)

class EpubReaderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EpubReaderUiState())
    val uiState = _uiState.asStateFlow()

    private val _errorChannel = Channel<String>()
    val errorFlow = _errorChannel.receiveAsFlow()

    private val PREFS_NAME = "epub_reader_prefs"
    private val KEY_SCANNED_FOLDERS = "scanned_folders_set"
    private val KEY_FONT_SIZE = "font_size"
    private val KEY_THEME = "reader_theme"
    private val KEY_VIEW_MODE = "is_grid_view"

    fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Important: Always copy the set from SharedPreferences
        val savedFolders = prefs.getStringSet(KEY_SCANNED_FOLDERS, emptySet())?.toSet() ?: emptySet()
        val savedFontSize = prefs.getInt(KEY_FONT_SIZE, 100)
        val savedThemeName = prefs.getString(KEY_THEME, ReaderTheme.LIGHT.name)
        val savedTheme = try { ReaderTheme.valueOf(savedThemeName!!) } catch (e: Exception) { ReaderTheme.LIGHT }
        val isGridView = prefs.getBoolean(KEY_VIEW_MODE, true)

        _uiState.update { it.copy(
            scannedFolders = savedFolders,
            fontSize = savedFontSize,
            theme = savedTheme,
            isGridView = isGridView
        ) }

        if (savedFolders.isNotEmpty()) {
            scanAllFolders(context)
        }
    }

    private fun saveSetting(context: Context, key: String, value: Any) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Set<*> -> putStringSet(key, value as Set<String>)
                else -> {}
            }
            apply()
        }
    }

    fun addFolderAndScan(uri: Uri, context: Context) {
        val uriString = uri.toString()
        val currentFolders = _uiState.value.scannedFolders.toMutableSet()
        val isNew = currentFolders.add(uriString)
        
        if (isNew) {
            _uiState.update { it.copy(scannedFolders = currentFolders) }
            saveSetting(context, KEY_SCANNED_FOLDERS, currentFolders)
        }
        
        // Always trigger scan when a folder is selected, even if it was already in memory
        scanAllFolders(context)
    }

    fun removeFolder(uriString: String, context: Context) {
        val currentFolders = _uiState.value.scannedFolders.toMutableSet()
        if (currentFolders.remove(uriString)) {
            _uiState.update { it.copy(scannedFolders = currentFolders) }
            saveSetting(context, KEY_SCANNED_FOLDERS, currentFolders)
            scanAllFolders(context)
        }
    }

    fun scanAllFolders(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val allEpubFiles = mutableListOf<EpubFile>()
            val folders = _uiState.value.scannedFolders
            
            if (folders.isEmpty()) {
                _uiState.update { it.copy(epubFiles = emptyList(), isLoading = false) }
                return@launch
            }

            withContext(Dispatchers.IO) {
                folders.forEach { uriString ->
                    try {
                        val uri = Uri.parse(uriString)
                        val documentTree = DocumentFile.fromTreeUri(context, uri)
                        if (documentTree != null && documentTree.exists()) {
                            scanDirectoryRecursively(documentTree, allEpubFiles, context)
                        } else {
                            Log.e("EpubViewModel", "Folder no longer exists or access denied: $uriString")
                        }
                    } catch (e: Exception) {
                        Log.e("EpubViewModel", "Error scanning folder $uriString", e)
                    }
                }
            }

            _uiState.update { it.copy(epubFiles = allEpubFiles, isLoading = false) }
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

    fun toggleViewMode(context: Context) {
        val newMode = !_uiState.value.isGridView
        _uiState.update { it.copy(isGridView = newMode) }
        saveSetting(context, KEY_VIEW_MODE, newMode)
    }

    private suspend fun scanDirectoryRecursively(
        directory: DocumentFile,
        epubList: MutableList<EpubFile>,
        context: Context
    ) {
        val files = directory.listFiles()
        for (file in files) {
            if (file.isFile && file.name?.endsWith(".epub", ignoreCase = true) == true) {
                try {
                    context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                        val metadata = extractEpubMetadata(inputStream)
                        val coverImage = extractCoverImageFromUri(file.uri, context)
                        epubList.add(EpubFile(
                            uri = file.uri,
                            title = metadata.first,
                            author = metadata.second,
                            coverImage = coverImage,
                            fileName = file.name ?: "Unknown"
                        ))
                    }
                } catch (e: Exception) { 
                    Log.e("EpubViewModel", "Failed to parse EPUB: ${file.name}", e)
                }
            } else if (file.isDirectory) {
                scanDirectoryRecursively(file, epubList, context)
            }
        }
    }

    fun openEpub(epubFile: EpubFile, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                context.contentResolver.openInputStream(epubFile.uri)?.use { inputStream ->
                    val content = parseEpubContent(inputStream)
                    _uiState.update { it.copy(
                        currentEpub = content,
                        currentChapterIndex = 0,
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                Log.e("EpubViewModel", "Failed to open EPUB", e)
                _errorChannel.send("Failed to open EPUB")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun closeEpub() {
        _uiState.update { it.copy(currentEpub = null) }
    }

    fun nextChapter() {
        _uiState.update { state ->
            val next = state.currentChapterIndex + 1
            if (next < (state.currentEpub?.chapters?.size ?: 0)) {
                state.copy(currentChapterIndex = next)
            } else state
        }
    }

    fun previousChapter() {
        _uiState.update { state ->
            val prev = state.currentChapterIndex - 1
            if (prev >= 0) {
                state.copy(currentChapterIndex = prev)
            } else state
        }
    }

    fun goToChapter(index: Int) {
        _uiState.update { it.copy(currentChapterIndex = index) }
    }

    private suspend fun extractEpubMetadata(inputStream: InputStream): Pair<String, String> = withContext(Dispatchers.IO) {
        var title = "Unknown Title"
        var author = "Unknown Author"
        try {
            val zipData = inputStream.readBytes()
            ZipInputStream(ByteArrayInputStream(zipData)).use { zipStream ->
                var entry = zipStream.nextEntry
                while (entry != null) {
                    if (entry.name.endsWith(".opf", ignoreCase = true)) {
                        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(zipStream.readBytes()))
                        title = doc.getElementsByTagName("dc:title").item(0)?.textContent?.trim() ?: title
                        author = doc.getElementsByTagName("dc:creator").item(0)?.textContent?.trim() ?: author
                        break
                    }
                    entry = zipStream.nextEntry
                }
            }
        } catch (e: Exception) {}
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
                        if (!entry.isDirectory) fileContents[entry.name] = zip.readBytes()
                        entry = zip.nextEntry
                    }
                }
                val opfEntry = fileContents.entries.find { it.key.endsWith(".opf", ignoreCase = true) }
                if (opfEntry != null) {
                    opfPath = opfEntry.key.substringBeforeLast('/', "")
                    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(opfEntry.value))
                    val items = doc.getElementsByTagName("item")
                    for (i in 0 until items.length) {
                        val item = items.item(i) as Element
                        if (item.getAttribute("properties") == "cover-image" || item.getAttribute("id").contains("cover", true)) {
                            coverHref = item.getAttribute("href")
                            break
                        }
                    }
                }
                coverHref?.let {
                    val fullPath = if (opfPath.isNotEmpty()) "$opfPath/$it" else it
                    fileContents[fullPath]?.let { bytes ->
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    }
                }
            }
        } catch (e: Exception) { null }
    }

    private suspend fun parseEpubContent(inputStream: InputStream): EpubContent = withContext(Dispatchers.IO) {
        val chapters = mutableListOf<Chapter>()
        var bookTitle = "Unknown Book"
        try {
            val zipData = inputStream.readBytes()
            val fileContents = mutableMapOf<String, ByteArray>()
            ZipInputStream(ByteArrayInputStream(zipData)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) fileContents[entry.name] = zip.readBytes()
                    entry = zip.nextEntry
                }
            }
            val opfEntry = fileContents.entries.find { it.key.endsWith(".opf", ignoreCase = true) }
            if (opfEntry != null) {
                val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(opfEntry.value))
                bookTitle = doc.getElementsByTagName("dc:title").item(0)?.textContent?.trim() ?: bookTitle
                val manifest = mutableMapOf<String, String>()
                val items = doc.getElementsByTagName("item")
                for (i in 0 until items.length) {
                    val item = items.item(i) as Element
                    manifest[item.getAttribute("id")] = item.getAttribute("href")
                }
                val spine = doc.getElementsByTagName("itemref")
                val opfPath = opfEntry.key.substringBeforeLast('/', "")
                for (i in 0 until spine.length) {
                    val idref = (spine.item(i) as Element).getAttribute("idref")
                    manifest[idref]?.let { href ->
                        val fullPath = if (opfPath.isNotEmpty()) "$opfPath/$href" else href
                        fileContents[fullPath]?.let { bytes ->
                            val html = bytes.toString(Charsets.UTF_8)
                            chapters.add(Chapter("Chapter ${i+1}", cleanHtmlContent(html), i))
                        }
                    }
                }
            }
        } catch (e: Exception) { Log.e("EpubViewModel", "Error parsing content", e) }
        EpubContent(bookTitle, chapters)
    }

    private fun cleanHtmlContent(html: String): String = html.replace("<?xml[^>]*?>".toRegex(), "").replace("<!DOCTYPE[^>]*>".toRegex(), "").trim()
    
    fun setDirectoryAccess(bool: Boolean) {
        _uiState.update { it.copy(hasDirectoryAccess = bool) }
    }

    fun scanForEpubFiles(uri: Uri, context: Context) {
        addFolderAndScan(uri, context)
    }
    
    fun refreshLibrary(context: Context) {
        scanAllFolders(context)
    }
}
