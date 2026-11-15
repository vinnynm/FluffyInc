package com.enigma.fluffyinc.apps.games.explodingkitties3.data.types

import kotlinx.serialization.Serializable

@Serializable
enum class CardType {
    EXPLODING_KITTEN,
    DEFUSE,
    SKIP,
    ATTACK,
    SEE_FUTURE,
    SHUFFLE,
    NORMAL,
    NOPE
}