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
package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.Assets
import com.watabou.utils.Random

import java.util.Arrays

class DeadEndLevel : Level() {
    init {
        color1 = 0x534f3e
        color2 = 0xb9d661
    }

    override fun tilesTex(): String = Assets.TILES_CAVES

    override fun waterTex(): String = Assets.WATER_HALLS

    override fun build(iterations: Int): Boolean {

        Arrays.fill(map, Terrain.WALL)

        for (i in 2 until SIZE) {
            for (j in 2 until SIZE) {
                map[i * width() + j] = Terrain.EMPTY
            }
        }

        for (i in 1..SIZE) {
            map[width() * i + SIZE] = Terrain.WATER
            map[width() * i + 1] = map[width() * i + SIZE]
            map[width() * SIZE + i] = map[width() * i + 1]
            map[width() + i] = map[width() * SIZE + i]
        }

        entrance = SIZE * width() + SIZE / 2 + 1
        map[entrance] = Terrain.ENTRANCE

        map[(SIZE / 2 + 1) * (width() + 1)] = Terrain.SIGN

        exit = 0

        return true
    }

    override fun decorate() {
        for (i in 0 until length()) {
            if (map[i] == Terrain.EMPTY && Random.Int(10) == 0) {
                map[i] = Terrain.EMPTY_DECO
            } else if (map[i] == Terrain.WALL && Random.Int(8) == 0) {
                map[i] = Terrain.WALL_DECO
            }
        }
    }

    override fun createMobs() {}

    override fun respawner(): Actor? = null

    override fun createItems() {}

    override fun randomRespawnCell(): Int = entrance - width()

    companion object {

        private val SIZE = 5
    }

}
