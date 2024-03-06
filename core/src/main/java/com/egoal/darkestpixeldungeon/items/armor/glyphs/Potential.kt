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

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.effects.Lightning
import com.egoal.darkestpixeldungeon.levels.traps.LightningTrap
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSprite.Glowing
import com.watabou.noosa.Camera
import com.watabou.utils.Random

class Potential : Armor.Glyph() {

    override fun proc(armor: Armor, damage: Damage): Damage {
        val attacker = damage.from as Char
        val defender = damage.to as Char

        val level = Math.max(0, armor.level())

        if (Random.Int(level + 20) >= 18) {
            var shockDmg = Random.NormalIntRange(defender.HT / 20, defender.HT / 10)

            shockDmg *= Math.pow(0.9, level.toDouble()).toInt()

            defender.takeDamage(Damage(shockDmg, this, defender).convertToElement(Damage.Element.Light))

            checkOwner(defender)
            if (defender === Dungeon.hero) {
                Dungeon.hero.belongings.charge(1f)
                Camera.main.shake(2f, 0.3f)
            }

            attacker.sprite.parent.add(Lightning(attacker.pos, defender.pos, null))

        }

        return damage
    }

    override fun glowing(): Glowing = WHITE

    companion object {
        private val WHITE = Glowing(0xFFFFFF, 0.6f)
    }
}
