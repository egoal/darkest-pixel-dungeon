package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.RiemannianManifoldShield
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

/**
 * Created by 93942 on 9/4/2018.
 */

//* check in Char::resistDamage
class ResistAny : Buff() {

    var resistCount = 1

    init {
        type = Buff.buffType.POSITIVE
    }

    fun set(resist: Int): ResistAny {
        resistCount = resist
        return this
    }

    fun resist() {
        if (--resistCount <= 0) {
            // would detach, check rms
            if (target is Hero) {
                for (item in (target as Hero).belongings.equippedItems())
                    if (item is RiemannianManifoldShield)
                        item.recharge()
            }
            target.sprite.showStatus(CharSprite.WARNING, Messages.get(this, "resist"))
            detach()
        }
    }

    override fun icon(): Int {
        return BuffIndicator.RESIST_ANY
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", resistCount)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(RESIST_COUNT, resistCount)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        resistCount = bundle.getInt(RESIST_COUNT)
    }

    companion object {

        private val RESIST_COUNT = "resist_count"
    }
}
