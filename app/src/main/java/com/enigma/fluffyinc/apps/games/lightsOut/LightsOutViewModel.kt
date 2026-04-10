package com.enigma.fluffyinc.apps.games.lightsOut

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LightsOutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LightsOutGameState())
    val uiState: StateFlow<LightsOutGameState> = _uiState.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        newGame(LightsOutDifficulty.MEDIUM)
    }

    fun newGame(difficulty: LightsOutDifficulty) {
        viewModelScope.launch {
            _isGenerating.value = true
            val puzzle = withContext(Dispatchers.Default) {
                generatePuzzle(difficulty)
            }
            _uiState.value = LightsOutGameState(
                cells = puzzle.board,
                solution = puzzle.solution,
                difficulty = difficulty
            )
            _isGenerating.value = false
        }
    }

    fun onCellPressed(index: Int) {
        _uiState.update { pressButton(it, index) }
    }

    fun toggleHint() {
        _uiState.update { it.copy(showHint = !it.showHint) }
    }
}
