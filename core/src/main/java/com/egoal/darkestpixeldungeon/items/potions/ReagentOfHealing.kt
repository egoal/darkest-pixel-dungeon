package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.utils.GameMath
import com.watabou.utils.Random
import kotlin.math.round

class ReagentOfHealing : Reagent(true) {
    init {
        image = ItemSpriteSheet.REAGENT_HEALING
    }

    override fun drink(hero: Hero) {
        if (hero.HP >= hero.HT / 2) {
            WndOptions.Confirm(name, M.L(ReagentOfHealing::class.java, "uneconomic")) {
                super.drink(hero)
                apply(hero)
            }
        } else {
            super.drink(hero)
            apply(hero)
        }
    }

    private fun apply(hero: Hero) {
        Buff.detach(hero, Bleeding::class.java)

        val ratio = hero.HP.toFloat() / hero.HT
        val amount = GameMath.clamp(round((1f - 2f * ratio) * hero.HT).toInt(), 1, 50) // usually, recover to about 1f-ratio

        hero.recoverHP(amount, this)

        if (hero.isAlive && ratio < 0.15f) {
            hero.sayShort(HeroLines.SAVED_ME)
            hero.recoverSanity(Random.Float(2f, 6f - ratio / 0.05f)) // 2~3 +
        }
    }
}