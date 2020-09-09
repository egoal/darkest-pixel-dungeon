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

import com.egoal.darkestpixeldungeon.PropertyConfiger
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.sprites.BruteSprite
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.HashSet

open class Brute : Mob() {
    private var enraged = false

    init {
        PropertyConfiger.set(this, "Brute")
        spriteClass = BruteSprite::class.java

        loot = Gold::class.java
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        enraged = HP < HT / 4
    }

    override fun giveDamage(enemy: Char): Damage {
        val damage = super.giveDamage(enemy)
        if (!damage.isFeatured(Damage.Feature.CRITICAL) && enraged) {
            damage.value = (damage.value * Random.Float(1.25f, 1.75f)).toInt()
            damage.addFeature(Damage.Feature.CRITICAL)
        }
        return damage
    }

    override fun takeDamage(dmg: Damage): Int {
        val value = super.takeDamage(dmg)

        if (isAlive && !enraged && HP < HT / 3) {
            enraged = true
            if (Dungeon.visible[pos]) {
                GLog.w(Messages.get(this, "enraged_text"))
                sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "enraged"))
            }
            spend(TIME_TO_ENRAGE)
        }

        return value
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    companion object {
        private const val TIME_TO_ENRAGE = 1f

        private val IMMUNITIES = hashSetOf<Class<*>>(Terror::class.java)
    }
}
