package com.enigma.fluffyinc.apps.readables.epublibrary.data

data class EpubContent(
    val title: String,
    val chapters: List<Chapter>
)

data class Chapter(
    val title: String,
    val content: String,
    val order: Int = 0
)
