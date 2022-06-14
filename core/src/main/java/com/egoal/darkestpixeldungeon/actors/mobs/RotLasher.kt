package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple

import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.sprites.RotLasherSprite
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.min

class RotLasher : Mob() {
    init {
        spriteClass = RotLasherSprite::class.java

        WANDERING = Waiting()
        state = WANDERING
    }

    private var level = 1

    fun setLevel(level: Int) {
        this.level = level

        Config = Config.copy(
                MaxHealth = 5 * level,
                EXP = level / 3 + 1,
                MaxLevel = level + 2,
                AttackSkill = 10f + level,
                DefendSkill = 3f + level
        )

        enemySeen = true
    }

    override fun act(): Boolean {
        if (enemy == null || !Dungeon.level.adjacent(pos, enemy!!.pos))
            HP = min(HT, HP + 3)

        return super.act()
    }

    override fun giveDamage(enemy: Char): Damage = Damage(Random.NormalIntRange(1 + level / 2, 2 + level), this, enemy)

    override fun defendDamage(dmg: Damage): Damage = dmg.apply {
        value -= Random.NormalIntRange(0, level)
    }

    override fun resistDamage(dmg: Damage): Damage {
        if (dmg.from is Burning) dmg.value = max(dmg.value, HT / 2) // burned...
        return super.resistDamage(dmg)
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    override fun attackProc(dmg: Damage): Damage {
        Buff.affect(dmg.to as Char, Cripple::class.java, 2f)
        return super.attackProc(dmg)
    }

    // immovable 
    override fun getCloser(target: Int): Boolean = true

    override fun getFurther(target: Int): Boolean = true

    private inner class Waiting : Mob.Wandering()

    companion object {
        private val IMMUNITIES: HashSet<Class<*>> = hashSetOf(ToxicGas::class.java)
    }
}