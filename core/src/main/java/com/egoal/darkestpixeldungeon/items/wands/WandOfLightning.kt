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
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Lightning
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Shocking
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.traps.LightningTrap
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.effects.particles.SparkParticle
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.BArray
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.utils.Callback
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.ArrayList

class WandOfLightning : DamageWand(isMissile = false) {

    private val affected = ArrayList<Char>()

    internal var arcs = ArrayList<Lightning.Arc>()

    init {
        image = ItemSpriteSheet.WAND_LIGHTNING
    }

    override fun min(lvl: Int): Int = 6 + lvl

    override fun max(lvl: Int): Int = 12 + lvl * 11 / 2

    override fun giveDamage(enemy: Char): Damage =
            super.giveDamage(enemy).convertToElement(Damage.Element.Light).addFeature(Damage.Feature.ACCURATE)

    override fun onZap(bolt: Ballistica) {

        //lightning deals less damage per-targetpos, the more targets that are hit.
        var multipler = 0.4f + 0.6f / affected.size
        //if the main targetpos is in water, all affected take full damage
        if (Level.water[bolt.collisionPos]) multipler = 1f

        for (ch in affected) {
            val dmg = giveDamage(ch)
            dmg.value = Math.round(dmg.value * multipler)

            processWandDamage(dmg)

            // note the damage is accurate 
            if (ch === Dungeon.hero) Camera.main.shake(2f, 0.3f)
            ch.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3)
            ch.sprite.flash()
        }

        if (!curUser.isAlive) {
            Dungeon.fail(javaClass)
            GLog.n(Messages.get(this, "ondeath"))
        }
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        //acts like shocking enchantment
        Shocking().proc(staff, damage)
    }

    private fun arc(ch: Char) {

        affected.add(ch)

        val dist: Int
        if (Level.water[ch.pos] && !ch.flying)
            dist = 2
        else
            dist = 1

        PathFinder.buildDistanceMap(ch.pos, BArray.not(Level.solid, null), dist)
        for (i in PathFinder.distance.indices) {
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                val n = Actor.findChar(i)
                if (n === Dungeon.hero && PathFinder.distance[i] > 1)
                //the hero is only zapped if they are adjacent
                    continue
                else if (n != null && !affected.contains(n)) {
                    arcs.add(Lightning.Arc(ch.pos, n.pos))
                    arc(n)
                }
            }
        }
    }

    override fun fx(bolt: Ballistica, callback: Callback) {

        affected.clear()
        arcs.clear()
        arcs.add(Lightning.Arc(bolt.sourcePos, bolt.collisionPos))

        val cell = bolt.collisionPos

        val ch = Actor.findChar(cell)
        if (ch != null) {
            arc(ch)
        } else {
            CellEmitter.center(cell).burst(SparkParticle.FACTORY, 3)
        }

        //don't want to wait for the effect before processing damage.
        curUser.sprite.parent.add(Lightning(arcs, null))
        callback.call()
    }

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(0xFFFFFF)
        particle.am = 0.6f
        particle.setLifespan(0.6f)
        particle.acc.set(0f, +10f)
        particle.speed.polar(-Random.Float(3.1415926f), 6f)
        particle.setSize(0f, 1.5f)
        particle.sizeJitter = 1f
        particle.shuffleXY(2f)
        val dst = Random.Float(2f)
        particle.x -= dst
        particle.y += dst
    }

}
