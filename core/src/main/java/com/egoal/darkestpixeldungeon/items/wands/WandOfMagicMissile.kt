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
package com.egoal.darkestpixeldungeon.items.wands

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Recharging
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.effects.SpellSprite
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class WandOfMagicMissile : DamageWand() {
    init {
        image = ItemSpriteSheet.WAND_MAGIC_MISSILE
    }

    override fun min(lvl: Int): Int = 2 + lvl

    override fun max(lvl: Int): Int = 8 + 2 * lvl

    override fun onZap(bolt: Ballistica) {
        Actor.findChar(bolt.collisionPos)?.let { ch ->
            ch.takeDamage(Damage(damageRoll(), Item.curUser, ch).type(Damage.Type.MAGICAL))

            ch.sprite.burst(-0x1, level() / 2 + 2)
        }
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        val attacker = damage.from as Char

        Buff.prolong(attacker, Recharging::class.java, 1 + staff.level() / 2f)
        SpellSprite.show(attacker, SpellSprite.CHARGE)
    }

    override fun initialCharges(): Int = 3
}
