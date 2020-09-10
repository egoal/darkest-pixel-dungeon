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
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Blazing
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.PathFinder

import java.util.HashSet

class WandOfFireblast : DamageWand() {

    //the actual affected cells
    private var affectedCells: HashSet<Int>? = null
    //the cells to trace fire shots to, for visual effects.
    private var visualCells: HashSet<Int>? = null
    private var direction = 0

    init {
        image = ItemSpriteSheet.WAND_FIREBOLT

        collisionProperties = Ballistica.STOP_TERRAIN
    }

    //1x/1.5x/2.25x damage
    override fun min(lvl: Int): Int {
        return Math.round((1 + lvl) * Math.pow(1.5, (chargesPerCast() - 1).toDouble())).toInt()
    }

    //1x/1.5x/2.25x damage
    override fun max(lvl: Int): Int {
        return Math.round((7 + 3 * lvl) * Math.pow(1.5, (chargesPerCast() - 1).toDouble())).toInt()
    }

    override fun giveDamage(enemy: Char): Damage {
        return super.giveDamage(enemy).addElement(Damage.Element.FIRE)
    }

    override fun onZap(bolt: Ballistica) {

        for (cell in affectedCells!!) {

            if (Level.flamable[cell] || !Dungeon.level.adjacent(bolt.sourcePos, cell))
                GameScene.add(Blob.seed(cell, 1 + chargesPerCast(), Fire::class.java))
            val ch = Actor.findChar(cell)
            if (ch != null) {

                ch.takeDamage(giveDamage(ch))
                Buff.affect(ch, Burning::class.java).reignite(ch)
                when (chargesPerCast()) {
                    1 -> {
                    }
                    2 -> Buff.affect(ch, Cripple::class.java, 4f)
                    3 -> Buff.affect(ch, Paralysis::class.java, 4f)
                }//no effects
            }
        }
    }

    //burn... BURNNNNN!.....
    private fun spreadFlames(cell: Int, strength: Float) {
        if (strength >= 0 && Level.passable[cell]) {
            affectedCells!!.add(cell)
            if (strength >= 1.5f) {
                visualCells!!.remove(cell)
                spreadFlames(cell + PathFinder.CIRCLE[left(direction)], strength - 1.5f)
                spreadFlames(cell + PathFinder.CIRCLE[direction], strength - 1.5f)
                spreadFlames(cell + PathFinder.CIRCLE[right(direction)], strength - 1.5f)
            } else {
                visualCells!!.add(cell)
            }
        } else if (!Level.passable[cell])
            visualCells!!.add(cell)
    }

    private fun left(direction: Int): Int {
        return if (direction == 0) 7 else direction - 1
    }

    private fun right(direction: Int): Int {
        return if (direction == 7) 0 else direction + 1
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        //acts like blazing enchantment
        Blazing().proc(staff, damage)
    }

    override fun fx(bolt: Ballistica, callback: Callback) {
        //need to perform flame spread logic here so we can determine what cells
        // to put flames in.
        affectedCells = HashSet()
        visualCells = HashSet()

        // 4/6/9 distance
        val maxDist = (4 * Math.pow(1.5, (chargesPerCast() - 1).toDouble())).toInt()
        val dist = Math.min(bolt.dist, maxDist)

        for (i in PathFinder.CIRCLE.indices) {
            if (bolt.sourcePos + PathFinder.CIRCLE[i] == bolt.path[1]) {
                direction = i
                break
            }
        }

        var strength = maxDist.toFloat()
        for (c in bolt.subPath(1, dist)) {
            strength-- //as we start at dist 1, not 0.
            affectedCells!!.add(c)
            if (strength > 1) {
                spreadFlames(c + PathFinder.CIRCLE[left(direction)], strength - 1)
                spreadFlames(c + PathFinder.CIRCLE[direction], strength - 1)
                spreadFlames(c + PathFinder.CIRCLE[right(direction)], strength - 1)
            } else {
                visualCells!!.add(c)
            }
        }

        //going to call this one manually
        visualCells!!.remove(bolt.path[dist])

        for (cell in visualCells!!) {
            //this way we only get the cells at the tip, much better performance.
            MagicMissile.fire(curUser.sprite.parent, bolt.sourcePos, cell, null)
        }
        MagicMissile.fire(curUser.sprite.parent, bolt.sourcePos, bolt.path[dist], callback)
        Sample.INSTANCE.play(Assets.SND_ZAP)
    }

    override fun chargesPerCast(): Int {
        //consumes 30% of current charges, rounded up, with a minimum of one.
        return Math.max(1, Math.ceil((curCharges * 0.3f).toDouble()).toInt())
    }

    override fun statsDesc(): String {
        return if (levelKnown)
            Messages.get(this, "stats_desc", chargesPerCast(), min(), max())
        else
            Messages.get(this, "stats_desc", chargesPerCast(), min(0), max(0))
    }

    override fun particleColor(): Int {
        return 0xEE7722
    }

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(0xEE7722)
        particle.am = 0.5f
        particle.setLifespan(0.6f)
        particle.acc.set(0f, -40f)
        particle.setSize(0f, 3f)
        particle.shuffleXY(2f)
    }

}
