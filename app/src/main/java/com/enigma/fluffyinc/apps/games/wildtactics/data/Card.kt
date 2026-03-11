package com.enigma.fluffyinc.apps.games.wildtactics.data

import androidx.compose.ui.graphics.Color

data class Card(
    val id: Int = 0,
    var animal: Animal,
    val color1: Color = when(animal.animalType){
        AnimalType.CounterAnimal -> Color.Black
        is AnimalType.Normal -> Color.Blue
        is AnimalType.Predator -> Color.Red
        is AnimalType.Trickster -> Color.Magenta
    },
    val color2: Color = when(animal.animalClass) {
        AnimalClass.Common ->  Color(0.451f, 0.447f, 0.427f, 0.894f)
        AnimalClass.Epic -> Color(0.545f, 0.145f, 0.761f, 0.216f)
        AnimalClass.Legendary ->  Color(0.58431375f,0.41960785f,0.06666667f, 0.21568628f)
        AnimalClass.Rare ->  Color(0.824f, 0.161f, 0.216f, 0.216f)
    }
)