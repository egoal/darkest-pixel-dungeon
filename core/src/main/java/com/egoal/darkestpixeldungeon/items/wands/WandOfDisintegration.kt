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

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.effects.Beam
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.PurpleParticle
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Callback
import com.watabou.utils.Random

import java.util.ArrayList
import kotlin.math.min

class WandOfDisintegration : DamageWand(isMissile = false) {
    init {
        image = ItemSpriteSheet.WAND_DISINTEGRATION

        collisionProperties = Ballistica.WONT_STOP
    }


    override fun min(lvl: Int): Int = 3 + lvl

    override fun max(lvl: Int): Int = 9 + lvl * 9 / 2

    override fun giveDamage(enemy: Char): Damage {
        return super.giveDamage(enemy).addFeature(Damage.Feature.PURE or Damage.Feature.ACCURATE)
    }

    override fun onZap(beam: Ballistica) {
        var terrainAffected = false

        val level = level()

        val maxDistance = min(distance(), beam.dist)

        val chars = ArrayList<Char>()

        var terrainPassed = 2
        var terrainBonus = 0
        for (c in beam.subPath(1, maxDistance)) {
            Actor.findChar(c)?.let {
                // we don't want to count passed terrain after the last enemy hit. That would be a lot of bonus levels.
                // terrainPassed starts at 2, equivalent of rounding up when /3 for integer arithmetic.
                terrainBonus += terrainPassed / 3
                terrainPassed %= 3

                chars.add(it)
            }

            if (Level.flamable[c]) {
                Dungeon.level.destroy(c)
                GameScene.updateMap(c)
                terrainAffected = true
            }

            if (Level.solid[c]) terrainPassed++

            CellEmitter.center(c).burst(PurpleParticle.BURST, Random.IntRange(1, 2))
        }

        if (terrainAffected) {
            Dungeon.observe()
        }

        val lvl = level + (chars.size - 1) + terrainBonus
        for (ch in chars) {
            val damage = Damage(damageRoll(lvl), curUser, ch).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE)
            processWandDamage(damage)
            ch.sprite.centerEmitter().burst(PurpleParticle.BURST, Random.IntRange(1, 2))
            ch.sprite.flash()
        }
        if (chars.count { !it.isAlive } >= 3) Badges.validate_WandOfDisintergration()
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        //no direct effect, see magesStaff.reachfactor
    }

    private fun distance(): Int = level() * 2 + 4

    override fun fx(beam: Ballistica, callback: Callback) {

        val cell = beam.path[Math.min(beam.dist, distance())]
        curUser.sprite.parent.add(Beam.DeathRay(
                DungeonTilemap.tileCenterToWorld(beam.sourcePos),
                DungeonTilemap.tileCenterToWorld(cell)))
        callback.call()
    }

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(0x220022)
        particle.am = 0.6f
        particle.setLifespan(0.6f)
        particle.acc.set(40f, -40f)
        particle.setSize(0f, 3f)
        particle.shuffleXY(2f)
    }

}
