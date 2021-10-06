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
package com.egoal.darkestpixeldungeon.items.armor.glyphs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle

class Camouflage : Armor.Glyph() {

    override fun proc(armor: Armor, damage: Damage): Damage {
        //no proc effect, see HighGrass.trample
        return damage
    }

    override fun glowing(): ItemSprite.Glowing {
        return GREEN
    }

    class Camo : Invisibility() {
        private var pos: Int = 0
        private var left: Int = 0

        override fun act(): Boolean {
            left--
            if (left == 0 || target.pos != pos) {
                detach()
            } else {
                spend(Actor.TICK)
            }
            return true
        }

        fun set(time: Int) {
            left = time
            pos = target.pos
            Sample.INSTANCE.play(Assets.SND_MELD)
        }

        override fun toString(): String {
            return Messages.get(this, "name")
        }

        override fun desc(): String {
            return Messages.get(this, "desc", dispTurns(left.toFloat()))
        }

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put(POS, pos)
            bundle.put(LEFT, left)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            pos = bundle.getInt(POS)
            left = bundle.getInt(LEFT)
        }

        companion object {

            private const val POS = "pos"
            private const val LEFT = "left"
        }
    }

    companion object {

        private val GREEN = ItemSprite.Glowing(0x448822)
    }

}

