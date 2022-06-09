package com.egoal.darkestpixeldungeon

import android.util.Log
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.watabou.noosa.Game
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object Database {
    private const val DATA_FILE = "data/mobs.json"
    private val _mobs: Map<String, MobProperty>

    init {
        val br = Game.instance.assets.open(DATA_FILE).bufferedReader().readText()
        _mobs = Json.decodeFromString(br)
    }

    @Serializable
    data class MobProperty(
            val HT: Int = 0,
            val atkSkill: Float = 0f, val defSkill: Float = 0f,
            val EXP: Int = 0, val maxLvl: Int = 0,
            val lootChance: Float = 0f, val loot: String = "",
            val minDamage: Int = 0, val maxDamage: Int = 0, val typeDamage: Damage.Type = Damage.Type.NORMAL,
            val criticalChance: Float = 0f, val criticalRatio: Float = 1f,
            val minDefend: Int = 0, val maxDefend: Int = 0,
            val magicalResistance: Float = 0f) {
        val elementalResistances = FloatArray(Damage.Element.ELEMENT_COUNT) { 0f }
        val properties = hashSetOf<Char.Property>()
    }

    fun InitMob(mob: Mob, key: String){
        if (!_mobs.containsKey(key)) Log.w("dpd", "missing mob config for: $key")
        else {
            TODO()
        }
    }

    fun InitMob(mob: Mob) = InitMob(mob, mob.javaClass.simpleName)
}