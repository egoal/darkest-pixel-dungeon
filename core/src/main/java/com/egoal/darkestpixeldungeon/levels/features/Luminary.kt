package com.egoal.darkestpixeldungeon.levels.features

import android.util.Log
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.effects.Halo
import com.egoal.darkestpixeldungeon.levels.Level
import com.watabou.noosa.particles.Emitter

// basic class for luminaries
open class Luminary(var pos: Int = -1) {
    open fun light(level: Level) {
        L2R3.forEach {
            if (level.insideMap(pos + it)) Level.lighted[pos + it] = true
        }
    }

    open fun createVisual(): LightVisual = TorchLight(pos)

    // 
    abstract class LightVisual(val cell: Int) : Emitter()

    open class TorchLight(cell: Int) : LightVisual(cell) {
        init {
            val p = DungeonTilemap.tileCenterToWorld(cell)
            pos(p.x - 1f, p.y + 3f, 2f, 0f)

            add(Halo(20f, 0xffffcc, 0.2f).point(p.x, p.y))
        }

        override fun update() {
            visible = Dungeon.visible[cell]
            if (visible)
                super.update()
        }
    }

    companion object {
        // caches...
        lateinit var L2R3: IntArray // l2 norm with radius 3

        fun SetMapSize(width: Int, height: Int) {
            L2R3 = intArrayOf(
                    -width * 2 - 1, -width * 2, -width * 2 + 1,
                    -width - 2, -width - 1, -width, -width + 1, -width + 2,
                    -2, -1, 0, +1, +2,
                    +width - 2, +width - 1, +width, +width + 1, +width + 2,
                    +width * 2 - 1, +width * 2, +width * 2 + 1
            )
        }
    }
}