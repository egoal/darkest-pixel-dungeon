package com.egoal.darkestpixeldungeon

import android.util.Log
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.watabou.noosa.Game

// hand craft database
object PropertyConfiger {
    private const val MOBS_FILE = "data/mobs.csv"

    private val headerIndex = mutableMapOf<String, Int>()
    private val mobsProperties = mutableMapOf<String, MobProperty>()

    init {
        val br = Game.instance.assets.open(MOBS_FILE).bufferedReader()

        val header = br.readLine()
        val titles = header.split(',')
        for (pr in titles.withIndex())
            headerIndex[pr.value] = pr.index

        println(header)

        br.useLines {
            it.forEach { line ->
                val elements = line.split(',')
                val at = { str: String -> elements[headerIndex[str]!!] }

                val name = elements[0]
                val mp = MobProperty(
                        HT = int(at("HT"), 1),
                        atkSkill = float(at("atkSkill")), defSkill = float(at("defSkill")),
                        EXP = int(at("EXP"), 1), maxLvl = int(at("maxLvl"), Hero.MAX_LEVEL),
                        lootChance = float(at("lootChance")), loot = at("loot"), //todo: loot
                        minDamage = int(at("minDamage")), maxDamage = int(at("maxDamage")), typeDamage = damageType(at("typeDamage")),
                        criticalChance = float(at("critChance")), criticalRatio = float(at("critRatio"), 1f),
                        minDefend = int(at("minDefend")), maxDefend = int(at("maxDefend")),
                        magicalResistance = float(at("magicalResistance"))).apply {
                    elementalResistances[0] = float(at("FIRE"))
                    elementalResistances[1] = float(at("POISON"))
                    elementalResistances[2] = float(at("ICE"))
                    elementalResistances[3] = float(at("LIGHT"))
                    elementalResistances[4] = float(at("SHADOW"))
                    elementalResistances[5] = float(at("HOLY"))
                    setProperties(at("Properties"))
                }

                mobsProperties[name] = mp
            }
        }

        Log.d("dpd", "${mobsProperties.size} mobs loaded.")
    }

    fun set(mob: Mob, tag: String) {
        assert(mobsProperties.contains(tag))

        val mp = mobsProperties[tag]!!
        mob.HT = mp.HT
        mob.atkSkill = mp.atkSkill
        mob.defSkill = mp.defSkill
        mob.EXP = mp.EXP
        mob.maxLvl = mp.maxLvl
        mob.lootChance = mp.lootChance
        //todo: set loot
        mob.minDamage = mp.minDamage
        mob.maxDamage = mp.maxDamage
        mob.typeDamage = mp.typeDamage
        mob.criticalChance = mp.criticalChance
        mob.criticalRatio = mp.criticalRatio
        mob.minDefense = mp.minDefend
        mob.maxDefense = mp.maxDefend

        mob.magicalResistance = mp.magicalResistance

        for (i in 0 until mp.elementalResistances.size) mob.elementalResistance[i] = mp.elementalResistances[i];
        for (p in mp.properties) mob.properties().add(p)

        mob.HP = mob.HT


    }

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

        fun setProperties(string: String) {
            if (string.isEmpty()) return

            for (str in string.split(' '))
                properties.add(enumValueOf(str))
        }
    }

    private fun int(string: String, default: Int = 0) = string.toIntOrNull() ?: default
    private fun float(string: String, default: Float = 0f) = string.toFloatOrNull() ?: default
    private fun damageType(string: String, default: Damage.Type = Damage.Type.NORMAL) =
            if (string.isEmpty()) default else enumValueOf(string)

}