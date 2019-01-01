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

    open fun patch(rect: Rect): BooleanArray = Patch.generate(rect.width - 2, rect.height - 2, 0.5f, 1)
    open fun patchTile() = if (Random.Float() < 0.5f) Terrain.GRASS else Terrain.CHASM

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)

        // fill patch
        val patch = patch(rect)
        val tile = patchTile()
        for (i in 0 until patch.size)
            if (patch[i])
                Set(level, rect.x1 + i % rect.width + 1, rect.y1 + i / rect.width + 1, tile)

        return dr
    }


}