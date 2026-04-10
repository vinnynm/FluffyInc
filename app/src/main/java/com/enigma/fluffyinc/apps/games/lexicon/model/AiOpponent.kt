package com.enigma.fluffyinc.apps.games.lexicon.model

// ─────────────────────────────────────────────────────────────────────────────
//  Difficulty
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Controls how well the AI plays.
 *
 * [vocabCoverage]   – fraction of the full word library the AI "knows"
 *                     (randomly sampled once at construction time, so each
 *                     game feels slightly different at the same difficulty).
 * [pickTopN]        – the AI finds all legal moves, then picks randomly from
 *                     the top-N by score.  1 = always best, higher = more
 *                     human-like variance.
 * [exchangeThreshold] – AI exchanges tiles when its best possible score is
 *                     below this value (0 = never exchanges).
 */
enum class AiDifficulty(
    val displayName: String,
    val vocabCoverage: Float,
    val pickTopN: Int,
    val exchangeThreshold: Int
) {
    EASY(
        displayName        = "Easy",
        vocabCoverage      = 0.4f,   // reduced from 0.20
        pickTopN           = 10,       // increased from 8
        exchangeThreshold  = 6        // decreased from 8
    ),
    MEDIUM(
        displayName        = "Medium",
        vocabCoverage      = 0.6f,   // reduced from 0.55
        pickTopN           = 8,        // increased from 4
        exchangeThreshold  = 10       // decreased from 12
    ),
    HARD(
        displayName        = "Hard",
        vocabCoverage      = 0.850f,   // reduced from 0.85
        pickTopN           = 6,        // increased from 2
        exchangeThreshold  = 16       // decreased from 18
    ),
    EXPERT(
        displayName        = "Expert",
        vocabCoverage      = 1.00f,   // full library
        pickTopN           = 1,        // always plays the best scoring move
        exchangeThreshold  = 22
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Move representation
// ─────────────────────────────────────────────────────────────────────────────

data class AiMove(
    val tiles: List<PlacedTile>,   // tiles to place on the board
    val score: Int,
    val word: String,
    val isHorizontal: Boolean
)

sealed class AiDecision {
    data class PlayWord(val move: AiMove) : AiDecision()
    object ExchangeTiles : AiDecision()
    object Skip : AiDecision()
}

// ─────────────────────────────────────────────────────────────────────────────
//  AI Opponent
// ─────────────────────────────────────────────────────────────────────────────

class AiOpponent(
    fullDictionary: Set<String>,
    val difficulty: AiDifficulty
) {

    // ── Vocabulary ──────────────────────────────────────────────────────────

    /**
     * Short "always-legal" words every difficulty level always knows.
     * These are standard 2-letter Scrabble-accepted words that would be in
     * any official word list.
     */
    private val coreWords: Set<String> = setOf(
        // vowel pairs / exclamations
        "AA", "AE", "AI", "OE", "OI", "OU",
        // common 2-letter
        "AB", "AD", "AG", "AH", "AM", "AN", "AR", "AS", "AT", "AW", "AX", "AY",
        "BA", "BE", "BI", "BO", "BY",
        "DA", "DE", "DO",
        "ED", "EF", "EH", "EL", "EM", "EN", "ER", "ES", "ET", "EW", "EX",
        "FA", "FE",
        "GI", "GO",
        "HA", "HE", "HI", "HM", "HO",
        "ID", "IF", "IN", "IS", "IT",
        "JO",
        "KA", "KI",
        "LA", "LI", "LO",
        "MA", "ME", "MI", "MM", "MO", "MU", "MY",
        "NA", "NE", "NO", "NU",
        "OD", "OF", "OH", "OM", "ON", "OP", "OR", "OS", "OW", "OX", "OY",
        "PA", "PE", "PI", "PO",
        "QI",
        "RE",
        "SH", "SI", "SO",
        "TA", "TI", "TO",
        "UH", "UM", "UN", "UP", "UR", "US", "UT",
        "WE", "WO",
        "XI", "XU",
        "YA", "YE", "YO",
        "ZA", "ZO"
    )

    /**
     * The actual vocabulary this AI instance will use.
     * Always includes coreWords; then adds a random sample of the full library
     * proportional to [AiDifficulty.vocabCoverage].
     */
    val vocabulary: Set<String> = buildVocabulary(fullDictionary)

    private fun buildVocabulary(full: Set<String>): Set<String> {
        if (difficulty.vocabCoverage >= 1.0f) return full + coreWords
        val nonCore = full.filter { it !in coreWords }
        val sampleSize = (nonCore.size * difficulty.vocabCoverage).toInt()
        val sampled = nonCore.shuffled().take(sampleSize).toSet()
        return sampled + coreWords
    }

    // ── Letter values (mirrors ScrabbleGame) ───────────────────────────────

    private val letterValues = mapOf(
        'A' to 1, 'B' to 3, 'C' to 3, 'D' to 2, 'E' to 1, 'F' to 4, 'G' to 2,
        'H' to 4, 'I' to 1, 'J' to 8, 'K' to 5, 'L' to 1, 'M' to 3, 'N' to 1,
        'O' to 1, 'P' to 3, 'Q' to 10, 'R' to 1, 'S' to 1, 'T' to 1, 'U' to 1,
        'V' to 4, 'W' to 4, 'X' to 8, 'Y' to 4, 'Z' to 10, '?' to 0
    )

    private val premiumSquares = buildPremiumMap()

    private fun buildPremiumMap(): Map<String, String> {
        val m = mutableMapOf<String, String>()
        listOf(0 to 0, 0 to 7, 0 to 14, 7 to 0, 7 to 14, 14 to 0, 14 to 7, 14 to 14)
            .forEach { m["${it.first},${it.second}"] = "TW" }
        listOf(
            1 to 1, 2 to 2, 3 to 3, 4 to 4, 10 to 10, 11 to 11, 12 to 12, 13 to 13,
            1 to 13, 2 to 12, 3 to 11, 4 to 10, 10 to 4, 11 to 3, 12 to 2, 13 to 1
        ).forEach { m["${it.first},${it.second}"] = "DW" }
        listOf(1 to 5, 1 to 9, 5 to 1, 5 to 5, 5 to 9, 5 to 13, 9 to 1, 9 to 5, 9 to 9, 9 to 13, 13 to 5, 13 to 9)
            .forEach { m["${it.first},${it.second}"] = "TL" }
        listOf(
            0 to 3, 0 to 11, 2 to 6, 2 to 8, 3 to 0, 3 to 7, 3 to 14, 6 to 2, 6 to 6, 6 to 8,
            6 to 12, 7 to 3, 7 to 11, 8 to 2, 8 to 6, 8 to 8, 8 to 12, 11 to 0, 11 to 7,
            11 to 14, 12 to 6, 12 to 8, 14 to 3, 14 to 11
        ).forEach { m["${it.first},${it.second}"] = "DL" }
        return m
    }

    // ── Public entry point ─────────────────────────────────────────────────

    /**
     * Given the current board and the AI's rack, return the best decision.
     * Call this from a coroutine / background thread — it can be CPU-heavy on
     * Expert for a full board.
     */
    fun decideMove(
        board: Array<Array<Tile?>>,
        rack: List<Char>,
        bagSize: Int
    ): AiDecision {
        val boardEmpty = board.all { row -> row.all { it == null } }
        val candidates = findAllMoves(board, rack, boardEmpty)

        if (candidates.isEmpty()) {
            // Nothing to play — exchange if possible, else skip
            return if (bagSize >= 7) AiDecision.ExchangeTiles else AiDecision.Skip
        }

        val sorted = candidates.sortedByDescending { it.score }
        val best = sorted.first()

        // Exchange if even the best move is too weak and bag has tiles
        if (best.score < difficulty.exchangeThreshold && bagSize >= 7) {
            return AiDecision.ExchangeTiles
        }

        // Pick from top-N to add human-like variance on lower difficulties
        val topN = sorted.take(difficulty.pickTopN)
        val chosen = topN.random()
        return AiDecision.PlayWord(chosen)
    }

    // ── Move finder ────────────────────────────────────────────────────────

    private fun findAllMoves(
        board: Array<Array<Tile?>>,
        rack: List<Char>,
        boardEmpty: Boolean
    ): List<AiMove> {
        val moves = mutableListOf<AiMove>()
        val anchors = if (boardEmpty) listOf(7 to 7) else findAnchors(board)

        for ((anchorRow, anchorCol) in anchors) {
            for (isHorizontal in listOf(true, false)) {
                moves += generateMovesAt(board, rack, anchorRow, anchorCol, isHorizontal, boardEmpty)
            }
        }

        // Deduplicate by (word, startRow, startCol, isHorizontal)
        return moves.distinctBy { "${it.word}:${it.tiles.minOf { t -> if (it.isHorizontal) t.col else t.row }}" +
                ":${it.isHorizontal}" }
    }

    /**
     * Anchor squares = any empty cell that is orthogonally adjacent to a
     * placed tile, plus (for the very first move) the centre.
     */
    private fun findAnchors(board: Array<Array<Tile?>>): List<Pair<Int, Int>> {
        val anchors = mutableListOf<Pair<Int, Int>>()
        for (r in 0..14) {
            for (c in 0..14) {
                if (board[r][c] != null) continue
                val neighbors = listOf(r - 1 to c, r + 1 to c, r to c - 1, r to c + 1)
                if (neighbors.any { (nr, nc) -> nr in 0..14 && nc in 0..14 && board[nr][nc] != null }) {
                    anchors += r to c
                }
            }
        }
        return anchors
    }

    /**
     * For a given anchor and direction, try every word in vocabulary that
     * could be placed through/near that anchor using tiles from [rack].
     */
    private fun generateMovesAt(
        board: Array<Array<Tile?>>,
        rack: List<Char>,
        anchorRow: Int,
        anchorCol: Int,
        isHorizontal: Boolean,
        boardEmpty: Boolean
    ): List<AiMove> {
        val results = mutableListOf<AiMove>()
        val rackLetters = rack.toMutableList()
        val hasBlank = '?' in rackLetters

        for (word in vocabulary) {
            if (word.length < 2) continue

            // Try every offset of the word that passes through the anchor cell
            for (offset in word.indices) {
                val startRow = if (isHorizontal) anchorRow else anchorRow - offset
                val startCol = if (isHorizontal) anchorCol - offset else anchorCol

                if (startRow < 0 || startCol < 0) continue
                val endRow = if (isHorizontal) startRow else startRow + word.length - 1
                val endCol = if (isHorizontal) startCol + word.length - 1 else startCol
                if (endRow > 14 || endCol > 14) continue

                // First move must cover centre
                if (boardEmpty) {
                    val coversCenter = (0 until word.length).any { i ->
                        val r = if (isHorizontal) startRow else startRow + i
                        val c = if (isHorizontal) startCol + i else startCol
                        r == 7 && c == 7
                    }
                    if (!coversCenter) continue
                }

                val move = tryPlaceWord(
                    board, word, startRow, startCol, isHorizontal,
                    rackLetters, hasBlank, boardEmpty
                ) ?: continue

                results += move
            }
        }
        return results
    }

    /**
     * Attempts to place [word] starting at (startRow, startCol).
     * Returns an [AiMove] with score if legal, null otherwise.
     *
     * Legality checks:
     * 1. Each cell is either already the correct letter on the board OR
     *    we consume a matching tile from the rack (or a blank).
     * 2. No tile placed beyond the word's end in the same direction.
     * 3. All cross-words formed are in the vocabulary.
     * 4. At least one new tile is placed.
     * 5. Connects to existing tiles (unless board is empty).
     */
    private fun tryPlaceWord(
        board: Array<Array<Tile?>>,
        word: String,
        startRow: Int,
        startCol: Int,
        isHorizontal: Boolean,
        availableRack: List<Char>,
        hasBlank: Boolean,
        boardEmpty: Boolean
    ): AiMove? {
        val tempRack = availableRack.toMutableList()
        val newTiles = mutableListOf<PlacedTile>()

        for (i in word.indices) {
            val r = if (isHorizontal) startRow else startRow + i
            val c = if (isHorizontal) startCol + i else startCol
            val letter = word[i].uppercaseChar()

            val existing = board[r][c]
            if (existing != null) {
                // Must match the existing board tile
                if (existing.letter.uppercaseChar() != letter) return null
                // This tile is already placed — don't consume from rack
            } else {
                // Need to play this tile from the rack
                when {
                    tempRack.contains(letter) -> tempRack.remove(letter)
                    hasBlank && tempRack.contains('?') -> {
                        tempRack.remove('?')
                        // Place blank as this letter (0 points)
                        newTiles.add(PlacedTile(r, c, letter, 0, isBlank = true))
                        continue
                    }
                    else -> return null   // can't form this word
                }
                newTiles.add(PlacedTile(r, c, letter, letterValues[letter] ?: 0, isBlank = false))
            }
        }

        // Must place at least one new tile
        if (newTiles.isEmpty()) return null

        // Must not extend into an occupied cell immediately after the word
        val afterR = if (isHorizontal) startRow else startRow + word.length
        val afterC = if (isHorizontal) startCol + word.length else startCol
        if (afterR in 0..14 && afterC in 0..14 && board[afterR][afterC] != null) return null

        // Must not have an occupied cell immediately before the word
        val beforeR = if (isHorizontal) startRow else startRow - 1
        val beforeC = if (isHorizontal) startCol - 1 else startCol
        if (beforeR in 0..14 && beforeC in 0..14 && board[beforeR][beforeC] != null) return null

        // Must connect to existing tiles (unless first move)
        if (!boardEmpty) {
            val connects = newTiles.any { t ->
                listOf(t.row - 1 to t.col, t.row + 1 to t.col, t.row to t.col - 1, t.row to t.col + 1)
                    .any { (nr, nc) -> nr in 0..14 && nc in 0..14 && board[nr][nc] != null }
            }
            // Also connected if the word itself overlaps existing tiles
            val overlaps = (0 until word.length).any { i ->
                val r = if (isHorizontal) startRow else startRow + i
                val c = if (isHorizontal) startCol + i else startCol
                board[r][c] != null
            }
            if (!connects && !overlaps) return null
        }

        // Validate all cross-words formed by new tiles
        for (tile in newTiles) {
            val crossWord = buildCrossWord(board, tile.row, tile.col, !isHorizontal, newTiles)
            if (crossWord.length >= 2 && !vocabulary.contains(crossWord.uppercase())) return null
        }

        // Score the move
        val score = scoreMove(board, word, startRow, startCol, isHorizontal, newTiles)

        return AiMove(
            tiles        = newTiles,
            score        = score,
            word         = word,
            isHorizontal = isHorizontal
        )
    }

    // ── Scoring ────────────────────────────────────────────────────────────

    private fun scoreMove(
        board: Array<Array<Tile?>>,
        word: String,
        startRow: Int,
        startCol: Int,
        isHorizontal: Boolean,
        newTiles: List<PlacedTile>
    ): Int {
        var total = 0

        // Score the main word
        total += scoreWord(board, startRow, startCol, isHorizontal, newTiles)

        // Score each cross-word formed
        for (tile in newTiles) {
            val crossWord = buildCrossWord(board, tile.row, tile.col, !isHorizontal, newTiles)
            if (crossWord.length >= 2) {
                total += scoreWord(board, tile.row, tile.col, !isHorizontal, newTiles)
            }
        }

        // Bingo bonus
        if (newTiles.size == 7) total += 50

        return total
    }

    private fun scoreWord(
        board: Array<Array<Tile?>>,
        startRow: Int,
        startCol: Int,
        isHorizontal: Boolean,
        newTiles: List<PlacedTile>
    ): Int {
        // Walk to real start of the word (may extend into existing tiles)
        var sr = startRow; var sc = startCol
        while (true) {
            val nr = if (isHorizontal) sr else sr - 1
            val nc = if (isHorizontal) sc - 1 else sc
            if (nr in 0..14 && nc in 0..14 && (board[nr][nc] != null || newTiles.any { it.row == nr && it.col == nc }))
            { sr = nr; sc = nc } else break
        }

        var letterScore = 0; var wordMult = 1; var r = sr; var c = sc
        while (r in 0..14 && c in 0..14) {
            val newTile = newTiles.find { it.row == r && it.col == c }
            val boardTile = board[r][c]
            val tile = newTile ?: boardTile?.let {
                PlacedTile(r, c, it.letter, it.points, it.isBlank)
            } ?: break

            var lv = tile.points
            if (newTile != null) {
                when (premiumSquares["$r,$c"]) {
                    "DL" -> lv *= 2
                    "TL" -> lv *= 3
                    "DW" -> wordMult *= 2
                    "TW" -> wordMult *= 3
                }
                if (r == 7 && c == 7) wordMult *= 2
            }
            letterScore += lv
            r += if (isHorizontal) 0 else 1
            c += if (isHorizontal) 1 else 0
        }
        return letterScore * wordMult
    }

    // ── Cross-word builder ─────────────────────────────────────────────────

    private fun buildCrossWord(
        board: Array<Array<Tile?>>,
        row: Int,
        col: Int,
        isHorizontal: Boolean,
        newTiles: List<PlacedTile>
    ): String {
        fun tileAt(r: Int, c: Int): Char? {
            newTiles.find { it.row == r && it.col == c }?.let { return it.letter }
            return board[r][c]?.letter
        }

        var sr = row; var sc = col
        while (true) {
            val nr = if (isHorizontal) sr else sr - 1
            val nc = if (isHorizontal) sc - 1 else sc
            if (nr in 0..14 && nc in 0..14 && tileAt(nr, nc) != null) { sr = nr; sc = nc } else break
        }

        val sb = StringBuilder()
        var r = sr; var c = sc
        while (r in 0..14 && c in 0..14) {
            val ch = tileAt(r, c) ?: break
            sb.append(ch)
            r += if (isHorizontal) 0 else 1
            c += if (isHorizontal) 1 else 0
        }
        return sb.toString()
    }
}
