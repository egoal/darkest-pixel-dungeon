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
package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.PoisonParticle
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSprite.Glowing
import com.watabou.utils.Random

class Venomous : Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon)

        if (Random.Float() < 0.5f) {
            val defender = damage.to as Char

            Buff.affect(defender, Poison::class.java).extend(Poison.durationFactor(defender) * 2)
            CellEmitter.center(defender.pos).burst(PoisonParticle.SPLASH, 5)
        }

        return damage.setAdditionalDamage(Damage.Element.POISON, Random.Int(2, damage.value / 5))
    }

    override fun glowing(): Glowing = PURPLE

    companion object {

        private val PURPLE = ItemSprite.Glowing(0x4400AA)
    }
}
