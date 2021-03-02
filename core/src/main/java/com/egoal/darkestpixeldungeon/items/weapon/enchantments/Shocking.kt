/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.traps.LightningTrap
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.effects.Lightning
import com.egoal.darkestpixeldungeon.effects.particles.SparkParticle
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.ArrayList
import java.util.HashSet

class Shocking : Enchantment() {

    private val affected = ArrayList<Char>()

    private val arcs = ArrayList<Lightning.Arc>()

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon)

        if (Random.Float() < 0.35f) {
            val defender = damage.to as Char
            val attacker = damage.from as Char

            affected.clear()
            affected.add(attacker)

            arcs.clear()
            arcs.add(Lightning.Arc(attacker.pos, defender.pos))
            hit(defender, Random.Int(1, damage.value / 3))

            attacker.sprite.parent.add(Lightning(arcs, null))

        }

        return damage.addElement(Damage.Element.LIGHT)

    }

    override fun glowing(): ItemSprite.Glowing = WHITE

    private fun hit(ch: Char, damage: Int) {

        if (damage < 1) {
            return
        }

        affected.add(ch)

        ch.takeDamage(Damage(if (Level.water[ch.pos] && !ch.flying)
            damage * 2
        else
            damage,
                this, ch).addElement(Damage.Element.LIGHT))

        ch.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3)
        ch.sprite.flash()

        val ns = HashSet<Char>()
        for (i in PathFinder.NEIGHBOURS8.indices) {
            val n = Actor.findChar(ch.pos + PathFinder.NEIGHBOURS8[i])
            if (n != null && !affected.contains(n)) {
                arcs.add(Lightning.Arc(ch.pos, n.pos))
                hit(n, Random.Int(damage / 2, damage))
            }
        }
    }

    companion object {

        private val WHITE = ItemSprite.Glowing(0xFFFFFF,
                0.6f)
    }
}
