package com.enigma.fluffyinc.apps.games.lexicon.data

import android.content.Context
import com.enigma.fluffyinc.R
import com.enigma.fluffyinc.apps.games.processors.OptimizedWordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WordLibraryUpdate(
    val version: Int,
    val downloadUrl: String,
    val releaseDate: String
)

class WordDictionaryManager(private val context: Context) {

    private val _dictionary = MutableStateFlow<Set<String>>(emptySet())
    val dictionary: StateFlow<Set<String>> = _dictionary

    private val _largeDictionary = MutableStateFlow<Set<String>>(emptySet())
    val largeDictionary: StateFlow<Set<String>> = _largeDictionary

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val wordRepository = OptimizedWordRepository(context)

    suspend fun load() {
        _isLoading.value = true
        _error.value = null
        try {
            val words = withContext(Dispatchers.IO) {
                wordRepository.getAllWords().toSet()
            }
            _dictionary.value = words
        } catch (e: Exception) {
            android.util.Log.e("WordDictionaryManager", "Failed to load dictionary", e)
            _error.value = "Failed to load dictionary: ${e.localizedMessage}"
            _dictionary.value = emptySet()
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadLargeDictionary() {
        _isLoading.value = true
        _error.value = null
        try {
            val words = withContext(Dispatchers.IO) {
                wordRepository
                    .getAllWordsLargeLibrary()
                    .toSet()
            }
            _largeDictionary.value = words
        } catch (e: Exception) {
            android.util.Log.e("WordDictionaryManager", "Failed to load dictionary", e)
            _error.value = "Failed to load dictionary: ${e.localizedMessage}"
            _largeDictionary.value = emptySet()
        } finally {
            _isLoading.value = false
        }
    }

    fun isWordValid(word: String) = _dictionary.value.contains(word.uppercase())
    fun getWordCount() = _dictionary.value.size
}
