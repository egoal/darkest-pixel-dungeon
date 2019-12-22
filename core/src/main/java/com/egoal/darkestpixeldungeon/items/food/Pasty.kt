package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.actors.buffs.Recharging
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRecharging
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import java.util.*

class Pasty : Food(Hunger.STARVING, 5) {
    private enum class Holiday {
        NONE,
        EASTER, //TBD
        HALLOWEEN,//TBD
        XMAS //3rd week of december through first week of january
    }

    init {
        image = ItemSpriteSheet.PASTY
        bones = true
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_EAT) {
            when (holiday) {
                Holiday.XMAS -> {
                    Buff.affect(hero, Recharging::class.java, 2f) //half of a charge
                    ScrollOfRecharging.charge(hero)
                }
                else -> {
                }
            }
        }
    }

    override fun name(): String = when (holiday) {
        Holiday.XMAS -> M.L(this, "cane")
        else -> M.L(this, "pasty")
    }

    override fun image(): Int = when (holiday) {
        Holiday.XMAS -> ItemSpriteSheet.CANDY_CANE
        else -> ItemSpriteSheet.PASTY
    }

    override fun info(): String = when (holiday) {
        Holiday.XMAS -> M.L(this, "cane_desc")
        else -> M.L(this, "pasty_desc")
    }

    override fun price(): Int = 20 * quantity

    companion object {
        private val holiday: Holiday = run {
            val calendar = Calendar.getInstance()
            when {
                (calendar.get(Calendar.MONTH) + 1) == 1 && calendar.get(Calendar.WEEK_OF_MONTH) == 1 -> Holiday.XMAS
                (calendar.get(Calendar.MONTH) + 1) == 12 && calendar.get(Calendar.WEEK_OF_MONTH) >= 3 -> Holiday.XMAS
                else -> Holiday.NONE
            }
        }
    }

}