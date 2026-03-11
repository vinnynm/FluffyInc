package com.enigma.fluffyinc.apps.games.wildtactics.data

import kotlin.collections.component1

data class Player(
    val id: Int =0,
    val name: String = "Player $id",
    val isAlive: Boolean = true,
    var playerEffects: MutableMap<PlayerEffects, Int> = mutableMapOf(),
    var isAiPlayer:Boolean = false,
    var hand: MutableList<Card> = mutableListOf(),
    var lives:Int = 100
){
    fun runTurn(playerEffectsExpiry: MutableMap<PlayerEffects, Int>): MutableMap<PlayerEffects, Int> {
        return playerEffectsExpiry
            .map {
                    mapEntry ->
                mapEntry.key to (mapEntry.value-1)
            }
            .toMap()
            .filter { (_, i)->
                i>0
            }
            .toMutableMap()

    }

    fun runTheTurn(){
        playerEffects.keys.forEach {effects ->
            when(effects){
                PlayerEffects.Ambush -> {

                }
                is PlayerEffects.Bleeding -> {
                    lives- effects.bleedingDamage
                }
                PlayerEffects.Blind -> TODO()
                PlayerEffects.MonkeySee -> TODO()
                is PlayerEffects.Poisoned -> {
                    lives - effects.poisonDamage
                }
                PlayerEffects.ShieldOfSavannah -> {

                }
                PlayerEffects.SwordOfHorus -> {

                }
                PlayerEffects.ToxicBlood -> {

                }
            }

        }
        playerEffects = runTurn(playerEffects)
    }


}



sealed class PlayerEffects(val lastsFOrTurns:Int = 2){
    data class Poisoned(val poisonDamage:Int = 8): PlayerEffects()
    data class Bleeding(val bleedingDamage:Int = 5): PlayerEffects()
    data object SwordOfHorus: PlayerEffects()
    data object ShieldOfSavannah: PlayerEffects()
    data object ToxicBlood: PlayerEffects()
    data object MonkeySee: PlayerEffects()

    data object Ambush:PlayerEffects()

    data object Blind: PlayerEffects()
}