package com.egoal.darkestpixeldungeon.levels.diggers.unordinary

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.items.quest.CeremonialCandle
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RoundDigger
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual
import com.watabou.utils.PathFinder

/**
 * Created by 93942 on 2018/12/18.
 */

class RitualSiteDigger : RoundDigger() {
    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)

        // put ritual
        val rm = RitualMarker()
        rm.pos(rect.center.x - 1, rect.center.y - 1)
        level.customTiles.add(rm)

        val cen = level.pointToCell(rect.center)
        for (i in PathFinder.NEIGHBOURS9)
            Set(level, cen + i, Terrain.EMPTY_DECO)

        for (i in 0..3)
            level.addItemToSpawn(CeremonialCandle())
        CeremonialCandle.ritualPos = cen

        return dr
    }

    class RitualMarker : CustomTileVisual() {
        init {
            name = Messages.get(this, "name")

            tx = Assets.PRISON_QUEST
            txY = 0
            txX = txY
            tileH = 3
            tileW = tileH
        }

        override fun desc(): String? {
            return Messages.get(this, "desc")
        }
    }
}
