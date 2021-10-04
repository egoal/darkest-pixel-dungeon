package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.buffs.Drunk
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.actors.hero.perks.Drunkard
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random
import java.util.*
import kotlin.math.min

/**
 * Created by 93942 on 5/31/2018.
 */

private const val AC_DRINK = "drink"
private const val TIME_TO_DRINK = 2f

open class Wine(val gourdValue: Int = 5) : Item() {
    init {
        image = ItemSpriteSheet.DPD_WINE
        defaultAction = AC_DRINK
        stackable = true
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply {
        add(AC_DRINK)
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action === AC_DRINK) {
            if (hero.subClass == HeroSubClass.WINEBIBBER) {
                hero.say(M.L(Wine::class.java, "boring"))
                return
            }

            detach(hero.belongings.backpack)
            hero.spend(TIME_TO_DRINK)
            hero.busy()

            var value = recoverValue(hero)
            if (hero.heroPerk.get(Drunkard::class.java) != null) {
                value += value / 4
                hero.recoverSanity(value)

                if (this is BrownAle)
                    hero.sayShort(HeroLines.AWFUL)
            } else {
                hero.recoverSanity(value)
                // get drunk
                Drunk.Affect(hero)
                // hero.takeDamage(Damage(hero.HP / 4, this, hero).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE))
            }

            onDrunk(hero)

            hero.sprite.operate(hero.pos)
            GLog.i(Messages.get(this, "drunk"))
            Sample.INSTANCE.play(Assets.SND_DRINK)
        }
    }

    protected open fun onDrunk(hero: Hero) {}

    protected open fun recoverValue(hero: Hero): Float = min(Random.Float(15f, hero.pressure.pressure * 0.4f), 30f)

    override fun price(): Int = 20 * quantity()
}

class BrownAle : Wine(3) {
    init {
        image = ItemSpriteSheet.BROWN_ALE
    }

    override fun recoverValue(hero: Hero): Float = min(Random.Float(10f, hero.pressure.pressure * 0.3f), 22.5f)

    override fun price(): Int = 15 * quantity()
}

class RiceWine : Wine(3) {
    init {
        image = ItemSpriteSheet.NULLWARN
    }

    override fun recoverValue(hero: Hero): Float = min(Random.Float(10f, hero.pressure.pressure * 0.25f), 20f)

    override fun onDrunk(hero: Hero) {
        hero.buff(Hunger::class.java)!!.satisfy(Hunger.STARVING - Hunger.HUNGRY)
    }

    override fun price(): Int = 20 * quantity()
}