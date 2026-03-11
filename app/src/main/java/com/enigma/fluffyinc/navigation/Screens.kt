package com.enigma.fluffyinc.navigation

sealed class Screens(val route: String) {
    data object Home: Screens(route= "Home")
    data object Finance: Screens(route= "Finance")
    data object Readables: Screens(route= "Readables")
    data object Settings: Screens(route= "Settings")
    data object Games: Screens(route= "Games")
    data object Lists: Screens(route = "lists_main")
}
