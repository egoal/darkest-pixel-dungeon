package com.egoal.darkestpixeldungeon.levels

import com.watabou.utils.Random

object Patch {

    fun Generate(level: Level, seed: Float, nGen: Int): BooleanArray =
            Patch.Generate(level.width(), level.height(), seed, nGen)

    fun Generate(w: Int, h: Int, seed: Float, nGen: Int): BooleanArray {
        var cur = BooleanArray(w * h)
        var off = BooleanArray(w * h)
        for (i in 0 until w * h)
            off[i] = Random.Float() < seed

        repeat(nGen) {
            for (y in 1 until h - 1) {
                for (x in 1 until w - 1) {
                    // count neighbors
                    var count = 0
                    val pos = x + y * w
                    if (off[pos - w - 1]) count++
                    if (off[pos - w]) count++
                    if (off[pos - w + 1]) count++
                    if (off[pos - 1]) count++
                    if (off[pos + 1]) count++
                    if (off[pos + w - 1]) count++
                    if (off[pos + w]) count++
                    if (off[pos + w + 1]) count++

                    cur[pos] = when {
                        !off[pos] && count >= 5 -> true
                        off[pos] && count >= 4 -> true
                        else -> false
                    }
                }
            }

            val tmp = cur
            cur = off
            off = tmp
        }

        return off
    }
}