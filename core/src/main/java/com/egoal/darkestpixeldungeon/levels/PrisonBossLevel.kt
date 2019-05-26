package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Bones
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Ignorant
import com.egoal.darkestpixeldungeon.actors.mobs.Tengu
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.books.textbook.WardenSmithNotes
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.items.unclassified.MoonStone
import com.egoal.darkestpixeldungeon.items.unclassified.Torch
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.Direction
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.traps.AlarmTrap
import com.egoal.darkestpixeldungeon.levels.traps.SpearTrap
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.audio.Music
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.min

class PrisonBossLevel : Level() {
    private lateinit var rtStart: Rect
    lateinit var rtHall: Rect
    private lateinit var rtExit: Rect

    private val prisonCells = mutableListOf<Rect>()
    private var hallLights = mutableListOf<Int>()

    private var enteredMainHall = false
    var isLighted = true

    private var bossAppeared = false
    private var bossDefeated = false

    init {
        color1 = 0x6a723d
        color2 = 0x88924c
    }

    override fun trackMusic(): String = if (bossAppeared && !bossDefeated) Assets.TRACK_BOSS_LOOP
    else Assets.TRACK_CHAPTER_2

    override fun tilesTex(): String = Assets.TILES_PRISON

    override fun waterTex(): String = Assets.WATER_PRISON

    fun turnLights(on: Boolean) {
        if (on == isLighted) return

        isLighted = on
        for (i in hallLights) {
            map[i] = if (isLighted) Terrain.WALL_LIGHT_ON else Terrain.WALL_LIGHT_OFF
            GameScene.updateMap(i)
        }

        if (!isLighted)
            for (i in hallLights) removeLuminaryAt(i)

        Dungeon.observe()
    }

    fun hallCenter(): Int = pointToCell(rtHall.center)

    override fun build(iteration: Int): Boolean {
        map.fill(Terrain.WALL)
        hallLights.clear()
        prisonCells.clear()

        buildHall()

        // entrance
        rtStart = run {
            val w = Random.Int(3, 5)
            val h = Random.Int(3, 5)
            val x = Random.Int(1, rtHall.x1 - w)
            val y = Random.Int(1, height / 3)

            Rect.Create(x, y, w, h)
        }
        Digger.Fill(this, rtStart, Terrain.EMPTY)
        Digger.Set(this, rtStart.center.x, rtStart.y2 + 1, Terrain.DOOR)

        // link to hall
        Digger.LinkVertical(this, rtStart.center.x, rtStart.y2 + 2, rtHall.y2, Terrain.EMPTY)
        Digger.LinkHorizontal(this, rtHall.y2, rtStart.center.x, rtHall.x1 - 2, Terrain.EMPTY)

        // exit
        rtExit = run {
            val w = Random.Int(3, 5)
            val h = Random.Int(3, 5)
            val x = Random.Int(rtHall.x2 + 2, width - w)
            val y = Random.Int(height * 2 / 3, height) - h

            Rect.Create(x, y, w, h)
        }
        Digger.Fill(this, rtExit, Terrain.EMPTY)
        Digger.Set(this, rtExit.center.x, rtExit.y1 - 1, Terrain.DOOR)

        // hall to exit
        Digger.LinkHorizontal(this, rtHall.y1, rtHall.x2 + 2, rtExit.center.x, Terrain.EMPTY)
        Digger.LinkVertical(this, rtExit.center.x, rtHall.y1, rtExit.y1 - 2, Terrain.EMPTY)

        // now, add some prison cells 
        if (rtStart.center.x >= 6)
            placePrisonCellsBesideLaneV(rtStart.center.x, rtStart.y2 + 2, rtHall.y2 - 1, true)
        if (rtHall.x1 - rtStart.center.x >= 6)
            placePrisonCellsBesideLaneV(rtStart.center.x, rtStart.y2 + 2, rtHall.y2 - 1, false)

        if (prisonCells.isEmpty()) return false // on left cells, cannot spawn items

        if (width - rtExit.center.x >= 6)
            placePrisonCellsBesideLaneV(rtExit.center.x, rtHall.y1 + 1, rtExit.y2 - 2, false)
        if (rtExit.center.x - rtHall.x2 >= 6)
            placePrisonCellsBesideLaneV(rtExit.center.x, rtHall.y1 + 1, rtExit.y2 - 2, true)

        if (height - rtHall.y2 >= 6)
            placePrisonCellsBesideLaneH(rtHall.y2, rtStart.center.x, rtHall.x2, false)
        if (rtHall.y1 >= 6)
            placePrisonCellsBesideLaneH(rtHall.y1, rtHall.x1, rtExit.center.x, true)

        // check door
        if (PathFinder.NEIGHBOURS4.any {
                    map[hallEntrance() + it] == Terrain.DOOR || map[hallExit() + it] == Terrain.DOOR
                }) return false

        // settings
        entrance = pointToCell(rtStart.center)
        exit = pointToCell(rtExit.center)

        map[entrance] = Terrain.ENTRANCE
        map[exit] = Terrain.EXIT

        return true
    }

    override fun decorate() {
        // some blood on floor
        for (r in prisonCells)
            if (Random.Int(2) == 0) Digger.Set(this, r.random(), Terrain.EMPTY_DECO)

        var cnt = Random.Int(6, 15)
        while (cnt > 0) {
            val cell = pointToCell(rtHall.random())
            if (map[cell] == Terrain.EMPTY) {
                map[cell] = Terrain.EMPTY_DECO
                --cnt
            }
        }

        // book shelves
    }

    override fun press(cell: Int, ch: Char?) {
        super.press(cell, ch)

        // hero get into the main hall, lock and spawn tengu
        if (ch === Dungeon.hero && !enteredMainHall && rtHall.inside(cellToPoint(cell))) {
            enteredMainHall = true

            seal()
            set(hallEntrance(), Terrain.LOCKED_DOOR)
            GameScene.updateMap(cell)
            Dungeon.observe()

            val tengu = Tengu().apply { pos = pointToCell(rtHall.center) }
            GameScene.add(tengu)
            tengu.state = tengu.HUNTING
            tengu.notice()

            // give buff
            Buff.affect(ch, Ignorant::class.java)
            MoonStone.Use(1000f)

            bossAppeared = true
            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }
    }

    override fun drop(item: Item?, cell: Int): Heap {
        if (item is SkeletonKey) {
            // oh no, tengu died...
            unseal()

            set(hallEntrance(), Terrain.DOOR)
            GameScene.updateMap(hallEntrance())
            Dungeon.observe()

            bossDefeated = true
            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }

        return super.drop(item, cell)
    }

    override fun randomRespawnCell(): Int =
            PathFinder.NEIGHBOURS8.map { it + entrance }.filter { passable[it] }.random()

    private fun buildHall() {
        rtHall = run {
            val w = Random.Int(16, 20)
            val h = Random.Int(16, 20)
            val l = Random.Int(8, width - 8 - w)
            val t = Random.Int(8, height - 8 - h)

            Rect.Create(l, t, w, h)
        }

        val cx = rtHall.center.x
        val cy = rtHall.center.y
        Digger.Fill(this, rtHall, Terrain.EMPTY)
        Digger.Fill(this, cx - 1, cy - 1, 3, 3, Terrain.EMPTY_SP)
        Digger.Set(this, rtHall.center, Terrain.WATER)

        // lights: 8 random, 4 corners
        val randomPos = { x1: Int, x2: Int, y1: Int, y2: Int, inner: Int ->
            pointToCell(Rect(x1, x2, y1, y2).shrink(inner).random())
        }
        hallLights.add(randomPos(rtHall.x1, cx - 4, rtHall.y1, cy - 4, 1))
        hallLights.add(randomPos(cx - 3, cx + 3, rtHall.y1, cy - 4, 1))
        hallLights.add(randomPos(cx + 4, rtHall.x2, rtHall.y1, cy - 4, 1))
        hallLights.add(randomPos(rtHall.x1, cx - 4, cy - 3, cy + 3, 1))
        hallLights.add(randomPos(cx + 4, rtHall.x2, cy - 3, cy + 3, 1))
        hallLights.add(randomPos(rtHall.x1, cx - 4, cy + 4, rtHall.y2, 1))
        hallLights.add(randomPos(cx - 3, cx + 3, cy + 4, rtHall.y2, 1))
        hallLights.add(randomPos(cx + 4, rtHall.x2, cy + 4, rtHall.y2, 1))

        hallLights.add(xy2cell(rtHall.center.x - 3, rtHall.center.y - 3))
        hallLights.add(xy2cell(rtHall.center.x + 3, rtHall.center.y - 3))
        hallLights.add(xy2cell(rtHall.center.x - 3, rtHall.center.y + 3))
        hallLights.add(xy2cell(rtHall.center.x + 3, rtHall.center.y + 3))

        for (i in hallLights) map[i] = Terrain.WALL_LIGHT_ON

        // put traps
        for (x in cx - 3..cx + 3)
            for (y in cy - 3..cy + 3) {
                val cell = xy2cell(x, y)
                if (map[cell] == Terrain.EMPTY) {
                    setTrap(SpearTrap().reveal(), cell)
                    map[cell] = Terrain.TRAP
                }
            }

        // alarm at bottom left
        val lb = xy2cell(rtHall.x1, rtHall.y2)
        setTrap(AlarmTrap().hide(), lb)
        map[lb] = Terrain.SECRET_TRAP

        Digger.Set(this, rtHall.x1, rtHall.y2 - 1, Terrain.WALL)

        // door
        Digger.Set(this, hallEntrance(), Terrain.LOCKED_DOOR)
        Digger.Set(this, hallExit(), Terrain.LOCKED_EXIT)
    }

    private fun placePrisonCellsBesideLaneV(x: Int, y1: Int, y2: Int, onLeft: Boolean) {
        val s = min(y1, y2)
        val e = max(y1, y2)

        val left = if (onLeft) x - 4 else x + 2
        val dir = if (onLeft) Direction.Left else Direction.Right
        var top = s
        while (top + 4 < e) {
            if (Random.Int(2) == 0) {
                ++top
            } else {
                placePrisonCell(left, top, dir)
                top += 4
            }
        }
    }

    private fun placePrisonCellsBesideLaneH(y: Int, x1: Int, x2: Int, onTop: Boolean) {
        val s = min(x1, x2)
        val e = max(x1, x2)

        val top = if (onTop) y - 4 else y + 2
        val dir = if (onTop) Direction.Up else Direction.Down
        var left = s
        while (left + 4 < e) {
            if (Random.Int(2) == 0) {
                ++left
            } else {
                placePrisonCell(left, top, dir)
                left += 4
            }
        }
    }

    private fun placePrisonCell(left: Int, top: Int, dir: Direction) {
        val r = Rect.Create(left, top, 3, 3)

        // dig
        Digger.Fill(this, r, Terrain.EMPTY)

        var door = -1
        var light = -1
        when (dir) {
            Direction.Left -> {
                door = xy2cell(r.x2 + 1, r.center.y)
                light = xy2cell(r.x1 - 1, r.center.y)
            }
            Direction.Right -> {
                door = xy2cell(r.x1 - 1, r.center.y)
                light = xy2cell(r.x2 + 1, r.center.y)
            }
            Direction.Up -> {
                door = xy2cell(r.center.x, r.y2 + 1)
                light = xy2cell(r.center.x, r.y1 - 1)
            }
            Direction.Down -> {
                door = xy2cell(r.center.x, r.y1 - 1)
                light = xy2cell(r.center.x, r.y2 + 1)
            }
        }

        Digger.Set(this, door, Terrain.DOOR)
        Digger.Set(this, light, Terrain.WALL_LIGHT_ON)

        prisonCells.add(r)
    }

    private fun hallEntrance(): Int = xy2cell(rtHall.x1 - 1, rtHall.y2)
    private fun hallExit(): Int = xy2cell(rtHall.x2 + 1, rtHall.y1)

    override fun createMobs() {}

    override fun respawner(): Actor? = null

    override fun createItems() {
        Bones.get()?.let {
            drop(it, randomRespawnCell()).type = Heap.Type.REMAINS
        }

        val leftCells = prisonCells.filter { it.x2 < rtHall.x1 }

        drop(IronKey(Dungeon.depth), pointToCell(leftCells.random().random()))
        drop(Torch(), pointToCell(leftCells.random().random()))
        drop(WardenSmithNotes(), pointToCell(leftCells.random().random()))
    }

    override fun tileName(tile: Int): String = when (tile) {
        Terrain.WATER -> M.L(PrisonLevel::class.java, "water_name")
        else -> super.tileName(tile)
    }

    override fun tileDesc(tile: Int): String = when (tile) {
        Terrain.EMPTY_DECO -> M.L(PrisonLevel::class.java, "empty_deco_desc")
        Terrain.BOOKSHELF -> M.L(PrisonLevel::class.java, "book_self_desc")
        Terrain.EXIT -> M.L(PrisonBossLevel::class.java, "exit_desc")
        Terrain.LOCKED_EXIT -> M.L(PrisonBossLevel::class.java, "locked_exit_desc")
        else -> super.tileDesc(tile)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        bundle.put(ENTERED, enteredMainHall)
        bundle.put(HALL, rtHall)
        bundle.put(LIGHTED, isLighted)
        bundle.put(HALL_LIGHTS, hallLights.toIntArray())
        bundle.put(BOSS_APPEARED, bossAppeared)
        bundle.put(BOSS_DEFEATED, bossDefeated)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        enteredMainHall = bundle.getBoolean(ENTERED)
        isLighted = bundle.getBoolean(LIGHTED)
        rtHall = Rect()
        rtHall.restoreFromBundle(bundle.getBundle(HALL))
        hallLights = bundle.getIntArray(HALL_LIGHTS).toMutableList()
        bossAppeared = bundle.getBoolean(BOSS_APPEARED)
        bossDefeated = bundle.getBoolean(BOSS_DEFEATED)
    }

    companion object {
        private const val ENTERED = "entered"
        private const val HALL = "hall"
        private const val LIGHTED = "lighted"
        private const val HALL_LIGHTS = "hall_lights"
        private const val BOSS_APPEARED = "boss-appeared"
        private const val BOSS_DEFEATED = "boss-defeated"
    }
}