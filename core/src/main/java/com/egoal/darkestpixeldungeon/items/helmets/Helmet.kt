package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random


open class Helmet(private var ticksToKnow: Int = TICKS_TO_KNOW) : EquipableItem() {
    override val isUpgradable: Boolean
        get() = false

    //todo:
    open fun uncurse() {}

    override fun doEquip(hero: Hero): Boolean {
        detach(hero.belongings.backpack)

        if (hero.belongings.helmet?.doUnequip(hero, true, false) != false) {
            hero.belongings.helmet = this

            identify()
            if (cursed) {
                equipCursed(hero)
                GLog.n(Messages.get(Helmet::class.java, "equip_cursed"))
            }

            activate(hero)

            hero.spendAndNext(time2equip(hero))
            return true
        }

        collect(hero.belongings.backpack)

        return false
    }

    override fun time2equip(hero: Hero): Float = 2f / hero.speed()

    // when equip
//    override fun activate(ch: Char) 

    open fun procGivenDamage(dmg: Damage) {}

    open fun procTakenDamage(dmg: Damage) {}

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean =
            if (super.doUnequip(hero, collect, single)) {
                hero.belongings.helmet = null
                true
            } else
                false

    override fun isEquipped(hero: Hero): Boolean = hero.belongings.helmet === this

    override fun random(): Item {
        cursed = Random.Float() < 0.3f

        return this
    }

    override fun price(): Int = if (cursedKnown && cursed) 20 else 40

    open fun viewAmend(): Int = 0

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(UNFAMILIRIARITY, ticksToKnow)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        ticksToKnow = bundle.getInt(UNFAMILIRIARITY)
    }

    companion object {
        private const val TICKS_TO_KNOW = 150
        private const val UNFAMILIRIARITY = "unfamiliarity"
    }
}