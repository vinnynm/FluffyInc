package com.enigma.fluffyinc.apps.games.wildtactics.data

import androidx.compose.ui.graphics.Color

data class Card(
    val id: Int,
    val animal: Animal,
    val currentStrength: Int = animal.strength,
    val hasAttacked: Boolean = false
) {
    val color1: Color = when (animal.animalType) {
        AnimalType.Predator -> Color(0xFFE53935)
        AnimalType.Trickster -> Color(0xFF8E24AA)
        AnimalType.CounterAnimal -> Color(0xFF1E88E5)
    }

    val color2: Color = when (animal.animalClass) {
        AnimalClass.Common -> Color(0xFF757575)
        AnimalClass.Rare -> Color(0xFF43A047)
        AnimalClass.Epic -> Color(0xFFFB8C00)
        AnimalClass.Legendary -> Color(0xFFFDD835)
    }
}
