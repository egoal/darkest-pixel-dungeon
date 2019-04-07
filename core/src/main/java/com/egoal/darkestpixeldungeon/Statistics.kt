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
package com.egoal.darkestpixeldungeon

import com.egoal.darkestpixeldungeon.actors.buffs.MoonNight
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle

object Statistics {

    var GoldCollected: Int = 0
    var DeepestFloor: Int = 0
    var EnemiesSlain: Int = 0
    var FoodEaten: Int = 0
    var PotionsCooked: Int = 0
    var PiranhasKilled: Int = 0
    var NightHunt: Int = 0
    var AnkhsUsed: Int = 0

    var Duration: Float = 0.toFloat()

    var QualifiedForNoKilling = false
    var CompletedWithNoKilling = false

    var AmuletObtained = false

    val Clock = ClockTime()

    init {
        Clock.set(0, 9, 0f)
    }

    private val GOLD = "score"
    private val DEEPEST = "maxDepth"
    private val SLAIN = "enemiesSlain"
    private val FOOD = "foodEaten"
    private val ALCHEMY = "potionsCooked"
    private val PIRANHAS = "priranhas"
    private val NIGHT = "nightHunt"
    private val ANKHS = "ankhsUsed"
    private val DURATION = "duration"
    private val AMULET = "amuletObtained"
    private val TOTAL_MINUTES = "total-minutes"

    fun reset() {

        GoldCollected = 0
        DeepestFloor = Dungeon.initialDepth_
        EnemiesSlain = 0
        FoodEaten = 0
        PotionsCooked = 0
        PiranhasKilled = 0
        NightHunt = 0
        AnkhsUsed = 0

        Duration = 0f
        Clock.set(0, 9, 0f)

        QualifiedForNoKilling = false

        AmuletObtained = false
    }

    fun storeInBundle(bundle: Bundle) {
        bundle.put(GOLD, GoldCollected)
        bundle.put(DEEPEST, DeepestFloor)
        bundle.put(SLAIN, EnemiesSlain)
        bundle.put(FOOD, FoodEaten)
        bundle.put(ALCHEMY, PotionsCooked)
        bundle.put(PIRANHAS, PiranhasKilled)
        bundle.put(NIGHT, NightHunt)
        bundle.put(ANKHS, AnkhsUsed)
        bundle.put(DURATION, Duration)
        bundle.put(AMULET, AmuletObtained)
        bundle.put(TOTAL_MINUTES, Clock.totalMinutes)
    }

    fun restoreFromBundle(bundle: Bundle) {
        GoldCollected = bundle.getInt(GOLD)
        DeepestFloor = bundle.getInt(DEEPEST)
        EnemiesSlain = bundle.getInt(SLAIN)
        FoodEaten = bundle.getInt(FOOD)
        PotionsCooked = bundle.getInt(ALCHEMY)
        PiranhasKilled = bundle.getInt(PIRANHAS)
        NightHunt = bundle.getInt(NIGHT)
        AnkhsUsed = bundle.getInt(ANKHS)
        Duration = bundle.getFloat(DURATION)
        AmuletObtained = bundle.getBoolean(AMULET)

        Clock.totalMinutes = bundle.getFloat(TOTAL_MINUTES)
        Clock.updateState()
    }

    class ClockTime {
        var totalMinutes = 0f
        var state = State.Day

        val day: Int get() = totalMinutes.toInt() / 60 / 24
        val hour: Int get() = totalMinutes.toInt() / 60 % 24
        val minute: Int get() = totalMinutes.toInt() % 60

        val timestr: String get() = "%02d:%02d".format(hour, minute)

        enum class State {
            Day, Night, MidNight
        }

        fun spend(minutes: Float) {
            totalMinutes += minutes
            updateState()
        }

        fun set(day: Int = 0, hour: Int, minutes: Float) {
            totalMinutes = day * 60 * 24 + hour * 60 + minutes
            updateState()
        }

        // special time point would indicate the player
        val special: Boolean get() = minute in 0..20 && hour in listOf(2, 7, 19, 22)

        fun updateState() {
            var newState = when {
                hour in 7 until 19 -> State.Day
                hour < 2 || hour >= 22 -> State.MidNight
                else -> State.Night
            }
            
            // night control
            if(newState==State.Day && (Dungeon.hero?.buff(MoonNight::class.java)!=null ||
                            Dungeon.depth> 20|| Dungeon.isChallenged(Challenges.THE_LONG_NIGHT)))
                newState = State.Night

            if (newState != state) {
                // state changed.
                state = newState

                val info = when (state) {
                    State.Day -> Messages.get(Hero::class.java, "clock-day")
                    State.Night -> Messages.get(Hero::class.java, "clock-night")
                    State.MidNight -> Messages.get(Hero::class.java, "clock-midnight")
                }
                GLog.w(info)
            }
        }
    }

}
