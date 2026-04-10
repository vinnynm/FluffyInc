package com.enigma.fluffyinc.navigation

sealed class Screens(val route: String) {
    data object Home : Screens("home")
    data object Finance : Screens("finance")
    data object Readables : Screens("readables")
    data object Games : Screens("games")
    data object Settings : Screens("settings")
    data object Lists : Screens("lists_main")
    data object Lexicon : Screens("lexicon")
    data object Betweenle : Screens("betweenle")
    data object Absurdle : Screens("absurdle")
    data object ChromaWord : Screens("chromaword")
    data object KillerSudoku : Screens("killersudoku")
    data object Sudoku : Screens("sudoku")
    data object LightsOut : Screens("lightsout")
}
