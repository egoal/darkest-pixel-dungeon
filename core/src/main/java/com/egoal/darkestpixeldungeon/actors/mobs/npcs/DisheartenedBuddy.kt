package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.unclassified.Amulet
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.DisheartenedBuddySprite
import com.watabou.utils.Bundle
import com.watabou.utils.Random

/**
 * Created by 93942 on 5/8/2018.
 */

class DisheartenedBuddy : NPC.Unbreakable() {
    private var meetTimes_ = 0

    init {
        spriteClass = DisheartenedBuddySprite::class.java
        
        properties.add(Property.IMMOVABLE)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(MEET_TIMES, meetTimes_)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        meetTimes_ = bundle.getInt(MEET_TIMES)
    }

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        if (Dungeon.hero.belongings.getItem(Amulet::class.java) == null) {
            val chances = floatArrayOf(1f, 2f, 2f, 2f)
            if (meetTimes_++ == 0) {
                chances[0] = 10f
            }

            tell(Messages.get(this, "discourage" + Random.chances(chances)))
        } else {
            // with amulet
            tell(Messages.get(this, "amazed"))
        }

        return false
    }

    override fun reset(): Boolean = true

    companion object {
        private const val MEET_TIMES = "meettimes"
    }

}
