package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.unclassified.EmptyBottle
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.audio.Sample
import com.watabou.utils.GameMath
import com.watabou.utils.Random
import java.util.*
import kotlin.math.round

class ReagentOfHealing : Item() {
    init {
//        initials = 0
        bones = true

        image = ItemSpriteSheet.HEAL_REAGENT

        defaultAction = AC_DRINK
        stackable = true
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_DRINK)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_DRINK) {
            if (hero.HP >= hero.HT / 2) {
                WndOptions.Confirm(name, M.L(ReagentOfHealing::class.java, "uneconomic")) {
                    drink(hero)
                }
            } else drink(hero)
        }
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    override fun price(): Int = 15 * quantity

    private fun drink(hero: Hero) {
        detach(hero.belongings.backpack)
        EmptyBottle.produce()

        hero.spend(1f)
        hero.busy()
        apply(hero)

        Sample.INSTANCE.play(Assets.SND_DRINK)

        hero.sprite.operate(hero.pos)
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

    companion object {
        private const val AC_DRINK = "DRINK"
    }
}