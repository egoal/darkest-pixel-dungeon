package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Amok
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class ReagentOfHallucinogenic : Reagent(false) {
    init {
        image = ItemSpriteSheet.REAGENT_HALLUCINOGENIC
    }

    override fun shatter(cell: Int) {
        super.shatter(cell)

        Actor.findChar(cell)?.let {
            Buff.prolong(it, Hallucinogenic::class.java, 6f)
        }
    }

    class Hallucinogenic : Amok() {
        override fun detach() {
            super.detach()
            prolong(target, Cripple::class.java, 4f)
        }
    }
}