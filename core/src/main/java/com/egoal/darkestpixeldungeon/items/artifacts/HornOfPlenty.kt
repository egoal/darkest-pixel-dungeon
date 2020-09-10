/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.actors.buffs.Recharging
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.food.Blandfruit
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRecharging
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.perks.GoodAppetite
import com.egoal.darkestpixeldungeon.effects.SpellSprite
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.ArrayList
import kotlin.math.max

class HornOfPlenty : Artifact() {
    protected var mode: WndBag.Mode = WndBag.Mode.FOOD

    init {
        image = ItemSpriteSheet.ARTIFACT_HORN1

        levelCap = 30

        charge = 0
        partialCharge = 0f
        chargeCap = 10 + visiblyUpgraded()

        defaultAction = AC_EAT
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && charge > 0)
            actions.add(AC_EAT)
        if (isEquipped(hero) && level() < 30 && !cursed)
            actions.add(AC_STORE)
        return actions
    }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action == AC_EAT) {

            if (!isEquipped(hero))
                GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
            else if (charge == 0)
                GLog.i(Messages.get(this, "no_food"))
            else {
                //consume as many
                var chargesToUse = max(1, hero.buff(Hunger::class.java)!!.hunger() / (Hunger.STARVING / 10).toInt())
                if (chargesToUse > charge) chargesToUse = charge
                hero.buff(Hunger::class.java)!!.satisfy(Hunger.STARVING / 10 * chargesToUse)

                if (chargesToUse >= 3)
                    hero.heroPerk.get(GoodAppetite::class.java)?.onFoodEaten(hero, Food()) // fixme: Food() as placeholder here

                // mental
                hero.recoverSanity(Random.Int(1, chargesToUse))

                Statistics.FoodEaten = Statistics.FoodEaten + 1

                charge -= chargesToUse

                hero.sprite.operate(hero.pos)
                hero.busy()
                SpellSprite.show(hero, SpellSprite.FOOD)
                Sample.INSTANCE.play(Assets.SND_EAT)
                GLog.i(Messages.get(this, "eat"))

                hero.spend(TIME_TO_EAT)

                Badges.validateFoodEaten()

                updateImage()
                updateQuickslot()
            }

        } else if (action == AC_STORE) {
            GameScene.selectItem(itemSelector, mode, Messages.get(this, "prompt"))
        }
    }

    override fun passiveBuff() = hornRecharge()

    override fun desc(): String {
        var desc = super.desc()

        if (isEquipped(Dungeon.hero)) {
            if (!cursed) {
                if (level() < levelCap)
                    desc += "\n\n" + Messages.get(this, "desc_hint")
            } else {
                desc += "\n\n" + Messages.get(this, "desc_cursed")
            }
        }

        return desc
    }

    override fun upgrade(): Item {
        super.upgrade()
        chargeCap = 10 + visiblyUpgraded()
        return this
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        updateImage()
    }

    private fun updateImage() {
        image = when {
            charge >= 15 -> ItemSpriteSheet.ARTIFACT_HORN4
            charge >= 10 -> ItemSpriteSheet.ARTIFACT_HORN3
            charge >= 15 -> ItemSpriteSheet.ARTIFACT_HORN2
            else -> ItemSpriteSheet.ARTIFACT_HORN1
        }
    }

    inner class hornRecharge : Artifact.ArtifactBuff() {

        fun gainCharge(levelPortion: Float) {
            if (charge < chargeCap) {
                if (cursed) return
                //generates 0.25x max hunger value every hero level, +0.035x max
                // value per horn level
                //to a max of 1.3x max hunger value per hero level
                //This means that a standard ration will be recovered in ~7.15 hero
                // levels
                partialCharge += Hunger.STARVING * levelPortion * (0.25f + 0.035f * level())

                //charge is in increments of 1/10 max hunger value.
                while (partialCharge >= Hunger.STARVING / 10) {
                    charge++
                    partialCharge -= Hunger.STARVING / 10

                    updateImage()

                    if (charge == chargeCap) {
                        GLog.p(Messages.get(HornOfPlenty::class.java, "full"))
                        partialCharge = 0f
                    }

                    updateQuickslot()
                }
            } else
                partialCharge = 0f
        }

    }

    companion object {
        private const val TIME_TO_EAT = 3f

        const val AC_EAT = "EAT"
        const val AC_STORE = "STORE"

        private var itemSelector: WndBag.Listener = WndBag.Listener { item ->
            if (item != null && item is Food) {
                if (item is Blandfruit && item.potionAttrib == null) {
                    GLog.w(Messages.get(HornOfPlenty::class.java, "reject"))
                } else {
                    val hero = Dungeon.hero
                    hero.sprite.operate(hero.pos)
                    hero.busy()
                    hero.spend(TIME_TO_EAT)

                    curItem.upgrade(item.hornValue)
                    if (curItem.level() >= 30) {
                        curItem.level(30)
                        GLog.p(Messages.get(HornOfPlenty::class.java, "maxlevel"))
                    } else
                        GLog.p(Messages.get(HornOfPlenty::class.java, "levelup"))
                    item.detach(hero.belongings.backpack)
                }
            }
        }
    }
}
