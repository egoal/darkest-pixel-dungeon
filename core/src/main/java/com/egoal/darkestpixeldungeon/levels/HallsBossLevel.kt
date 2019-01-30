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
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.ViewMark
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Bones
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.mobs.Yog
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.painters.Painter
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.Group
import com.watabou.noosa.audio.Music
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class HallsBossLevel : Level() {
    init {
        color1 = 0x801500
        color2 = 0xa68521
    }

    private var stairs = -1
    private var enteredArena = false
    private var keyDropped = false

    override fun tilesTex(): String? {
        return Assets.TILES_HALLS
    }

    override fun waterTex(): String? {
        return Assets.WATER_HALLS
    }

    override fun trackMusic(): String {
        return if (enteredArena && !keyDropped)
            Assets.TRACK_FINAL_INTRO
        else
            Assets.TRACK_CHAPTER_5
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(STAIRS, stairs)
        bundle.put(ENTERED, enteredArena)
        bundle.put(DROPPED, keyDropped)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        stairs = bundle.getInt(STAIRS)
        enteredArena = bundle.getBoolean(ENTERED)
        keyDropped = bundle.getBoolean(DROPPED)
    }

    override fun setupSize() {
        if (width == 0 || height == 0) {
            width = WIDTH
            height = HEIGHT
        }

        length = width * height
    }

    override fun build(iterations: Int): Boolean {

        for (i in 0..4) {

            val top = Random.IntRange(2, ROOM_TOP - 1)
            val bottom = Random.IntRange(ROOM_BOTTOM + 1, 22)
            Painter.fill(this, 2 + i * 4, top, 4, bottom - top + 1, Terrain.EMPTY)

            if (i == 2) {
                exit = i * 4 + 3 + (top - 1) * width()
            }

            for (j in 0..3) {
                if (Random.Int(2) == 0) {
                    val y = Random.IntRange(top + 1, bottom - 1)
                    map[i * 4 + j + y * width()] = Terrain.WALL_DECO
                }
            }
        }

        map[exit] = Terrain.LOCKED_EXIT

        Painter.fill(this, ROOM_LEFT - 1, ROOM_TOP - 1,
                ROOM_RIGHT - ROOM_LEFT + 3, ROOM_BOTTOM - ROOM_TOP + 3, Terrain
                .WALL)
        Painter.fill(this, ROOM_LEFT, ROOM_TOP,
                ROOM_RIGHT - ROOM_LEFT + 1, ROOM_BOTTOM - ROOM_TOP + 1, Terrain
                .EMPTY)

        entrance = Random.Int(ROOM_LEFT + 1, ROOM_RIGHT - 1) + Random.Int(ROOM_TOP + 1, ROOM_BOTTOM - 1) * width()
        map[entrance] = Terrain.ENTRANCE

        val patch = Patch.generate(this, 0.45f, 6)
        for (i in 0 until length()) {
            if (map[i] == Terrain.EMPTY && patch[i]) {
                map[i] = Terrain.WATER
            }
        }

        return true
    }

    override fun decorate() {

        for (i in 0 until length()) {
            if (map[i] == Terrain.EMPTY && Random.Int(10) == 0) {
                map[i] = Terrain.EMPTY_DECO
            }
        }
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
        if (entrance == -1) return entrance
        var cell = entrance + PathFinder.NEIGHBOURS8[Random.Int(8)]
        while (!Level.passable[cell]) {
            cell = entrance + PathFinder.NEIGHBOURS8[Random.Int(8)]
        }
        return cell
    }

    override fun press(cell: Int, hero: Char) {

        super.press(cell, hero)

        if (!enteredArena && hero === Dungeon.hero && cell != entrance) {

            enteredArena = true
            seal()

            for (i in ROOM_LEFT - 1..ROOM_RIGHT + 1) {
                doMagic((ROOM_TOP - 1) * width() + i)
                doMagic((ROOM_BOTTOM + 1) * width() + i)
            }
            for (i in ROOM_TOP until ROOM_BOTTOM + 1) {
                doMagic(i * width() + ROOM_LEFT - 1)
                doMagic(i * width() + ROOM_RIGHT + 1)
            }
            doMagic(entrance)
            GameScene.updateMap()

            Dungeon.observe()

            val boss = Yog().apply { 
                do{
                    pos = Random.Int(length())
                }while(!Level.passable[pos] || Dungeon.visible[pos]) 
            }
            GameScene.add(boss)
            boss.spawnFists()

            // give an observer
            Buff.prolong(hero, ViewMark::class.java, 1000f).observer = boss.id()

            stairs = entrance
            entrance = -1

            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }
    }

    private fun doMagic(cell: Int) {
        Level.set(cell, Terrain.EMPTY_SP)
        CellEmitter.get(cell).start(FlameParticle.FACTORY, 0.1f, 3)
    }

    override fun drop(item: Item, cell: Int): Heap {
        if (!keyDropped && item is SkeletonKey) {
            keyDropped = true
            unseal()

            entrance = stairs
            Level.set(entrance, Terrain.ENTRANCE)
            GameScene.updateMap(entrance)

            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }

        return super.drop(item, cell)
    }

    override fun tileName(tile: Int): String = when (tile) {
        Terrain.WATER -> Messages.get(HallsLevel::class.java, "water_name")
        Terrain.GRASS -> Messages.get(HallsLevel::class.java, "grass_name")
        Terrain.HIGH_GRASS -> Messages.get(HallsLevel::class.java, "high_grass_name")
        Terrain.STATUE, Terrain.STATUE_SP -> Messages.get(HallsLevel::class.java, "statue_name")
        else -> super.tileName(tile)
    }


    override fun tileDesc(tile: Int): String = when (tile) {
        Terrain.WATER -> Messages.get(HallsLevel::class.java, "water_desc")
        Terrain.STATUE, Terrain.STATUE_SP -> Messages.get(HallsLevel::class.java, "statue_desc")
        Terrain.BOOKSHELF -> Messages.get(HallsLevel::class.java, "bookshelf_desc")
        else -> super.tileDesc(tile)
    }


    override fun addVisuals(): Group {
        super.addVisuals()
        HallsLevel.AddHallsVisuals(this, visuals)
        return visuals
    }

    companion object {

        private val WIDTH = 32
        private val HEIGHT = 32

        private val ROOM_LEFT = WIDTH / 2 - 1
        private val ROOM_RIGHT = WIDTH / 2 + 1
        private val ROOM_TOP = HEIGHT / 2 - 1
        private val ROOM_BOTTOM = HEIGHT / 2 + 1

        private val STAIRS = "stairs"
        private val ENTERED = "entered"
        private val DROPPED = "droppped"
    }
}
