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
package com.egoal.darkestpixeldungeon.items.weapon.curses

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

class Displacing : Weapon.Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        val defender = damage.to as Char
        val attacker = damage.from as Char

        if (Random.Int(12) == 0 && !defender.properties().contains(com.egoal.darkestpixeldungeon.actors.Char.Property
                        .IMMOVABLE)) {
            var count = 10
            var newPos: Int
            do {
                newPos = Dungeon.level.randomRespawnCell()
                if (count-- <= 0) {
                    break
                }
            } while (newPos == -1)

            if (newPos != -1 && !Dungeon.bossLevel()) {

                if (Dungeon.visible[defender.pos]) {
                    CellEmitter.get(defender.pos).start(Speck.factory(Speck.LIGHT),
                            0.2f, 3)
                }

                defender.pos = newPos
                if (defender is Mob && defender.state === defender.HUNTING)
                    defender.state = defender.WANDERING

                defender.sprite.place(defender.pos)
                defender.sprite.visible = Dungeon.visible[defender.pos]

                damage.value = 0
                return damage

            }
        }

        return damage
    }

    override fun curse(): Boolean {
        return true
    }

    override fun glowing(): ItemSprite.Glowing {
        return BLACK
    }

    companion object {

        private val BLACK = ItemSprite.Glowing(0x000000)
    }

}
