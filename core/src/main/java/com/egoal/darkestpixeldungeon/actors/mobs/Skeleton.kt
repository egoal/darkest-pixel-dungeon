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

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.Ability
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.ExplodeDying
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.MentalExplodeDying
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.RespawnDying
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.HandOfTheElder
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.sprites.SkeletonSprite
import com.watabou.utils.Random

class Skeleton : Mob() {
    init {
        spriteClass = SkeletonSprite::class.java

        immunities.addAll(listOf(Bleeding::class.java))
        abilities.add(ExplodeDying())
    }

    override fun createLoot(): Item? {
        return if (!Dungeon.limitedDrops.handOfElder.dropped() && Random.Float() < 0.04f) {
            Dungeon.limitedDrops.handOfElder.drop()
            HandOfTheElder().random()
        } else super.createLoot().apply {
            if (this is MeleeWeapon && tier >= 4 && Random.Int(2) == 0) level(0)
        }
    }
}
