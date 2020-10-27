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

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.VenomGas
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Venomous
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.PathFinder

class WandOfVenom : DamageWand(isMissile = true) {
    init {
        image = ItemSpriteSheet.WAND_VENOM

        collisionProperties = Ballistica.STOP_TARGET or Ballistica.STOP_TERRAIN
    }

    override fun min(lvl: Int): Int = 2 + lvl

    override fun max(lvl: Int): Int = 8 + lvl * 5 / 2

    override fun giveDamage(enemy: Char): Damage = super.giveDamage(enemy).addElement(Damage.Element.POISON)

    override fun onZap(bolt: Ballistica) {
        super.onZap(bolt)

        val venomGas = Blob.seed(bolt.collisionPos, 40 + 10 * level(), VenomGas::class.java)
        venomGas.setStrength(level() + 1)
        GameScene.add(venomGas)
    }

    override fun onHit(damage: Damage) {
        super.onHit(damage)
        val ch = damage.to as Char
        ch.sprite.burst(particleColor(), level() / 2 + 2)
    }

    override fun fx(bolt: Ballistica, callback: Callback) {
        MagicMissile.poison(curUser.sprite.parent, bolt.sourcePos, bolt.collisionPos, callback)
        Sample.INSTANCE.play(Assets.SND_ZAP)
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        //acts like venomous enchantment
        Venomous().proc(staff, damage)
    }

    override fun particleColor(): Int = 0x8844FF

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(particleColor())
        particle.am = 0.6f
        particle.setLifespan(0.6f)
        particle.acc.set(0f, 40f)
        particle.setSize(0f, 3f)
        particle.shuffleXY(2f)
    }

}
