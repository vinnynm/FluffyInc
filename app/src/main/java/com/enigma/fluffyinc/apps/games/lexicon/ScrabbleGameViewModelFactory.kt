package com.enigma.fluffyinc.apps.games.lexicon

import android.content.Context
import com.enigma.fluffyinc.apps.games.lexicon.data.WordDictionaryManager
import com.enigma.fluffyinc.apps.games.lexicon.ui.ScrabbleGameViewModel


class ScrabbleGameViewModelFactory(
    private val dictionaryManager: WordDictionaryManager,
    private val context: Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScrabbleGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScrabbleGameViewModel(dictionaryManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
