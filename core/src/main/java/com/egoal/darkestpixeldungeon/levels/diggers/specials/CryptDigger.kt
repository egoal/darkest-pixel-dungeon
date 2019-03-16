package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KGenerator
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Direction
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.Point
import com.watabou.utils.Random

class CryptDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall): Point {
        val w = 2 * Random.IntRange(1, 2) + 1
        val l = Random.IntRange(4, 6)
        return if (wall.direction.vertical) Point(w, l) else Point(l, w)
    }

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.GRASS)
        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.LOCKED_DOOR)

        val lastRowTile = Terrain.STATUE
        val plat = rect.center
        when (wall.direction) {
            Direction.Left -> {
                LinkVertical(level, rect.x1, rect.y1, rect.y2, lastRowTile)
                Set(level, rect.x1, (rect.y1 + rect.y2) / 2, Terrain.GRASS)
                plat.x = rect.x1 + 1
            }
            Direction.Right -> {
                LinkVertical(level, rect.x2, rect.y1, rect.y2, lastRowTile)
                Set(level, rect.x2, (rect.y1 + rect.y2) / 2, Terrain.GRASS)
                plat.x = rect.x2 - 1
            }
            Direction.Up -> {
                LinkHorizontal(level, rect.y1, rect.x1, rect.x2, lastRowTile)
                Set(level, (rect.x1 + rect.x2) / 2, rect.y1, Terrain.GRASS)
                plat.y = rect.y1 + 1
            }
            Direction.Down -> {
                LinkHorizontal(level, rect.y2, rect.x1, rect.x2, lastRowTile)
                Set(level, (rect.x1 + rect.x2) / 2, rect.y2, Terrain.GRASS)
                plat.y = rect.y2 - 1
            }
        }

        level.drop(prize(level), level.pointToCell(plat)).type = Heap.Type.TOMB
        level.addItemToSpawn(IronKey(Dungeon.depth))

        return DigResult(rect, DigResult.Type.Locked)
    }

    private fun prize(level: Level): Item = KGenerator.ARMOR.random(KGenerator.CurrentFloorSet() + 1).apply {
        if (!cursed) {
            // not cursed, upgrade and cursed.
            upgrade()
            if (!hasGoodGlyph())
                inscribe(Armor.Glyph.randomCurse())
        }
        cursedKnown = true
        cursed = true
    }
}
