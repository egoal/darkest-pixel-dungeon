package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.actors.hero.perks.GoodAppetite
import com.egoal.darkestpixeldungeon.effects.BubbleText
import com.egoal.darkestpixeldungeon.effects.SpellSprite
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random
import java.util.ArrayList

open class Food(val enery: Float = Hunger.HUNGRY,
                val hornValue: Int = 3) : Item() {
    init {
        stackable = true
        image = ItemSpriteSheet.RATION

        bones = true
        defaultAction = AC_EAT
    }

    val message: String = Messages.get(this, "eat_msg")

    override val isUpgradable: Boolean
        get() = false

    override val isIdentified: Boolean
        get() = true

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_EAT) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_EAT) {
            detach(hero.belongings.backpack)

            onEat(hero)

            hero.sprite.operate(hero.pos)
            hero.busy()
            SpellSprite.show(hero, SpellSprite.FOOD)
            Sample.INSTANCE.play(Assets.SND_EAT)

            hero.spend(TIME_TO_EAT)

            Statistics.FoodEaten++
            Badges.validateFoodEaten()

            hero.say(message)
        }
    }

    protected open fun onEat(hero: Hero) {
        hero.buff(Hunger::class.java)!!.satisfy(enery)
        hero.heroPerk.get(GoodAppetite::class.java)?.onFoodEaten(hero, this)
        hero.recoverSanity(Random.Float(2f, 6f))
    }

    override fun price(): Int = 10 * quantity

    companion object {
        private const val TIME_TO_EAT = 3f

        const val AC_EAT = "EAT"
    }

}