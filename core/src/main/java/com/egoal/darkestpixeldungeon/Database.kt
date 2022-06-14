package com.egoal.darkestpixeldungeon

import android.util.Log
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.watabou.noosa.Game
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

object Database {
    private const val DATA_FILE = "data/database.cdb"

    private val _mobLines: List<MobsLine>

    val DummyMobConfig: MobsLine by lazy { ConfigOfMob("DUMMY")!! }

    init {
        val br = Game.instance.assets.open(DATA_FILE).bufferedReader().readText()
        val database = Json.decodeFromString<Map<String, JsonElement>>(br)
        val mobsheet = database["sheets"]!!.jsonArray.find { it.jsonObject["name"]!!.jsonPrimitive.content == "Mobs" }

        _mobLines = mobsheet!!.jsonObject["lines"]!!.jsonArray.map { Json.decodeFromJsonElement(it) }

        Log.d("dpd", "database file loaded: ${_mobLines.size} mobs loaded.")
    }

    fun Test() {
        Log.d("dpd", "do")
    }

    @Serializable
    data class ResistanceLine(val Magic: Float, val Fire: Float, val Poison: Float, val Ice: Float, val Light: Float, val Shadow: Float, val Holy: Float)

    @Serializable
    data class LootLine(val Name: String, val Chance: Float)

    /**
     * TypeDamage: Normal, Magical, Mental
     * Properties: BOSS, MINIBOSS, UNDEAD, DEMONIC, MACHINE, IMMOVABLE, PHANTOM
     */
    @Serializable
    data class MobsLine(val Class: String, val MaxHealth: Int, val AttackSkill: Float, val DefendSkill: Float,
                        val EXP: Int, val MaxLevel: Int, val MinDamage: Int, val MaxDamage: Int, val TypeDamage: Int,
                        val CritChance: Float, val CritRatio: Float, val MinDefend: Int, val MaxDefend: Int,
                        val Resistance: List<ResistanceLine>, val Properties: Int,
                        val LootChance: Float, val Loot: List<LootLine>) {
        val DamageType: Damage.Type by lazy {
            when (TypeDamage) {
                0 -> Damage.Type.NORMAL
                1 -> Damage.Type.MAGICAL
                2 -> Damage.Type.MENTAL
                else -> TODO()
            }
        }

        val MobProperties: HashSet<Char.Property> by lazy {
            HashSet<Char.Property>().apply {
                for (pr in _proparr.withIndex())
                    if ((Properties and (0x01 shl pr.index)) != 0) add(pr.value)
            }
        }
    }

    fun ConfigOfMob(key: String) = _mobLines.find { it.Class == key }

    private val _proparr = arrayOf(Char.Property.BOSS, Char.Property.MINIBOSS, Char.Property.UNDEAD,
            Char.Property.DEMONIC, Char.Property.IMMOVABLE, Char.Property.PHANTOM)
}