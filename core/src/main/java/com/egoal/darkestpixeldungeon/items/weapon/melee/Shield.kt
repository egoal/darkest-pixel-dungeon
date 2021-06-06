package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.round

abstract class Shield : MeleeWeapon() {
    override fun info(): String = if (isIdentified)
        super.info() + "\n\n" + M.L(Shield::class.java, "block", def(0), def(level()))
    else super.info()

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply {
        if (hero.buff(ShieldsUp::class.java) == null) add(AC_SHLD_UP)
        else add(AC_SHLD_DOWN)
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_SHLD_UP) {
            if (!isEquipped(hero)) GLog.w(M.L(this, "need_to_equip"))
            else if (cursed) GLog.n(M.L(this, "cursed"))
            else hero.doOperation(1f) { Buff.affect(hero, ShieldsUp::class.java) }
        } else if (action == AC_SHLD_DOWN) hero.buff(ShieldsUp::class.java)?.detach()
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        val re = super.doUnequip(hero, collect, single)
        if (re) hero.buff(ShieldsUp::class.java)?.detach()
        return re
    }

    protected abstract fun def(level: Int): Int

    private fun checkDefend(dmg: Damage): Boolean {
        if (dmg.from is Char && dmg.to is Char) {
            val attacker = dmg.from as Char
            val defender = dmg.to as Char

            val r = if (defender.buff(ShieldsUp::class.java) != null) 1.5f else 1f

            return Random.Float(attacker.atkSkill) < Random.Float(defender.defSkill * r)
        }

        return false
    }

    override fun defendDamage(dmg: Damage): Damage {
        val value = if (checkDefend(dmg)) {
            Sample.INSTANCE.play(Assets.SND_BLOCK)
            def(level())
        } else def(0)

        return defendValue(dmg, value)
    }

    protected open fun defendValue(dmg: Damage, defValue: Int): Damage {
        var value = defValue
        if (dmg.to is Hero) {
            val burden = STRReq() - (dmg.to as Hero).STR()
            if (burden > 0) value -= 2 * burden
        }

        if (value > 0) {
            if (dmg.isFeatured(Damage.Feature.RANGED))
                value += round(value / 5f).toInt()

            if (dmg.type == Damage.Type.NORMAL)
                dmg.value -= value
            else if (dmg.type == Damage.Type.MAGICAL)
                dmg.value -= value * 2 / 3
        }

        return dmg
    }

    companion object {
        private const val AC_SHLD_UP = "up"
        private const val AC_SHLD_DOWN = "down"

        class ShieldsUp : Buff() {
            init {
                type = buffType.NEUTRAL
            }

            override fun toString(): String = M.L(Shield::class.java, "shields_up")

            override fun icon(): Int = BuffIndicator.SHIELDS_UP

            override fun desc(): String = M.L(Shield::class.java, "shields_up_desc")
        }
    }
}