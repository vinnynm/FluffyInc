package com.enigma.fluffyinc.navigation

sealed class Screens(val route: String) {
    data object Home : Screens("home")
    data object Finance : Screens("finance")
    data object Readables : Screens("readables")
    data object Games : Screens("games")
    data object Settings : Screens("settings")
    data object Lists : Screens("lists_main")
}
