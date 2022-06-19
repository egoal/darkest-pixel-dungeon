package com.egoal.darkestpixeldungeon.scenes

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.utils.GLog

abstract class CellSelectListener(private val flag: Int,
                                  private val range: Int = Int.MAX_VALUE,
                                  private val visibleCheck: Boolean = true) : CellSelector.Listener {
    override fun prompt(): String = M.L(CellSelectListener::class.java, "prompt")

    final override fun onSelect(cell: Int?) {
        if (cell == null) onCancelled()
        else if (range < 100 && Dungeon.level.distance(Dungeon.hero.pos, cell) > range)
            GLog.w(M.L(CellSelectListener::class.java, "out_of_range"))
        else if (!filter(cell))
            GLog.w(M.L(CellSelectListener::class.java, "invalid_target"))
        else onSelected(cell)
    }

    open fun filter(cell: Int): Boolean {
        if (visibleCheck && !Dungeon.visible[cell]) return false

        if ((flag and FLAG_CELL) != 0) return true

        val ch = Actor.findChar(cell)
        if (ch == null) return false

        if ((flag and FLAG_ALLY) != 0 && ch.camp == Char.Camp.HERO) return true
        if ((flag and FLAG_ENEMY) != 0 && ch.camp == Char.Camp.ENEMY) return true
        if ((flag and FLAG_NEUTRAL) != 0 && ch.camp == Char.Camp.NEUTRAL) return true

        return false
    }

    open protected fun onCancelled() {}
    abstract protected fun onSelected(cell: Int)

    companion object {
        val FLAG_ENEMY = 0x01
        val FLAG_ALLY = 0x02
        val FLAG_NEUTRAL = 0x04
        val FLAG_CELL = 0x08

        val FLAG_CHAR = FLAG_ENEMY or FLAG_ALLY or FLAG_NEUTRAL
    }
}