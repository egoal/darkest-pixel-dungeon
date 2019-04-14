package com.egoal.darkestpixeldungeon.items

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions

abstract class KindofMisc : EquipableItem() {
    override fun doEquip(hero: Hero): Boolean {
        if (hero.belongings.misc1 != null && hero.belongings.misc2 != null &&
                hero.belongings.misc3 != null) {

            val m1 = hero.belongings.misc1
            val m2 = hero.belongings.misc2
            val m3 = hero.belongings.misc3
            GameScene.show(
                    object : WndOptions(Messages.get(KindofMisc::class.java, "unequip_title"),
                            Messages.get(KindofMisc::class.java, "unequip_message"),
                            Messages.titleCase(m1.toString()),
                            Messages.titleCase(m2.toString()),
                            Messages.titleCase(m3.toString())) {

                        override fun onSelect(index: Int) {
                            var equipped: KindofMisc? = null
                            when (index) {
                                0 -> equipped = m1
                                1 -> equipped = m2
                                2 -> equipped = m3
                            }
                            detach(hero.belongings.backpack)
                            if (equipped!!.doUnequip(hero, true, false)) {
                                execute(hero, AC_EQUIP)
                            } else {
                                collect(hero.belongings.backpack)
                            }
                        }
                    })

            return false
        } else {
            when {
                hero.belongings.misc1 == null -> hero.belongings.misc1 = this
                hero.belongings.misc2 == null -> hero.belongings.misc2 = this
                hero.belongings.misc3 == null -> hero.belongings.misc3 = this
            }

            detach(hero.belongings.backpack)
            actions(hero)

            cursedKnown = true
            if (cursed) {
                equipCursed(hero)
                GLog.n(Messages.get(this, "cursed", this))
            }

            hero.spendAndNext(TIME_TO_EQUIP)
            return true
        }
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        if (super.doUnequip(hero, collect, single)) {
            when {
                hero.belongings.misc1 === this -> hero.belongings.misc1 = null
                hero.belongings.misc2 === this -> hero.belongings.misc2 = null
                hero.belongings.misc3 === this -> hero.belongings.misc3 = null
            }

            return true
        }

        return false
    }

    override fun isEquipped(hero: Hero): Boolean = hero.belongings.misc1 === this ||
            hero.belongings.misc2 === this || hero.belongings.misc3 === this

    companion object {
        private const val TIME_TO_EQUIP = 1f
    }

}