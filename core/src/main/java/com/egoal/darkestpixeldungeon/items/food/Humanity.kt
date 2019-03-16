package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog

import java.util.ArrayList
import kotlin.math.min

/**
 * Created by 93942 on 5/13/2018.
 */

class Humanity : Item() {

    init {
        image = ItemSpriteSheet.DPD_HUMANITY
        defaultAction = AC_CONSUME
        stackable = true

        identify()
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply {
        add(AC_CONSUME)
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action === AC_CONSUME) {
            //todo: add effects

            //0. detach
            detach(hero.belongings.backpack)
            hero.spend(TIME_TO_CONSUME)
            hero.busy()

            //1. recover sanity
            hero.recoverSanity((Pressure.heroPressure() * 0.5f).toInt())
            //todo: show effects

            //2. recover hp
            hero.HP = min(hero.HT, hero.HP + hero.HT / 4)

            hero.sprite.operate(hero.pos)
            GLog.p(Messages.get(this, "used"))
        }
    }

    override fun isUpgradable(): Boolean = false

    companion object {
        private const val AC_CONSUME = "CONSUME"
        private const val TIME_TO_CONSUME = 1f
    }

}
