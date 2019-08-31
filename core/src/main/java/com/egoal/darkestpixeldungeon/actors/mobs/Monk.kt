/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Amok
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Imp
import com.egoal.darkestpixeldungeon.items.KindOfWeapon
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.weapon.melee.Knuckles
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.MonkSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.HashSet

open class Monk : Mob() {
    private var hitsToDisarm = 0

    init {
        spriteClass = MonkSprite::class.java

        HT = 70
        HP = HT
        defenseSkill = 30

        EXP = 11
        maxLvl = 21

        loot = Food()
        lootChance = 0.064f

        addResistances(Damage.Element.SHADOW, 1.25f)
        addResistances(Damage.Element.HOLY, .667f)
    }

    override fun giveDamage(target: Char): Damage {
        val value = Random.NormalIntRange(12, 25)
        val dmg = Damage(value, this, target)
        if (value > 20) dmg.addFeature(Damage.Feature.CRITICAL)
        return dmg
    }

    override fun attackSkill(target: Char): Int = 30

    override fun attackDelay(): Float = 0.45f

    override fun defendDamage(dmg: Damage): Damage = dmg.apply { value -= Random.NormalIntRange(0, 2) }

    override fun die(cause: Any) {
        Imp.Quest.process(this)

        super.die(cause)
    }

    override fun attackProc(damage: Damage): Damage {
        if (damage.to === Dungeon.hero) {

            val hero = Dungeon.hero
            val weapon = hero.belongings.weapon

            if (weapon != null && weapon !is Knuckles && !weapon.cursed) {
                if (hitsToDisarm == 0) hitsToDisarm = Random.NormalIntRange(3, 7)

                if (--hitsToDisarm == 0) {
                    hero.belongings.weapon = null
                    Dungeon.quickslot.clearItem(weapon)
                    weapon.updateQuickslot()

                    val ops = hero.pos + (hero.pos - pos)
                    val dst = if (Level.passable[ops] && Actor.findChar(ops) == null) ops else hero.pos

                    Dungeon.level.drop(weapon, dst).sprite.drop()
                    GLog.w(Messages.get(this, "disarm", weapon.name()))
                }
            }
        }

        return damage
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(DISARMHITS, hitsToDisarm)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        hitsToDisarm = bundle.getInt(DISARMHITS)
    }

    companion object {

        private val IMMUNITIES = hashSetOf<Class<*>>(Amok::class.java, Terror::class.java)

        private const val DISARMHITS = "hitsToDisarm"
    }
}
