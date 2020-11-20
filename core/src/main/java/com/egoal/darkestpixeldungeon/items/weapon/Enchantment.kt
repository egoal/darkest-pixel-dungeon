package com.egoal.darkestpixeldungeon.items.weapon

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.*
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

abstract class Enchantment : Bundlable {
    var left = 0

    fun name(): String = M.L(this, "name")

    fun desc(): String = selfDesc() + M.L(Enchantment::class.java, "left_time", left)

    protected open fun selfDesc(): String = M.L(this, "desc")

    fun proc(weapon: Weapon, damage: Damage): Damage {
        left--
        if (left == 0) {
            weapon.enchantment = null
            GLog.w(M.L(Enchantment::class.java, "no_effect", name()))

            QuickSlotButton.refresh()
        }

        return procImpl(weapon, damage)
    }

    protected abstract fun procImpl(weapon: Weapon, damage: Damage): Damage

    abstract fun glowing(): ItemSprite.Glowing

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(LEFT, left)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        left = bundle.getInt(LEFT)
    }

    companion object {
        private const val LEFT = "left"

        private val enchantments = arrayOf<Class<out Enchantment>>(
                Blazing::class.java, Chilling::class.java, Shocking::class.java,
                Unstable::class.java, Venomous::class.java
        )

        fun Random(): Class<out Enchantment> = com.watabou.utils.Random.oneOf(*enchantments)
    }
}
