package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.hero.Hero

class Stasis : FlavourBuff() {
    private var duration = 1f

    fun setDuration(duration: Float) {
        postpone(duration)
        this.duration = duration
    }

    override fun attachTo(target: Char): Boolean {
        if (!super.attachTo(target)) return false

        (target as Hero).spendAndNext(duration + .1f) // left buff ends first.
        //todo: need fix time

        target.buff(Hunger::class.java)!!.satisfy(duration * 1.1f)

        target.invisible++
        Dungeon.observe()

        return true
    }

    override fun detach() {
        if (target.invisible > 0) target.invisible--
        super.detach()
        Dungeon.observe()
    }
}