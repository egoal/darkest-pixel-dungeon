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
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

class Blazing : Enchantment() {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon)

        val defender = damage.to as Char


        defender.takeDamage(Damage(Random.Int(1, damage.value/10), this, defender)
                .type(Damage.Type.MAGICAL)
                .addElement(Damage.Element.FIRE))

        defender.sprite.emitter().burst(FlameParticle.FACTORY, 3)

        if (Random.Float() < 0.3f) Buff.affect(defender, Burning::class.java).reignite(defender)

        return damage.addElement(Damage.Element.FIRE)
    }

    override fun glowing(): ItemSprite.Glowing = ORANGE

    companion object {
        private val ORANGE = ItemSprite.Glowing(0xFF4400)
    }
}
