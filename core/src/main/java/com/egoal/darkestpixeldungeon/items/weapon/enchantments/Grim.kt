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

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.watabou.utils.Random

class Grim : Weapon.Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        val defender = damage.to as Char
        val attacker = damage.from as Char

        val level = Math.max(0, weapon.level())

        val enemyHealth = defender.HP - damage.value
        if (enemyHealth == 0)
            return damage //no point in proccing if they're already dead.

        //scales from 0 - 30% based on how low hp the enemy is, plus 1% per level
        val chance = Math.round((defender.HT - enemyHealth) / defender.HT.toFloat() * 30 + level)

        if (Random.Int(100) < chance) {
            defender.takeDamage(Damage(defender.HP, this, defender).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.DEATH).addElement(Damage.Element.SHADOW))
            defender.sprite.emitter().burst(ShadowParticle.UP, 5)

            if (!defender.isAlive && attacker is Hero)
                Badges.validateGrimWeapon()
        }

        return damage
    }

    override fun glowing(): ItemSprite.Glowing {
        return BLACK
    }

    companion object {

        private val BLACK = ItemSprite.Glowing(0x000000)
    }

}
