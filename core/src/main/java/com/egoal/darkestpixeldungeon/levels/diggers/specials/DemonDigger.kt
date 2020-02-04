package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.ArchDemon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.DiamondDigger
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class DemonDigger : DiamondDigger() {
    private val demon = ArchDemon()

    override fun chooseDigArea(wall: Wall): Rect {
        val size = Random.IntRange(3, 4) * 2 + 1
        return chooseCenteredRect(wall, size, size)
    }

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)

        val cen = level.pointToCell(rect.center)
        Set(level, cen, Terrain.WATER)
        for (i in PathFinder.NEIGHBOURS8) Set(level, cen + i, Terrain.CHASM)

        demon.pos = cen
        level.mobs.add(demon)

        return dr
    }
}