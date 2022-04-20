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

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.PropertyConfiger
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.ExplodeDyingAbility
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.artifacts.HandOfTheElder
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.SkeletonSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.HashSet

class Skeleton : Mob() {
    init {
        PropertyConfiger.set(this, "Skeleton")

        spriteClass = SkeletonSprite::class.java
        loot = Generator.WEAPON.generate()

        immunities.addAll(listOf(Bleeding::class.java))
        abilities.add(ExplodeDyingAbility())
    }

    override fun createLoot(): Item? {
        return if (!Dungeon.limitedDrops.handOfElder.dropped() && Random.Float() < 0.04f) {
            Dungeon.limitedDrops.handOfElder.drop()
            HandOfTheElder().random()
        } else {
            var loot: Item
            do {
                loot = Generator.WEAPON.generate()
                //50% chance of re-rolling tier 4 or 5 items
            } while (loot is MeleeWeapon && loot.tier >= 4 &&
                    Random.Int(2) == 0)
            loot.level(0)

            loot
        }
    }
}
