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
                        minDefend = int(at("minDefend")), maxDefend = int(at("maxDefend")),
                        magicalResistance = float(at("magicalResistance"))
                ).apply {
                    setElementalResistance(at("elementalResistance"))
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
        mob.minDefense = mp.minDefend
        mob.maxDefense = mp.maxDefend

        mob.magicalResistance = mp.magicalResistance

        for(pr in mp.elementalResistances) mob.addResistances(pr.key, pr.value)
        for(p in mp.properties) mob.properties().add(p)

        mob.HP = mob.HT
    }

    data class MobProperty(
            val HT: Int = 0,
            val atkSkill: Float = 0f, val defSkill: Float = 0f,
            val EXP: Int = 0, val maxLvl: Int = 0,
            val lootChance: Float = 0f, val loot: String = "",
            val minDamage: Int = 0, val maxDamage: Int = 0, val typeDamage: Damage.Type = Damage.Type.NORMAL,
            val minDefend: Int = 0, val maxDefend: Int = 0,
            val magicalResistance: Float = 0f) {
        val elementalResistances = hashMapOf<Int, Float>()
        val properties = hashSetOf<Char.Property>()

        fun setElementalResistance(string: String) {
            if (string.isEmpty()) return

            for (pr in string.split(' ')) {
                val prs = pr.split(':')
                val ele = Damage.Element.String2Element(prs[0])
                val rt = prs[1].toFloat()

                elementalResistances[ele] = rt
            }
        }

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