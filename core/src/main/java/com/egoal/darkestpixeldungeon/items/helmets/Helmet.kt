package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KindofMisc
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random


open class Helmet : EquipableItem() {

    override fun doEquip(hero: Hero): Boolean {
        detach(hero.belongings.backpack)

        if (hero.belongings.helmet == null || hero.belongings.helmet.doUnequip(hero, true, false)) {
            hero.belongings.helmet = this

            cursedKnown = true
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

    protected var buff: Buff? = null

    // when equip
    override fun activate(ch: Char) {
        buff = buff()
        buff!!.attachTo(ch)
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean =
            if (super.doUnequip(hero, collect, single)) {
                hero.belongings.helmet = null

                hero.remove(buff!!)
                buff = null
                true
            } else
                false

    override fun isEquipped(hero: Hero): Boolean = hero.belongings.helmet === this

    override fun random(): Item {
        cursed = Random.Float() < 0.3f

        return this
    }

    override fun isUpgradable(): Boolean = false

    override fun price(): Int = if (cursedKnown && cursed) 20 else 40

    // this is what we call helmets!
    open fun procGivenDamage(dmg: Damage): Damage = dmg

    open fun procTakenDamage(dmg: Damage): Damage = dmg

    open fun viewAmend(): Int = 0

    protected open fun buff(): HelmetBuff = HelmetBuff()

    // default buff, increase pressure when cursed
    open inner class HelmetBuff : Buff() {
        val Cursed: Boolean get() = cursed

        override fun act(): Boolean {
            if (cursed && Random.Int(10) == 0)
                target.takeDamage(Damage(1, Char.Nobody(), target).type(Damage.Type.MENTAL))

            return super.act()
        }
    }


}