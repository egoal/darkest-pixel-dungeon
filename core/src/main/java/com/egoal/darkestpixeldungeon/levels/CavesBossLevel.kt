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
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.RobotREN
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
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
    private var arenaDoorUp: Int = 0
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

    override fun build(iterations: Int): Boolean {
        loadMapDataFromFile(MAP_FILE)

        arenaDoor = xy2cell(16, 26)
        arenaDoorUp = xy2cell(16, 11)

        val patch = Patch.Generate(this, 0.45f, 6)
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
            sign = pointToCell(ROOM_ENTRANCE.shrink(1).random())
        } while (sign == entrance || map[sign] == Terrain.INACTIVE_TRAP)
        map[sign] = Terrain.SIGN
    }

    override fun createMobs() {
        mobs.add(RobotREN().apply { pos = xy2cell(12, 7) })
    }

    override fun respawner(): Actor? = null

    override fun createItems() {
        val item = Bones.get()
        if (item != null) {
            var pos: Int
            do {
                pos = pointToCell(ROOM_ENTRANCE.shrink(1).random())
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
                } while (pos < 400 || !Level.passable[pos] || !outsideEntranceRoom(pos) || Dungeon.visible[pos])
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

            // open
            val openDoor = { cell: Int ->
                CellEmitter.get(cell).start(Speck.factory(Speck.ROCK), 0.07f, 10)
                Level.set(cell, Terrain.EMPTY_DECO)
                GameScene.updateMap(cell)
            }

            openDoor(arenaDoor)
            openDoor(arenaDoorUp)

            Dungeon.observe()

            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }

        return super.drop(item, cell)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ENTERED, enteredArena)
        bundle.put(DROPPED, keyDropped)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        enteredArena = bundle.getBoolean(ENTERED)
        keyDropped = bundle.getBoolean(DROPPED)
    }


    private fun outsideEntranceRoom(cell: Int): Boolean = !ROOM_ENTRANCE.inside(cellToPoint(cell))

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
        private const val MAP_FILE: String = "data/CavesBossLevel.map"

        private val ROOM_ENTRANCE = Rect(14, 18, 22, 26)

        private const val DOOR = "door"
        private const val ENTERED = "entered"
        private const val DROPPED = "droppped"
    }
}
