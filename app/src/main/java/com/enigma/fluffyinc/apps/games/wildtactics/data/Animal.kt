package com.enigma.fluffyinc.apps.games.wildtactics.data

sealed class Animal(
    val name: String,
    val animalType: AnimalType,
    val strength: Int = 3,
    val animalClass: AnimalClass = AnimalClass.Common,
    val upgradesTo: Animal? = null
) {
    // Predators (Strong attackers)
    data object Leopard : Animal("Leopard", AnimalType.Predator, 3, AnimalClass.Common)
    data object Puma : Animal("Puma", AnimalType.Predator, 5, AnimalClass.Rare, Leopard)
    data object Lion : Animal("Lion", AnimalType.Predator, 8, AnimalClass.Epic, Puma)
    data object Tiger : Animal("Tiger", AnimalType.Predator, 9, AnimalClass.Epic)
    data object Jaguar : Animal("Jaguar", AnimalType.Predator, 6, AnimalClass.Rare, Tiger)
    data object Panther : Animal("Panther", AnimalType.Predator, 10, AnimalClass.Legendary)

    // Tricksters (Strategic/Special)
    data object Monkey : Animal("Monkey", AnimalType.Trickster, 3, AnimalClass.Common)
    data object Owl : Animal("Owl", AnimalType.Trickster, 4, AnimalClass.Rare)
    data object Fox : Animal("Fox", AnimalType.Trickster, 5, AnimalClass.Rare)
    data object Kitsune : Animal("Kitsune", AnimalType.Trickster, 8, AnimalClass.Legendary)
    data object Bunny : Animal("Bunny", AnimalType.Trickster, 2, AnimalClass.Common)

    // Counters (Defensive)
    data object Bison : Animal("Bison", AnimalType.CounterAnimal, 5, AnimalClass.Common)
    data object Rhino : Animal("Rhino", AnimalType.CounterAnimal, 7, AnimalClass.Rare, Bison)
    data object Elephant : Animal("Elephant", AnimalType.CounterAnimal, 10, AnimalClass.Epic, Rhino)
    data object Antelope : Animal("Antelope", AnimalType.CounterAnimal, 4, AnimalClass.Common)
    data object Moose : Animal("Moose", AnimalType.CounterAnimal, 8, AnimalClass.Epic)

    companion object {
        fun getAll(): List<Animal> = listOf(
            Leopard, Puma, Lion, Tiger, Jaguar, Panther,
            Monkey, Owl, Fox, Kitsune, Bunny,
            Bison, Rhino, Elephant, Antelope, Moose
        )
    }
}

sealed class AnimalType {
    data object Predator : AnimalType()
    data object Trickster : AnimalType()
    data object CounterAnimal : AnimalType()
}

sealed class AnimalClass {
    data object Common : AnimalClass()
    data object Rare : AnimalClass()
    data object Epic : AnimalClass()
    data object Legendary : AnimalClass()
}
