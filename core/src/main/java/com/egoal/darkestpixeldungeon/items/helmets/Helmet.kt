package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.KindofMisc
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog


open class Helmet : KindofMisc() {
    protected var buff: Buff? = null

    // when equip
    override fun activate(ch: Char) {
        buff = buff()
        buff!!.attachTo(ch)
    }

    override fun doEquip(hero: Hero): Boolean {
        if(listOf(hero.belongings.misc1, hero.belongings.misc2, hero.belongings.misc3).any{it is Helmet}){
            GLog.w(Messages.get(Helmet::class.java, "cannot-wear-two"))
            return false
        }
        
        return if (super.doEquip(hero)) {
            identify()
            true
        } else false
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean =
            if (super.doUnequip(hero, collect, single)) {
                hero.remove(buff)
                buff = null
                true
            } else
                false

    protected open fun buff(): HelmetBuff = HelmetBuff()

    open class HelmetBuff : Buff()

    override fun isUpgradable(): Boolean = false
    
    override fun price(): Int = if(cursedKnown && cursed) 20 else 40
}