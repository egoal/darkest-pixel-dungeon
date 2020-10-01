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

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.Regrowth
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.unclassified.Dewdrop
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.plants.BlandfruitBush
import com.egoal.darkestpixeldungeon.plants.Starflower
import com.egoal.darkestpixeldungeon.plants.Sungrass
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.ColorMath
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.ArrayList
import java.util.HashSet
import kotlin.math.max

class WandOfRegrowth : DamageWand.NoDamage(isMissile = false) {
    //the actual affected cells
    private var affectedCells: HashSet<Int>? = null
    //the cells to trace growth particles to, for visual effects.
    private var visualCells: HashSet<Int>? = null
    private var direction = 0

    init {
        image = ItemSpriteSheet.WAND_REGROWTH

        collisionProperties = Ballistica.STOP_TERRAIN
    }

    override fun onZap(bolt: Ballistica) {
        //ignore tiles which can't have anything grow in them.
        val i = affectedCells!!.iterator()
        while (i.hasNext()) {
            val c = Dungeon.level.map[i.next()]
            if (!(c == Terrain.EMPTY ||
                            c == Terrain.EMBERS ||
                            c == Terrain.EMPTY_DECO ||
                            c == Terrain.GRASS ||
                            c == Terrain.HIGH_GRASS)) {
                i.remove()
            }
        }

        val numPlants: Float
        val numDews: Float
        val numPods: Float
        val numStars: Float

        val chrgUsed = chargesPerCast()
        //numbers greater than n*100% means n guaranteed plants, e.g. 210% = 2
        // plants w/10% chance for 3 plants.
        numPlants = 0.2f + chrgUsed.toFloat() * chrgUsed.toFloat() * 0.020f //scales from 22% to 220%
        numDews = 0.05f + chrgUsed.toFloat() * chrgUsed.toFloat() * 0.016f //scales from 6.6% to 165%
        numPods = 0.02f + chrgUsed.toFloat() * chrgUsed.toFloat() * 0.013f //scales from 3.3% to 135%
        numStars = chrgUsed * chrgUsed * chrgUsed / 5f * 0.005f //scales from
        // 0.1% to 100%
        placePlants(numPlants, numDews, numPods, numStars)

        for (i in affectedCells!!) {
            val c = Dungeon.level.map[i]
            if (c == Terrain.EMPTY ||
                    c == Terrain.EMBERS ||
                    c == Terrain.EMPTY_DECO) {
                Level[i] = Terrain.GRASS
            }

            val ch = Actor.findChar(i)

            GameScene.add(Blob.seed(i, 10, Regrowth::class.java))
        }
    }

    private fun spreadRegrowth(cell: Int, strength: Float) {
        if (strength >= 0 && Level.passable[cell] && !Level.losBlocking[cell]) {
            affectedCells!!.add(cell)
            if (strength >= 1.5f) {
                spreadRegrowth(cell + PathFinder.CIRCLE[left(direction)], strength - 1.5f)
                spreadRegrowth(cell + PathFinder.CIRCLE[direction], strength - 1.5f)
                spreadRegrowth(cell + PathFinder.CIRCLE[right(direction)], strength - 1.5f)
            } else {
                visualCells!!.add(cell)
            }
        } else if (!Level.passable[cell] || Level.losBlocking[cell])
            visualCells!!.add(cell)
    }

    private fun placePlants(numPlants: Float, numDews: Float, numPods: Float,
                            numStars: Float) {
        var numPlants = numPlants
        var numDews = numDews
        var numPods = numPods
        var numStars = numStars
        val cells = affectedCells!!.iterator()
        val floor = Dungeon.level

        while (cells.hasNext() && Random.Float() <= numPlants) {
            val seed = Generator.SEED.generate() as Plant.Seed

            if (seed is BlandfruitBush.Seed) {
                if (Random.Int(15) - Dungeon.limitedDrops.blandfruitSeed.count >= 0) {
                    floor.plant(seed, cells.next())
                    Dungeon.limitedDrops.blandfruitSeed.count++
                }
            } else
                floor.plant(seed, cells.next())

            numPlants--
        }

        while (cells.hasNext() && Random.Float() <= numDews) {
            floor.plant(Dewcatcher.Seed(), cells.next())
            numDews--
        }

        while (cells.hasNext() && Random.Float() <= numPods) {
            floor.plant(Seedpod.Seed(), cells.next())
            numPods--
        }

        while (cells.hasNext() && Random.Float() <= numStars) {
            floor.plant(Starflower.Seed(), cells.next())
            numStars--
        }

    }

    private fun left(direction: Int): Int {
        return if (direction == 0) 7 else direction - 1
    }

    private fun right(direction: Int): Int {
        return if (direction == 7) 0 else direction + 1
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        //like pre-nerf vampiric enchantment, except with herbal healing buff

        val level = Math.max(0, staff.level())

        // lvl 0 - 33%
        // lvl 1 - 43%
        // lvl 2 - 50%
        val maxValue = damage.value * (level + 2) / (level + 6)

        val attacker = damage.from as Char
        val effValue = Math.min(Random.IntRange(0, maxValue),
                attacker.HT - attacker.HP)

        Buff.affect(attacker, Sungrass.Health::class.java).boost(effValue)

    }

    override fun fx(bolt: Ballistica, callback: Callback) {

        affectedCells = HashSet()
        visualCells = HashSet()

        val maxDist = Math.round(1.2f + chargesPerCast() * .8f)
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
            if (!Level.losBlocking[c]) {
                affectedCells!!.add(c)
                spreadRegrowth(c + PathFinder.CIRCLE[left(direction)], strength - 1)
                spreadRegrowth(c + PathFinder.CIRCLE[direction], strength - 1)
                spreadRegrowth(c + PathFinder.CIRCLE[right(direction)], strength - 1)
            } else {
                visualCells!!.add(c)
            }
        }

        //going to call this one manually
        visualCells!!.remove(bolt.path[dist])

        for (cell in visualCells!!) {
            //this way we only get the cells at the tip, much better performance.
            MagicMissile.foliage(Item.curUser.sprite.parent, bolt.sourcePos, cell, null)
        }
        MagicMissile.foliage(Item.curUser.sprite.parent, bolt.sourcePos, bolt.path[dist], callback)

        Sample.INSTANCE.play(Assets.SND_ZAP)
    }

    override fun initialCharges(): Int = 1

    //consumes all available charges, needs at least one.
    override fun chargesPerCast(): Int = max(1, curCharges)

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(ColorMath.random(0x004400, 0x88CC44))
        particle.am = 1f
        particle.setLifespan(1.2f)
        particle.setSize(1f, 2f)
        particle.shuffleXY(1f)
        val dst = Random.Float(11f)
        particle.x -= dst
        particle.y += dst
    }

    class Dewcatcher : Plant(12) {

        override fun activate() {

            var nDrops = Random.NormalIntRange(2, 8)

            val candidates = PathFinder.NEIGHBOURS8.map { pos + it }.filter { Level.passable[it] }.shuffled()

            for (c in candidates) {
                Dungeon.level.drop(Dewdrop(), c).sprite.drop(pos)
                --nDrops
                if (nDrops == 0) break
            }
        }

        //seed is never dropped, only care about plant class
        class Seed : Plant.Seed(Dewcatcher::class.java, PotionOfHealing::class.java)
    }

    class Seedpod : Plant(13) {

        override fun activate() {

            var nSeeds = Random.NormalIntRange(1, 5)

            val candidates = PathFinder.NEIGHBOURS8.map { pos + it }.filter { Level.passable[it] }.shuffled()

            for (c in candidates) {
                Dungeon.level.drop(Generator.SEED.generate(), c).sprite.drop(pos)
                --nSeeds
                if (nSeeds == 0) break
            }
        }

        //seed is never dropped, only care about plant class
        class Seed : Plant.Seed(Seedpod::class.java, PotionOfHealing::class.java)
    }

}
