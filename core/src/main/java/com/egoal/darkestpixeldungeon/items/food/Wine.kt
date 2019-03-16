package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Drunk
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroPerk
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.Potion.AC_DRINK
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

import java.util.ArrayList

/**
 * Created by 93942 on 5/31/2018.
 */

class Wine : Item() {
    init {
        image = ItemSpriteSheet.DPD_WINE
        defaultAction = AC_DRINK
        stackable = true
        
        identify()
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply {
        add(AC_DRINK)
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action === AC_DRINK) {
            detach(hero.belongings.backpack)
            hero.spend(TIME_TO_DRINK)
            hero.busy()

            var value = Math.min(Random.IntRange(15, (Pressure.heroPressure() * .5f).toInt()), 30)
            if (hero.heroPerk.contain(HeroPerk.Perk.DRUNKARD)) {
                value += value / 5
                hero.recoverSanity(value)
            } else {
                hero.recoverSanity(value)
                // get drunk
                Buff.prolong(hero, Drunk::class.java, Drunk.duration(hero))
                hero.takeDamage(Damage(hero.HP / 4, this, hero).type(Damage.Type
                        .MAGICAL).addFeature(Damage.Feature.PURE))
            }

            hero.sprite.operate(hero.pos)
            GLog.i(Messages.get(this, "drunk"))
            Sample.INSTANCE.play(Assets.SND_DRINK)
        }
    }

    override fun price(): Int = 20 * quantity()

    override fun isUpgradable(): Boolean = false

    companion object {
        private const val AC_DRINK = "drink"
        private const val TIME_TO_DRINK = 2f
    }


}
