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
package com.egoal.darkestpixeldungeon.plants

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.potions.PotionOfPurity
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.buffs.Drowsy
import com.egoal.darkestpixeldungeon.actors.buffs.MagicalSleep
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.actors.buffs.Slow
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness

class Dreamfoil : Plant(10) {

    override fun activate() {
        Actor.findChar(pos)?.let {
            when (it) {
                is Mob -> Buff.affect(it, MagicalSleep::class.java)
                is Hero -> {
                    GLog.i(Messages.get(this, "refreshed"))
                    for (buffClass in buffs) Buff.detach(it, buffClass)
                }
                else->{}
            }
        }
    }

    class Seed : Plant.Seed(Dreamfoil::class.java, PotionOfPurity::class.java) {
        init {
            image = ItemSpriteSheet.SEED_DREAMFOIL
        }
    }

    companion object {
        private val buffs = setOf(Poison::class.java,
                Cripple::class.java,
                Weakness::class.java,
                Bleeding::class.java,
                Drowsy::class.java,
                Slow::class.java,
                Vertigo::class.java,
                Vulnerable::class.java,
                Drunk::class.java)
    }
}