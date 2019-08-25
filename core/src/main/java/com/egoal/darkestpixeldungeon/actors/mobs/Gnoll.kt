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

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.sprites.GnollSprite
import com.watabou.utils.Random

open class Gnoll : Mob() {
    init {
        spriteClass = GnollSprite::class.java

        HT = 12
        HP = HT
        defenseSkill = 4

        EXP = 2
        maxLvl = 8

        loot = Gold::class.java
        lootChance = 0.3f
    }

    override fun giveDamage(target: Char): Damage = Damage(Random.NormalIntRange(1, 5), this, target)

    override fun defendDamage(dmg: Damage): Damage = dmg.apply { dmg.value -= Random.IntRange(0, 2) }

    override fun attackSkill(target: Char): Int = 10

}
