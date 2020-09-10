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
package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class MasterThievesArmband : Artifact() {
    init {
        image = ItemSpriteSheet.ARTIFACT_ARMBAND

        levelCap = 10

        charge = 0
    }

    override fun passiveBuff() = Thievery()

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero)) desc += "\n\n" + M.L(this, "desc_worn")

        return desc
    }

    inner class Thievery : Artifact.ArtifactBuff() {
        fun collect(gold: Int) {
            charge += gold / 2
        }

        override fun detach() {
            charge = (charge * 0.95).toInt()
            super.detach()
        }

        fun steal(value: Int): Boolean {
            if (value <= charge) {
                charge -= value
                exp += value
            } else {
                val chance = stealChance(value)
                if (Random.Float() > chance)
                    return false
                else {
                    if (chance <= 1)
                        charge = 0
                    else
                    //removes the charge it took you to reach 100%
                        charge -= (charge / chance).toInt()
                    exp += value
                }
            }
            while (exp >= 250 + 50 * level() && level() < levelCap) {
                exp -= 250 + 50 * level()
                upgrade()
            }
            return true
        }

        fun stealChance(value: Int): Float {
            //get lvl*50 gold or lvl*3.33% item value of free charge, whichever is less.
            val chargeBonus = Math.min(level() * 50, value * level() / 30)
            return (charge.toFloat() + chargeBonus) / value
        }
    }
}
