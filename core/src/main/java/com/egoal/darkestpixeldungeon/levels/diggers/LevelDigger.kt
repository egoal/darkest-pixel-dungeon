package com.egoal.darkestpixeldungeon.levels.diggers

import android.util.Log
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

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

            if (!dag) return false
        }

        if(minLoops>0 && makeLoopClosure(6)<=minLoops)
            return false

        return true
    }

    private fun reset() {
        diggers.clear()
        walls.clear()
        spaces.clear()
    }

    private fun digFirstRoom() {
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
