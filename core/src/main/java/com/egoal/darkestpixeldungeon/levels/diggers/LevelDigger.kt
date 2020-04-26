package com.egoal.darkestpixeldungeon.levels.diggers

import android.util.Log
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.min

data class Space(var rect: Rect = Rect(), var type: DigResult.Type = DigResult.Type.Normal) : Bundlable {
    override fun storeInBundle(bundle: Bundle) {
        bundle.put("rect", rect)
        bundle.put("type", type.toString())
    }

    override fun restoreFromBundle(bundle: Bundle) {
        rect = bundle.get("rect") as Rect
        type = DigResult.Type.valueOf(bundle.getString("type"))
    }
}

class LevelDigger(val level: Level, private val minLoops: Int = 2) {
    private var diggers = ArrayList<Digger>()
    private var walls = ArrayList<Wall>()
    var spaces = ArrayList<Space>()

    // this can fail, since the items to spawn should be reset outside.
    fun dig(chosenDiggers: ArrayList<Digger>): Boolean {
        reset()
        diggers.addAll(chosenDiggers)

        // init
        digFirstRoom()

        // dig!
        diggers.shuffle()
        for (digger in diggers) {
            if (walls.isEmpty()) {
                Log.d("dpd", "no walls to dig.")
                return false
            }

            var dag = false
            for (_i in 1..100) {
                val wall = Random.element(walls)
                val rect = digger.chooseDigArea(wall)

                if (canDigAt(rect)) {
                    dag = true
                    val dr = digger.dig(level, wall, rect)
                    walls.remove(wall)
                    walls.addAll(dr.walls)
                    spaces.add(Space(rect, dr.type))
                    break
                }
            }

            if (!dag) {
                Log.d("dpd", "failed after 100 trials.")
                return false
            }
        }

        if (minLoops > 0 && makeLoopClosure(6) <= minLoops)
            return false

        return true
    }

    private fun reset() {
        diggers.clear()
        walls.clear()
        spaces.clear()
    }

    private fun digFirstRoom() {
        val p = Random.Float()
        when {
            p < 0.15f -> digFirstRoomAnnular()
            p < 0.3f -> digFirstRoomCross()
            else -> digFirstRoomRect()
        }
    }

    private fun digFirstRoomRect() {
        val w = Random.IntRange(4, 6)
        val h = Random.IntRange(4, 6)
        val x = Random.IntRange(level.width() / 4, level.width() / 4 * 3 - w)
        val y = Random.IntRange(level.height() / 4, level.height() / 4 * 3 - h)

        val rect = Rect.Create(x, y, w, h)

        Digger.Fill(level, rect, Terrain.EMPTY)
        walls.addAll(listOf(
                Wall.Left(rect.x1 - 1, rect.y1, rect.y2),
                Wall.Right(rect.x2 + 1, rect.y1, rect.y2),
                Wall.Up(rect.y1 - 1, rect.x1, rect.x2),
                Wall.Down(rect.y2 + 1, rect.x1, rect.x2)))

        spaces.add(Space(rect, DigResult.Type.Normal))
    }

    private fun digFirstRoomAnnular() {
        val inner = if (Random.Float() < 0.075f) 1 else Random.Int(6, level.width() / 2 - 6)
        digFirstRoomAnnular(Rect(inner, level.width() - 1 - inner, inner, level.height() - 1 - inner), Random.Int(2, 4))
    }

    private fun digFirstRoomAnnular(outer: Rect, tunnelWidth: Int) {
        val inner = outer.shrink(tunnelWidth)
        Digger.Fill(level, outer, Terrain.EMPTY)
        Digger.Fill(level, inner, Terrain.WALL)
        if (Random.Float() < 0.3f) Digger.Set(level, outer.x1, outer.y1, Terrain.WALL)
        if (Random.Float() < 0.3f) Digger.Set(level, outer.x1, outer.y2, Terrain.WALL)
        if (Random.Float() < 0.3f) Digger.Set(level, outer.x2, outer.y2, Terrain.WALL)
        if (Random.Float() < 0.3f) Digger.Set(level, outer.x2, outer.y1, Terrain.WALL)

        val expandInner = inner.width > 5
        val expandOuter = outer.x1 > 6

        for (pr in makeSegments(inner.y1, inner.y2)) {
            if (expandInner) walls.add(Wall.Right(inner.x1, pr.first, pr.second))
            if (expandOuter) walls.add(Wall.Left(outer.x2 - 1, pr.first, pr.second))
        }
        for (pr in makeSegments(inner.y1, inner.y2)) {
            if (expandInner) walls.add(Wall.Left(inner.x2, pr.first, pr.second))
            if (expandOuter) walls.add(Wall.Right(outer.x2 + 1, pr.first, pr.second))
        }
        for (pr in makeSegments(inner.x1, inner.x2)) {
            if (expandInner) walls.add(Wall.Down(inner.y1, pr.first, pr.second))
            if (expandOuter) walls.add(Wall.Up(outer.y1 - 1, pr.first, pr.second))
        }
        for (pr in makeSegments(inner.x1, inner.x2)) {
            if (expandInner) walls.add(Wall.Up(inner.y2, pr.first, pr.second))
            if (expandOuter) walls.add(Wall.Down(outer.y2 + 1, pr.first, pr.second))
        }

        spaces.add(Space(Rect(outer.x1, inner.x1 - 1, inner.y1, inner.y2), DigResult.Type.Normal))
        spaces.add(Space(Rect(inner.x2 + 1, outer.x2, inner.y1, inner.y2), DigResult.Type.Normal))
        spaces.add(Space(Rect(inner.x1, inner.x2, outer.y1, inner.y1 - 1), DigResult.Type.Normal))
        spaces.add(Space(Rect(inner.x1, inner.x2, inner.y2 + 1, outer.y2), DigResult.Type.Normal))

        Log.d("dpd", "first room: ${walls.size} walls, ${spaces.size} spaces.")
    }

    private fun makeSegments(begin: Int, end: Int): ArrayList<Pair<Int, Int>> {
        val segs = ArrayList<Pair<Int, Int>>()
        var x1 = begin
        var x2 = begin + Random.Int(4, 6)
        while (x2 <= end) {
            segs.add(Pair(x1, x2))
            x1 = x2
            x2 += Random.Int(4, 6)
        }

        return segs
    }

    private fun digFirstRoomCross() {
        val w = Random.NormalIntRange(2, 4)
        val h = Random.NormalIntRange(2, 4)
        val x = Random.IntRange(4, level.width() - 4 - w)
        val y = Random.IntRange(4, level.height() - 4 - h)
        digFirstRoomCross(Rect.Create(x, y, w, h))
    }

    private fun digFirstRoomCross(center: Rect) {
        assert(center.x1 >= 1 && center.x2 <= level.width() - 2 && center.y1 >= 1 && center.y2 <= level.height() - 2)

        // expand
        val maxCrossLen = 5
        val left = max(1, center.x1 - maxCrossLen)
        val right = min(center.x2 + + maxCrossLen, level.width() - 2)
        val top = max(1, center.y1 - maxCrossLen)
        val bottom = min(center.y2 + maxCrossLen, level.height() - 2)

        Digger.Fill(level, Rect(left, right, center.y1, center.y2), Terrain.EMPTY)
        Digger.Fill(level, Rect(center.x1, center.x2, top, bottom), Terrain.EMPTY)
        if (Random.Float() < 0.3f) Digger.Set(level, center.x1 - 1, center.y1 - 1, Terrain.WALL_LIGHT_ON)
        if (Random.Float() < 0.3f) Digger.Set(level, center.x1 - 1, center.y2 + 1, Terrain.WALL_LIGHT_ON)
        if (Random.Float() < 0.3f) Digger.Set(level, center.x2 + 1, center.y1 - 1, Terrain.WALL_LIGHT_ON)
        if (Random.Float() < 0.3f) Digger.Set(level, center.x2 + 1, center.y2 + 1, Terrain.WALL_LIGHT_ON)

        for (pr in makeSegments(left, center.x1 - 1)) {
            if (center.y1 > 5) walls.add(Wall.Up(center.y1 - 1, pr.first, pr.second))
            if (center.y2 < level.height() - 6) walls.add(Wall.Down(center.y2 + 1, pr.first, pr.second))
        }
        for (pr in makeSegments(center.x2 + 1, right)) {
            if (center.y1 > 5) walls.add(Wall.Up(center.y1 - 1, pr.first, pr.second))
            if (center.y2 < level.height() - 6) walls.add(Wall.Down(center.y2 + 1, pr.first, pr.second))
        }
        for (pr in makeSegments(top, center.y1 - 1)) {
            if (center.x1 > 5) walls.add(Wall.Left(center.x1 - 1, pr.first, pr.second))
            if (center.x2 < level.width() - 6) walls.add(Wall.Right(center.x2 + 1, pr.first, pr.second))
        }
        for (pr in makeSegments(center.y2 + 1, bottom)) {
            if (center.x1 > 5) walls.add(Wall.Left(center.x1 - 1, pr.first, pr.second))
            if (center.x2 < level.width() - 6) walls.add(Wall.Right(center.x2 + 1, pr.first, pr.second))
        }

        spaces.add(Space(Rect(left, center.x1 - 1, center.y1, center.y2), DigResult.Type.Normal))
        spaces.add(Space(Rect(center.x2 + 1, right, center.y1, center.y2), DigResult.Type.Normal))
        spaces.add(Space(Rect(center.x1, center.x2, top, center.y1 - 1), DigResult.Type.Normal))
        spaces.add(Space(Rect(center.x1, center.x2, center.y2 + 1, bottom), DigResult.Type.Normal))
        spaces.add(Space(center, DigResult.Type.Normal))
    }

    private fun canDigAt(rect: Rect): Boolean {
        return rect.x1 > 0 && rect.x2 < level.width() - 1 &&
                rect.y1 > 0 && rect.y2 < level.height() - 1 &&
                Digger.All(level, rect.shrink(-1), Terrain.WALL)
    }

    private fun makeLoopClosure(maxLoops: Int): Int {
        var loops = 0

        // dig a wall when overlapped
        val overlaps = ArrayList<Pair<Wall, Wall>>()
        for (i in 0 until walls.size) {
            val wi = walls[i]
            for (j in i + 1 until walls.size) {
                val wj = walls[j]

                if (wi.direction.opposite == wj.direction && Rect.Overlap(wi, wj).valid)
                    overlaps.add(Pair(wi, wj))
            }
        }
        overlaps.shuffle()

        for (pr in overlaps) {
            val seg = Rect.Overlap(pr.first, pr.second)
            // Digger.Fill(level, seg, Terrain.EMPTY_SP)
            for (i in 1..3) {
                val dp = level.pointToCell(seg.random())

                if (PathFinder.NEIGHBOURS9.all { level.map[dp + it] != Terrain.DOOR }) {
                    Digger.Set(level, dp, Terrain.DOOR)
                    walls.remove(pr.first)
                    walls.remove(pr.second)

                    ++loops
                    break
                }
            }

            if (loops > maxLoops)
                break
        }

        //todo: extra strategy to handle more loops 

        return loops
    }
}
