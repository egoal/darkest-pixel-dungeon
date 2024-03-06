package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.ElementBroken
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class ReagentOfSnakeBite : Reagent(false) {
    init {
        image = ItemSpriteSheet.REAGENT_SNAKE_BITE
    }

    override fun shatter(cell: Int) {
        super.shatter(cell)

        Actor.findChar(cell)?.let {
            Buff.prolong(it, ElementBroken::class.java, 10f).add(Damage.Element.Poison, 2f)
            Buff.affect(it, Poison::class.java).apply {
                set(3f)
                addExtraDamage(Dungeon.depth / 3 + Random.Int(1, 4))
            }
        }
    }
}