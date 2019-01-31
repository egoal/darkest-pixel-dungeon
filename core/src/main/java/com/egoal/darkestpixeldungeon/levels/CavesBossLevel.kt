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

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Bones
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.levels.painters.Painter
import com.egoal.darkestpixeldungeon.levels.traps.ToxicTrap
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.Camera
import com.watabou.noosa.Group
import com.watabou.noosa.audio.Music
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class CavesBossLevel : Level() {

    private var arenaDoor: Int = 0
    private var enteredArena = false
    private var keyDropped = false

    init {
        color1 = 0x534f3e
        color2 = 0xb9d661
    }

    override fun tilesTex(): String = Assets.TILES_CAVES

    override fun waterTex(): String = Assets.WATER_CAVES

    override fun trackMusic(): String = if (enteredArena && !keyDropped)
        Assets.TRACK_BOSS_LOOP else Assets.TRACK_CHAPTER_3

    override fun setupSize() {
        if (width == 0 || height == 0) {
            height = 32
            width = height
        }
        length = width * height
    }

    override fun build(iterations: Int): Boolean {

        var topMost = Integer.MAX_VALUE

        for (i in 0..7) {
            val left: Int
            val right: Int
            val top: Int
            val bottom: Int
            if (Random.Int(2) == 0) {
                left = Random.Int(1, ROOM_LEFT - 3)
                right = ROOM_RIGHT + 3
            } else {
                left = ROOM_LEFT - 3
                right = Random.Int(ROOM_RIGHT + 3, width() - 1)
            }
            if (Random.Int(2) == 0) {
                top = Random.Int(2, ROOM_TOP - 3)
                bottom = ROOM_BOTTOM + 3
            } else {
                top = ROOM_LEFT - 3
                bottom = Random.Int(ROOM_TOP + 3, height() - 1)
            }

            Painter.fill(this, left, top, right - left + 1, bottom - top + 1,
                    Terrain.EMPTY)

            if (top < topMost) {
                topMost = top
                exit = Random.Int(left, right) + (top - 1) * width()
            }
        }

        map[exit] = Terrain.LOCKED_EXIT

        Painter.fill(this, ROOM_LEFT - 1, ROOM_TOP - 1,
                ROOM_RIGHT - ROOM_LEFT + 3, ROOM_BOTTOM - ROOM_TOP + 3, Terrain
                .WALL)
        Painter.fill(this, ROOM_LEFT, ROOM_TOP + 1,
                ROOM_RIGHT - ROOM_LEFT + 1, ROOM_BOTTOM - ROOM_TOP, Terrain.EMPTY)

        Painter.fill(this, ROOM_LEFT, ROOM_TOP,
                ROOM_RIGHT - ROOM_LEFT + 1, 1, Terrain.EMPTY_DECO)

        arenaDoor = Random.Int(ROOM_LEFT, ROOM_RIGHT) + (ROOM_BOTTOM + 1) * width()
        map[arenaDoor] = Terrain.DOOR

        entrance = Random.Int(ROOM_LEFT + 1, ROOM_RIGHT - 1) + Random.Int(ROOM_TOP + 1, ROOM_BOTTOM - 1) * width()
        map[entrance] = Terrain.ENTRANCE

        val patch = Patch.generate(this, 0.45f, 6)
        for (i in 0 until length()) {
            if (map[i] == Terrain.EMPTY && patch[i]) {
                map[i] = Terrain.WATER
            }
        }

        for (i in 0 until length()) {
            if (map[i] == Terrain.EMPTY && Random.Int(6) == 0) {
                map[i] = Terrain.INACTIVE_TRAP
                val t = ToxicTrap().reveal()
                t.active = false
                setTrap(t, i)
            }
        }

        return true
    }

    override fun decorate() {

        for (i in width() + 1 until length() - width()) {
            if (map[i] == Terrain.EMPTY) {
                var n = 0
                if (map[i + 1] == Terrain.WALL) {
                    n++
                }
                if (map[i - 1] == Terrain.WALL) {
                    n++
                }
                if (map[i + width()] == Terrain.WALL) {
                    n++
                }
                if (map[i - width()] == Terrain.WALL) {
                    n++
                }
                if (Random.Int(8) <= n) {
                    map[i] = Terrain.EMPTY_DECO
                }
            }
        }

        for (i in 0 until length()) {
            if (map[i] == Terrain.WALL && Random.Int(8) == 0) {
                map[i] = Terrain.WALL_DECO
            }
        }

        var sign: Int
        do {
            sign = Random.Int(ROOM_LEFT, ROOM_RIGHT) + Random.Int(ROOM_TOP,
                    ROOM_BOTTOM) * width()
        } while (sign == entrance || map[sign] == Terrain.INACTIVE_TRAP)
        map[sign] = Terrain.SIGN
    }

    override fun createMobs() {}

    override fun respawner(): Actor? = null

    override fun createItems() {
        val item = Bones.get()
        if (item != null) {
            var pos: Int
            do {
                pos = Random.IntRange(ROOM_LEFT, ROOM_RIGHT) + Random.IntRange(ROOM_TOP + 1, ROOM_BOTTOM) * width()
            } while (pos == entrance || map[pos] == Terrain.SIGN)
            drop(item, pos).type = Heap.Type.REMAINS
        }
    }

    override fun randomRespawnCell(): Int {
        var cell = entrance + PathFinder.NEIGHBOURS8[Random.Int(8)]
        while (!Level.passable[cell]) {
            cell = entrance + PathFinder.NEIGHBOURS8[Random.Int(8)]
        }
        return cell
    }

    override fun press(cell: Int, hero: Char?) {

        super.press(cell, hero)

        if (!enteredArena && outsideEntranceRoom(cell) && hero === Dungeon.hero) {
            enteredArena = true
            seal()

            val boss = Bestiary.mob(Dungeon.depth).apply {
                state = WANDERING
                do {
                    pos = Random.Int(length())
                } while (!Level.passable[pos] || !outsideEntranceRoom(pos) || Dungeon.visible[pos])
            }
            GameScene.add(boss)

            Level.set(arenaDoor, Terrain.WALL)
            GameScene.updateMap(arenaDoor)
            Dungeon.observe()

            CellEmitter.get(arenaDoor).start(Speck.factory(Speck.ROCK), 0.07f, 10)
            Camera.main.shake(3f, 0.7f)
            Sample.INSTANCE.play(Assets.SND_ROCKS)

            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }
    }

    override fun drop(item: Item, cell: Int): Heap {

        if (!keyDropped && item is SkeletonKey) {

            keyDropped = true
            unseal()

            CellEmitter.get(arenaDoor).start(Speck.factory(Speck.ROCK), 0.07f, 10)

            Level.set(arenaDoor, Terrain.EMPTY_DECO)
            GameScene.updateMap(arenaDoor)
            Dungeon.observe()

            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }

        return super.drop(item, cell)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(DOOR, arenaDoor)
        bundle.put(ENTERED, enteredArena)
        bundle.put(DROPPED, keyDropped)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        arenaDoor = bundle.getInt(DOOR)
        enteredArena = bundle.getBoolean(ENTERED)
        keyDropped = bundle.getBoolean(DROPPED)
    }


    private fun outsideEntranceRoom(cell: Int): Boolean {
        val cx = cell % width()
        val cy = cell / width()
        return cx < ROOM_LEFT - 1 || cx > ROOM_RIGHT + 1 || cy < ROOM_TOP - 1 ||
                cy > ROOM_BOTTOM + 1
    }

    override fun tileName(tile: Int): String = when (tile) {
        Terrain.GRASS -> Messages.get(CavesLevel::class.java, "grass_name")
        Terrain.HIGH_GRASS -> Messages.get(CavesLevel::class.java, "high_grass_name")
        Terrain.WATER -> Messages.get(CavesLevel::class.java, "water_name")
        else -> super.tileName(tile)
    }


    override fun tileDesc(tile: Int): String = when (tile) {
        Terrain.ENTRANCE -> Messages.get(CavesLevel::class.java, "entrance_desc")
        Terrain.EXIT -> Messages.get(CavesLevel::class.java, "exit_desc")
        Terrain.HIGH_GRASS -> Messages.get(CavesLevel::class.java, "high_grass_desc")
        Terrain.WALL_DECO -> Messages.get(CavesLevel::class.java, "wall_deco_desc")
        Terrain.BOOKSHELF -> Messages.get(CavesLevel::class.java, "bookshelf_desc")
        else -> super.tileDesc(tile)
    }

    override fun addVisuals(): Group {
        super.addVisuals()
        CavesLevel.AddCavesVisuals(this, visuals)
        return visuals
    }

    companion object {

        private val WIDTH = 32
        private val HEIGHT = 32

        private val ROOM_LEFT = WIDTH / 2 - 2
        private val ROOM_RIGHT = WIDTH / 2 + 2
        private val ROOM_TOP = HEIGHT / 2 - 2
        private val ROOM_BOTTOM = HEIGHT / 2 + 2

        private val DOOR = "door"
        private val ENTERED = "entered"
        private val DROPPED = "droppped"
    }
}
