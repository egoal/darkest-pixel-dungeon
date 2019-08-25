package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple

import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.sprites.RotLasherSprite
import com.watabou.utils.Random
import java.util.HashSet

class RotLasher : Mob() {
    init {
        spriteClass = RotLasherSprite::class.java

        HP = 1
        HT = 1

        defenseSkill = 0
        EXP = 1

        loot = Generator.SEED
        lootChance = 1f

        WANDERING = Waiting()
        state = WANDERING

        properties.add(Property.IMMOVABLE)

        addResistances(Damage.Element.POISON, 2f)
        addResistances(Damage.Element.FIRE, 0.2f)
    }

    private var level = 1

    fun setLevel(level: Int) {
        this.level = level

        HT = 5 * level
        HP = HT
        EXP = level / 3 + 1
        maxLvl = level + 2

        defenseSkill = 3 + level
        enemySeen = true
    }

    override fun act(): Boolean {
        if (enemy == null || !Dungeon.level.adjacent(pos, enemy.pos))
            HP = Math.min(HT, HP + 3)

        return super.act()
    }

    override fun attackSkill(target: Char): Int = 10 + level

    override fun giveDamage(enemy: Char): Damage = Damage(Random.NormalIntRange(1 + level / 2, 2 + level), this, enemy)

    override fun defendDamage(dmg: Damage): Damage = dmg.apply {
        value -= Random.NormalIntRange(0, level)
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    override fun attackProc(dmg: Damage): Damage {
        Buff.affect(dmg.to as Char, Cripple::class.java, 2f)
        return super.attackProc(dmg)
    }

    // immovable 
    override fun getCloser(target: Int): Boolean = true

    override fun getFurther(target: Int): Boolean = true

    private inner class Waiting : Mob.Wandering() {}

    companion object {
        private val IMMUNITIES: HashSet<Class<*>> = hashSetOf(ToxicGas::class.java)
    }
}