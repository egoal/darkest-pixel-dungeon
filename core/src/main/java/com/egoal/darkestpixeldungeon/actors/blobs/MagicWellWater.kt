package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.utils.Bundle
import kotlin.math.min

class MagicWellWater : Blob() {
    private var pos: Int = 0

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        for (i in cur.indices)
            if (cur[i] > 0) {
                pos = i
                break
            }
    }

    // do not spread
    override fun evolve() {
        off[pos] = cur[pos]
        volume = off[pos]

        area.union(pos % Dungeon.level.width(), pos / Dungeon.level.width())

        if (Dungeon.visible[pos]) {
            Journal.add(M.L(this, "name"))
        }
    }

    override fun seed(level: Level, cell: Int, amount: Int) {
        super.seed(level, cell, amount)

        cur[pos] = 0
        pos = cell
        cur[pos] = amount
        volume = cur[pos]

        area.setEmpty()
        area.union(cell % level.width(), cell / level.width())
    }

    private fun consume(amount: Int) {
        val amount = min(amount, cur[pos])
        cur[pos] -= amount
        off[pos] = cur[pos]
        volume = off[pos]

        if (volume <= 0) {
            Level.set(pos, Terrain.EMPTY_WELL)
            GameScene.updateMap(pos)
        }
    }

    fun interact(hero: Hero) {}

    companion object {
        fun Affect(cell: Int) {
            val water = Dungeon.level.blobs[MagicWellWater::class.java] as MagicWellWater?
            if (water != null && Dungeon.hero.pos == cell) {
                water.interact(Dungeon.hero)
            }
        }
    }
}