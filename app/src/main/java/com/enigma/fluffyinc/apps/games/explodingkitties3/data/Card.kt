package com.enigma.fluffyinc.apps.games.explodingkitties3.data

import com.enigma.fluffyinc.games.explodingkitties3.data.types.CardType
import kotlinx.serialization.Serializable


@Serializable
data class Card(
    val suit: String,
    val rank: String,
    val type: CardType,
    val name: String,
    val imageId:Int? = null
)