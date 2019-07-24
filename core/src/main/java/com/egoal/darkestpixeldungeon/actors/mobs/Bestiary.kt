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
package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.watabou.utils.Random

object Bestiary {

    fun mob(depth: Int): Mob = mobClass(depth).newInstance()

    fun mutable(depth: Int): Mob? {
        //todo: rework this.
        if (depth <= 20 && Statistics.Clock.state != Statistics.ClockTime.State.Day && Random.Int(15) == 0) {
            return Glowworm(depth)
        }

        var cl = mobClass(depth)
        if (Random.Int(30) == 0) {
            cl = when (cl) {
                Rat::class.java -> Albino::class.java
                Thief::class.java -> Bandit::class.java
                Brute::class.java -> Shielded::class.java
                Monk::class.java -> Senior::class.java
                Scorpio::class.java -> Acidic::class.java

                else -> cl
            }
        }

        return cl.newInstance()
    }

    private fun mobClass(depth: Int): Class<out Mob> {
        // mobs can created in each depth
        val chances: FloatArray
        val classes: Array<Class<out Mob>>

        when (depth) {
            // sewer
            1 -> {
                chances = floatArrayOf(1f)
                classes = arrayOf(Rat::class.java)
            }
            2 -> {
                chances = floatArrayOf(1f, 1f)
                classes = arrayOf(Rat::class.java, Gnoll::class.java)
            }
            3 -> {
                chances = floatArrayOf(2f, 4f, 0.5f, 1f)
                classes = arrayOf(Rat::class.java, Gnoll::class.java, Crab::class.java, Swarm::class.java)
            }
            4 -> {
                chances = floatArrayOf(1f, 2f, 3f, 1f, 0.02f)
                classes = arrayOf(Rat::class.java, Gnoll::class.java, Crab::class.java, Swarm::class.java, MadMan::class.java)
            }
            5 -> {
                chances = floatArrayOf(1f)
                classes = arrayOf(Goo::class.java)
            }

            // prison
            6 -> {
                chances = floatArrayOf(2f, 1f, 0.5f, .5f)
                classes = arrayOf(Skeleton::class.java, Thief::class.java, Swarm::class.java, Shaman::class.java)
            }
            7 -> {
                chances = floatArrayOf(3f, 1f, 1f, 1f, .2f)
                classes = arrayOf(Skeleton::class.java, Shaman::class.java, Thief::class.java, Guard::class.java, MadMan::class.java)
            }
            8 -> {
                chances = floatArrayOf(3f, 2f, 2f, 1f, .5f, 0.02f)
                classes = arrayOf(Skeleton::class.java, Shaman::class.java, Guard::class.java, Thief::class.java, MadMan::class.java, Bat::class.java)
            }
            9 -> {
                chances = floatArrayOf(3f, 2f, 2f, 1f, 0.6f, 0.1f)
                classes = arrayOf(Skeleton::class.java, Guard::class.java, Shaman::class.java, Thief::class.java, Bat::class.java, SkeletonKnight::class.java)
            }
            10 -> {
                chances = floatArrayOf(1f)
                classes = arrayOf(Tengu::class.java)
            }

            // caves
            11 -> {
                chances = floatArrayOf(1.5f, .5f, 0.2f)
                classes = arrayOf(Bat::class.java, SkeletonKnight::class.java, Brute::class.java)
            }
            12 -> {
                chances = floatArrayOf(1f, 1f, .5f, .2f)
                classes = arrayOf(Bat::class.java, Brute::class.java, SkeletonKnight::class.java, MadMan::class.java)
            }
            13 -> {
                chances = floatArrayOf(1f, 1f, .2f, 3f, 1f, 0.25f, 0.02f, .02f)
                classes = arrayOf(Bat::class.java, SkeletonKnight::class.java, MadMan::class.java, Brute::class.java, Spinner::class.java, Ballista::class.java, Elemental::class.java, Monk::class.java)
            }
            14 -> {
                chances = floatArrayOf(1f, 1f, 3f, 4f, .75f, 0.02f, 0.01f)
                classes = arrayOf(Bat::class.java, SkeletonKnight::class.java, Brute::class.java, Spinner::class.java, Ballista::class.java, Elemental::class.java, Monk::class.java)
            }
            15 -> {
                chances = floatArrayOf(1f)
                classes = arrayOf(DM300::class.java)
            }

            // city
            16 -> {
                chances = floatArrayOf(1f, 1f, 1f, 0.2f)
                classes = arrayOf(Elemental::class.java, Warlock::class.java, Ballista::class.java, Monk::class.java)
            }
            17 -> {
                chances = floatArrayOf(1f, 1f, 1f, 1f, .25f)
                classes = arrayOf(Elemental::class.java, Monk::class.java, Warlock::class.java, Ballista::class.java, MadMan::class.java)
            }
            18 -> {
                chances = floatArrayOf(1f, 2f, 1f, 0.5f, 1f, .25f)
                classes = arrayOf(Elemental::class.java, Monk::class.java, Golem::class.java, Ballista::class.java, Warlock::class.java, MadMan::class.java)
            }
            19 -> {
                chances = floatArrayOf(1f, 2f, 3f, 0.5f, 1f, 0.02f)
                classes = arrayOf(Elemental::class.java, Monk::class.java, Golem::class.java, Ballista::class.java, Warlock::class.java, Succubus::class.java)
            }
            20 -> {
                chances = floatArrayOf(1f)
                classes = arrayOf(King::class.java)
            }

            // halls
            22 -> {
                chances = floatArrayOf(1f, 1f, .1f)
                classes = arrayOf(Succubus::class.java, Eye::class.java, MadMan::class.java)
            }
            23 -> {
                chances = floatArrayOf(1f, 2f, 1f, .1f)
                classes = arrayOf(Succubus::class.java, Eye::class.java, Scorpio::class.java, MadMan::class.java)
            }
            24 -> {
                chances = floatArrayOf(1f, 2f, 3f)
                classes = arrayOf(Succubus::class.java, Eye::class.java, Scorpio::class.java)
            }
            25 -> {
                chances = floatArrayOf(1f)
                classes = arrayOf(Yog::class.java)
            }

            //
            else -> {
                chances = floatArrayOf(1f)
                classes = arrayOf(Eye::class.java)
            }
        }

        return classes[Random.chances(chances)]
    }
}
