package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Patch
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.watabou.utils.Point
import com.watabou.utils.Random

open class PatchDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(6, 10), Random.IntRange(6, 10))

    open fun patch(width: Int, height: Int): BooleanArray = Patch.Generate(width, height, 0.5f, 1)
    open fun patchTile() = if (Random.Float() < 0.5f) Terrain.GRASS else Terrain.CHASM
    open fun unpatchTile() = Terrain.EMPTY

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)

        // fill patch
        val tile = patchTile()
        val tile0 = unpatchTile()
        val region = if (Terrain.flags[tile] and Terrain.PASSABLE > 0) rect else rect.shrink(1)

        val patch = patch(region.width, region.height)
        fillPatch(level, region, patch, tile, tile0)

        return dr
    }

    protected fun fillPatch(level: Level, rect: Rect, patch: BooleanArray, patchTile: Int, unpathTile: Int) {
        assert(rect.area == patch.size)

        for (i in 0 until patch.size)
            Set(level, rect.x1 + i % rect.width, rect.y1 + i / rect.width, if (patch[i]) patchTile else unpathTile)
    }
}