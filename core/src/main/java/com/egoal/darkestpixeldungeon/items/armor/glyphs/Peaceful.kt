package com.egoal.darkestpixeldungeon.items.armor.glyphs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.ArmorExpose
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Bundle
import kotlin.math.floor
import kotlin.math.min

class Peaceful : Armor.Glyph() {
    override fun proc(armor: Armor, damage: Damage): Damage {
        if (damage.to is Hero) broken(damage.to as Hero)
        return damage
    }

    override fun glowing(): ItemSprite.Glowing = TEAL

    fun broken(hero: Hero) {
        Buff.prolong(hero, ArmorExpose::class.java, DURATION)
//        ch.buff(PeaceReg::class.java)?.broken = DURATION
        Buff.affect(hero, PeaceReg::class.java).apply {
            broken = DURATION
            setArmor(hero.belongings.armor)
        }
    }

    companion object {
        private val TEAL = ItemSprite.Glowing(0x239a1d)

        private const val DURATION = 10f
    }

    //fixme: not work in many situations
    class PeaceReg : Buff() {
        var broken = 0f
        private var dreg = 0f

        private var armor: Armor? = null

        fun setArmor(armor: Armor?) {
            this.armor = armor
        }

        override fun act(): Boolean {
            if (broken <= 0f && armor != null) {
                dreg += 0.5f + armor!!.level() * 0.5f
                if(dreg>=1f){
                    val dr = floor(dreg).toInt()
                    dreg -= dr
                    target.HP = min(target.HT, target.HP +dr)
                }

            } else {
                broken -= TICK
            }

            spend(TICK)

            return true
        }

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put("broken", broken)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            broken = bundle.getFloat("broken")
        }
    }
}