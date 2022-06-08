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
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.Ability
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.CrippleAttackAbility
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.sprites.GnollSprite
import com.watabou.utils.Random

open class Gnoll : Mob() {
    init {
        spriteClass = GnollSprite::class.java

        PropertyConfiger.set(this, "Gnoll")
        loot = Gold::class.java
    }

    override fun randomAbilities(): List<Ability> = when (Random.Int(10)) {
        0 -> listOf(CrippleAttackAbility())
        else -> listOf()
    }
}
