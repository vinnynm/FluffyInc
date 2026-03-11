package com.enigma.fluffyinc.apps.games.wildtactics.data

sealed class Animal(
    var name: String,
    val animalType:AnimalType,
    val animalTrickCounter: Array<Counter> = getTrick(animalType = animalType),
    var strength:Int = 3,
    val sacrificialValue : Int = 3,
    val upgradesToAnimal: Animal? = Lion,
    val animalClass: AnimalClass = AnimalClass.Common,
    val noOfUpgrades:Int = when(animalClass) {
        AnimalClass.Common -> 5
        AnimalClass.Epic -> 3
        AnimalClass.Legendary -> Int.MAX_VALUE
        AnimalClass.Rare -> 2
    }
){
    data object Lion: Animal(
        name = "Lion",
        animalType = AnimalType.Predator(
            Attack.Pounce
        ),
        animalClass = AnimalClass.Epic,
        upgradesToAnimal = Sphinx
    )

    data object Puma: Animal(
        name = "Puma",
        animalType = AnimalType.Predator(
            Attack.Pounce
        ),
        animalClass = AnimalClass.Rare,
        upgradesToAnimal = Lion
    )

    data object Sphinx: Animal(
        name = "Sphinx",
        animalType = AnimalType.Predator(
            Attack.Pounce
        ),
        animalClass = AnimalClass.Legendary
    )

    data object Cougar: Animal(
        name = "Cougar",
        animalType = AnimalType.Predator(
            Attack.Pounce
        ),
        animalClass = AnimalClass.Legendary,
        upgradesToAnimal = Puma
    )



    data object Jaguar: Animal(
        name = "Jaguar",
        animalType = AnimalType.Predator(
            Attack.Pounce
        ),
        animalClass = AnimalClass.Rare,
        upgradesToAnimal = Tiger
    )

    data object Leopard: Animal(
        name = "Leopard",
        animalType = AnimalType.Predator(
            Attack.Pounce
        ),
        animalClass = AnimalClass.Common,
        upgradesToAnimal = Jaguar
    )

    data object Tiger: Animal(
        name = "Tiger",
        animalType = AnimalType.Predator(
            Attack.Pounce
        ),
        animalClass = AnimalClass.Epic,
        upgradesToAnimal = Panther
    )

    data object Panther: Animal(
        name = "Panther",
        animalType = AnimalType.Predator(
            Attack.Pounce
        ),
        animalClass = AnimalClass.Legendary
    )



    data object Monkey:Animal(
        name = "Monkey",
        animalType = AnimalType.Trickster(
            trick = Trick.Steal
        ),
        animalTrickCounter = arrayOf(Counter.MonkeySee)
    )

    data object Owl : Animal(
        name = "Owl",
        animalType = AnimalType.Trickster(
            trick = Trick.ForceAttack
        )
    )

    data object Adder : Animal(
        name = "Adder",
        animalType = AnimalType.Predator(
            attack = Attack.PoisonousBite
        ),
        animalTrickCounter = arrayOf(Counter.ToxicBlood),
        upgradesToAnimal = Mamba,
        animalClass = AnimalClass.Common
    )

    data object Taipan : Animal(
        name = "Taipan",
        animalType = AnimalType.Predator(
            attack = Attack.PoisonousBite
        ),
        animalTrickCounter = arrayOf(Counter.ToxicBlood),
        animalClass = AnimalClass.Legendary
    )

    data object Mamba : Animal(
        name = "Mamba",
        animalType = AnimalType.Predator(
            attack = Attack.PoisonousBite
        ),
        animalTrickCounter = arrayOf(Counter.ToxicBlood),
        upgradesToAnimal = Cobra,
        animalClass = AnimalClass.Rare
    )

    data object Cobra : Animal(
        name = "Cobra",
        animalType = AnimalType.Predator(
            attack = Attack.PoisonousBite
        ),
        animalTrickCounter = arrayOf(Counter.ToxicBlood),
        upgradesToAnimal = Taipan,
        animalClass = AnimalClass.Epic
    )

    data object Triceratops: Animal(
        name = "Triceratops",
        animalType = AnimalType.CounterAnimal,
        animalTrickCounter = arrayOf(Counter.ShieldOfSavannah, Counter.SchoolAndHerd,Counter.Ambush,
            Counter.SwordOfHorus
        ),
        animalClass = AnimalClass.Legendary
    )

    data object Elephant: Animal(
        name = "Elephant",
        animalType = AnimalType.CounterAnimal,
        animalTrickCounter = arrayOf(Counter.ShieldOfSavannah, Counter.SchoolAndHerd,Counter.Ambush),
        animalClass = AnimalClass.Epic
    )

    data object Rhino: Animal(
        name = "Rhino",
        animalType = AnimalType.CounterAnimal,
        animalTrickCounter = arrayOf(Counter.SwordOfHorus, Counter.Ambush),
        upgradesToAnimal = Elephant,
        animalClass = AnimalClass.Rare
    )

    data object Bison: Animal(
        name = "Rhino",
        animalType = AnimalType.CounterAnimal,
        animalTrickCounter = arrayOf(Counter.SwordOfHorus),
        upgradesToAnimal = Rhino,
        animalClass = AnimalClass.Common
    )

    data object Antelope: Animal(
        name = "Antelope",
        animalTrickCounter = arrayOf(Counter.SchoolAndHerd),
        animalType = AnimalType.CounterAnimal,
        upgradesToAnimal = Deer,
        animalClass = AnimalClass.Common
    )

    data object Deer: Animal(
        name = "Deer",
        animalTrickCounter = arrayOf(Counter.SchoolAndHerd),
        animalType = AnimalType.CounterAnimal,
        animalClass = AnimalClass.Rare,
        upgradesToAnimal = Elk
    )

    data object Elk: Animal(
        name = "Elk",
        animalTrickCounter = arrayOf(Counter.SchoolAndHerd),
        animalType = AnimalType.CounterAnimal,
        animalClass = AnimalClass.Epic,
        upgradesToAnimal = Moose
    )

    data object Moose: Animal(
        name = "Moose",
        animalTrickCounter = arrayOf(Counter.SchoolAndHerd),
        animalType = AnimalType.CounterAnimal,
        animalClass = AnimalClass.Legendary
    )



    data object BlackAnt: Animal(
        name = "Black Ant",
        animalType = AnimalType.Predator(
            attack = Attack.Bite
        ),

        animalClass = AnimalClass.Common,
        upgradesToAnimal = RedAnt
    )

    data object RedAnt: Animal(
        name = "Red Ant",
        animalType = AnimalType.Predator(
            attack = Attack.Bite
        ),

        animalClass = AnimalClass.Rare,
        upgradesToAnimal = FireAnt
    )
    data object FireAnt: Animal(
        name = "Fire Ant",
        animalType = AnimalType.Predator(
            attack = Attack.Bite
        ),

        animalClass = AnimalClass.Epic,
        upgradesToAnimal = WhiteAnt
    )

    data object WhiteAnt: Animal(
        name = "White Ant",
        animalType = AnimalType.Predator(
            attack = Attack.Bite
        ),

        animalClass = AnimalClass.Legendary
    )

    data object Dingo: Animal(
        name = "Dingo",
        animalType = AnimalType.Trickster(
            Trick.Ambush
        ),
        animalTrickCounter = arrayOf(Counter.Ambush),
        animalClass = AnimalClass.Common,
        upgradesToAnimal = Coyote
    )

    data object Coyote: Animal(
        name = "Coyote",
        animalType = AnimalType.Trickster(
            Trick.Ambush
        ),
        animalTrickCounter = arrayOf(Counter.Ambush),
        animalClass = AnimalClass.Epic,
        upgradesToAnimal = Fox
    )

    data object Fox: Animal(
        name = "Fox",
        animalType = AnimalType.Trickster(
            Trick.Ambush
        ),
        animalTrickCounter = arrayOf(Counter.Ambush),
        animalClass = AnimalClass.Epic,
        upgradesToAnimal = Kitsune
    )

    data object Kitsune: Animal(
        name = "Kitsune",
        animalType = AnimalType.Trickster(
            Trick.Ambush
        ),
        animalClass = AnimalClass.Legendary
        ,
        animalTrickCounter = arrayOf(Counter.Ambush)
    )

    data object Bear: Animal(
        name = "Bear",
        animalType = AnimalType.Predator(
            attack = Attack.Slash
        ),
        animalClass = AnimalClass.Epic
    )

    data object Wolf: Animal(
        name = "Wolf",
        animalClass = AnimalClass.Common,
        animalType = AnimalType.Predator(
            Attack.Bite
        ),
        upgradesToAnimal = GreatWolf
    )

    data object SecretaryBird:Animal(
        name = "SecretaryBird",
        animalType = AnimalType.Predator(
            Attack.Kick
        ),
        animalClass = AnimalClass.Epic
    )


    data object Falcon:Animal(
        name = "Falcon",
        animalClass = AnimalClass.Epic,
        animalType = AnimalType.Predator(
            Attack.Pounce
        )
    )

    data object Raven :Animal(
        name = "Raven",
        animalType = AnimalType.Predator(
            Attack.Pounce
        ),
        animalClass = AnimalClass.Rare,
        upgradesToAnimal = Hawk
    )

    data object Hawk :Animal(
        name = "Hawk",
        animalType = AnimalType.Predator(
            Attack.Pounce
        ),
        animalClass = AnimalClass.Rare,
        upgradesToAnimal = Falcon
    )

    data object Eagle:Animal(
        name = "Eagle",
        animalClass = AnimalClass.Legendary,
        animalType = AnimalType.Trickster(
            Trick.Steal
        )
    )

    data object Dragon : Animal(
        name = "Dragon",
        animalType = AnimalType.Predator(
            Attack.ShockAndAwe
        ),
        animalClass = AnimalClass.Legendary
    )

    data object Bunny: Animal(
        name = "Bunny",
        animalType = AnimalType.Trickster(
            trick = Trick.ReversalOfFate
        ),
        animalClass = AnimalClass.Common,
        upgradesToAnimal = Rabbit
    )
    data object Rabbit: Animal(
        name = "Rabbit",
        animalType = AnimalType.Trickster(
            trick = Trick.ReversalOfFate
        ),
        animalClass = AnimalClass.Rare,
        upgradesToAnimal = Hare
    )

    data object Hare: Animal(
        name = "Hare",
        animalType = AnimalType.Trickster(
            trick = Trick.ReversalOfFate
        ),
        animalClass = AnimalClass.Epic,
        upgradesToAnimal = EasterBunny
    )

    data object EasterBunny: Animal(
        name = "Easter Bunny",
        animalType = AnimalType.Trickster(
            trick = Trick.ReversalOfFate
        ),
        animalClass = AnimalClass.Legendary
    )



    data object BlackWidow:Animal(
        name = "Black Widow",
        animalType = AnimalType.Predator(
            Attack.PoisonousBite
        ),
        animalClass = AnimalClass.Common,
        upgradesToAnimal = Mamba
    )

    data object Tarantula: Animal(
        name = "Tarantula",
        animalClass = AnimalClass.Common,
        animalType = AnimalType.Predator(
            Attack.Bite
        ),
        upgradesToAnimal = GreatWolf
    )

    data object GreatWolf: Animal(
        name = "GreatWolf",
        animalType = AnimalType.Predator(
            Attack.Bite
        ),
        animalClass = AnimalClass.Rare,
        upgradesToAnimal = DireWolf()
    )

    data class DireWolf(
        val animalName: String = "DireWolf",
        val itsClass: AnimalClass = AnimalClass.Epic
    ): Animal(
        name = animalName,
        animalClass = AnimalClass.Epic,
        animalType = AnimalType.Predator(
            Attack.Bite
        ),
        upgradesToAnimal = Fenrir()
    )

    data class Fenrir(
        val animalName: String = "Fenrir"
    ):Animal(
        name = animalName,
        animalType = AnimalType.Predator(
            Attack.ShockAndAwe
        ),
        animalClass = AnimalClass.Legendary
    )


}


fun getTrick(animalType: AnimalType): Array< Counter> {
    return when (animalType){
        AnimalType.CounterAnimal -> {
            arrayOf(listOf(Counter.MonkeySee, Counter.ShieldOfSavannah, Counter.ToxicBlood, Counter.SwordOfHorus).random())
        }
        is AnimalType.Normal -> {
            arrayOf(listOf(Counter.ShieldOfSavannah, Counter.ToxicBlood).random())
        }
        is AnimalType.Predator -> {
           arrayOf(Counter.Ambush)
        }
        is AnimalType.Trickster -> {
           arrayOf(Counter.Ambush)
        }
    }
}

sealed class AnimalClass{
    data object Common: AnimalClass()
    data object Rare: AnimalClass()
    data object Legendary: AnimalClass()
    data object Epic: AnimalClass()
}

sealed class AnimalType(){
    data object CounterAnimal: AnimalType()
    data class Predator(
        val attack: Attack
    ): AnimalType()
    data class Trickster(
        val trick:Trick
    ): AnimalType()
    data class Normal(
        val upgradesToAnimal: Animal
    ): AnimalType()
}

sealed class Upgrade{
    data class UpgradesToAnimal(val animal: Animal, val noOfCards:Int): Upgrade()
    data class UpgradesToEffect(val effect:Effect, val noOfCards:Int): Upgrade()

}

sealed class Counter{
    data object SwordOfHorus: Counter()
    data object ShieldOfSavannah: Counter()
    data object ToxicBlood: Counter()
    data object MonkeySee: Counter()

    data object Ambush:Counter()

    data object Stampede:Counter()

    data object SchoolAndHerd: Counter()
}

sealed class Trick{
    data object Steal : Trick()
    data object ReversalOfFate : Trick()
    data object ForceAttack :Trick()
    data object Ambush: Trick()
}

sealed class Effect{
    data object Drought: Effect()
    data object BountifulLands: Effect()
    data object AgeOfGiants: Effect()
    data object FeedingFrenzy: Effect()
    data object ImmortalGrounds: Effect()
}

sealed class AnimalSize{
    data object Small: AnimalSize()
    data object Big: AnimalSize()
    data object Medium: AnimalSize()
}

sealed class Attack(val targetType:TargetType = TargetType.SingleTarget){
    data object Pounce : Attack()
    data object Slash : Attack()
    data object ShockAndAwe : Attack(targetType = TargetType.All)
    data object Bite : Attack()
    data object Rush: Attack()
    data object Kick: Attack()
    data object PoisonousBite: Attack()
}

sealed class TargetType{
    data object SingleTarget : TargetType()
    data object All: TargetType()
    data object Self : TargetType()
}