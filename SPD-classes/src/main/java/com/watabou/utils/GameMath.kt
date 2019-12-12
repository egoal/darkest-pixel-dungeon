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

package com.watabou.utils

import com.watabou.noosa.Game

object GameMath {

    fun speed(speed: Float, acc: Float): Float = speed + acc * Game.elapsed

    fun gate(min: Float, value: Float, max: Float): Float = when {
        value < min -> min
        value > max -> max
        else -> value
    }

    fun clamp(value: Int, min: Int, max: Int): Int = when {
        value < min -> min
        value > max -> max
        else -> value
    }

    fun clampf(value: Float, min: Float, max: Float): Float = when {
        value < min -> min
        value > max -> max
        else -> value
    }

//    fun <T> clamp(value: T, min: T, max: T): T = when {
//        value < min -> min
//        value > max -> max
//        else -> value
//    }

    fun Lerp(lambda: Float, from: Float, to: Float): Float = from * (1 - lambda) + to * lambda

    fun ProbabilityPlus(a: Float, b: Float): Float = 1f - (1 - a) * (1 - b)

    fun ProbabilityPlus(vararg ps: Float): Float = 1f - ps.fold(1f) { s, i -> s * (1 - i) }

}
