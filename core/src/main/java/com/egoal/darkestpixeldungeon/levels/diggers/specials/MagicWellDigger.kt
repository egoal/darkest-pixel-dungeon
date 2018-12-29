package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfAwareness
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfHealth
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfTransmutation
import com.egoal.darkestpixeldungeon.actors.blobs.WellWater
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RoundDigger
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/12.
 */

class MagicWellDigger : RoundDigger() {
    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)

        // set a well
        val cen = level.pointToCell(rect.center)
        for (i in PathFinder.NEIGHBOURS8)
            if (Random.Int(2) == 0)
                Set(level, cen + i, Terrain.GRASS)
        Set(level, cen, Terrain.WELL)

        val cls = Random.element(WATERS) as Class<out WellWater>

        var ww: WellWater? = level.blobs[cls] as WellWater
        if (ww == null) {
            try {
                ww = cls.newInstance()
            } catch (e: Exception) {
                DarkestPixelDungeon.reportException(e)
                return dr
            }

        }
        ww!!.seed(level, cen, 1)
        level.blobs[cls] = ww

        return dr
    }

    companion object {
        private val WATERS = arrayOf(WaterOfAwareness::class.java, WaterOfHealth::class.java, WaterOfTransmutation::class.java)
    }

}
