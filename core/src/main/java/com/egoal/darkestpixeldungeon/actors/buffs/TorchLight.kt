package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.messages.M

class TorchLight : Light() {
    init {
        duration = Float.MAX_VALUE
    }

    override fun attachTo(target: Char): Boolean {
        return if (super.attachTo(target)) {
            duration = Dungeon.torch * DURATION_PER_TORCH
            true
        } else false
    }

    override fun act(): Boolean {
        Dungeon.torch -= TORCH_CONSUME_PER_TURN
        duration = Dungeon.torch * DURATION_PER_TORCH + Actor.TICK

        return super.act()
    }

    override fun desc(): String = M.L(this, "desc", "${duration.toInt()}")

    companion object {
        private const val DURATION_PER_TORCH = 200f
        private const val TORCH_CONSUME_PER_TURN = 1f / DURATION_PER_TORCH
    }
}