package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.features.Luminary
import com.watabou.utils.PathFinder

class TempPathLight(val path: ArrayList<Int>) : FlavourBuff() {

    private lateinit var lum: Luminary
    override fun onAdd() {
        if (Dungeon.level != null) {
            lum = Light()
            Dungeon.level.addLuminary(lum)
            Dungeon.observe()
        }
    }

    override fun attachTo(target: Char): Boolean {
        return if (super.attachTo(target)) {
            Dungeon.observe()
            true
        } else {
            false
        }
    }

    override fun detach() {
        super.detach()
        Dungeon.level.removeLuminary(lum)
        Dungeon.observe()
    }

    inner class Light : Luminary() {
        override fun light(level: Level) {
            for (i in path)
                if (i > level.width() && i < level.length() - level.width() - 1)
                    for (n in PathFinder.NEIGHBOURS9)
                        Level.lighted[i + n] = true
        }

        override fun createVisual(): LightVisual? = null
    }

    companion object {
        fun Light(path: ArrayList<Int>, duration: Float) {
            val tpl = TempPathLight(path)
            tpl.attachTo(Dungeon.hero)
            tpl.postpone(duration)
        }
    }
}
