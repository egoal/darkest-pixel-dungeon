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
package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Berserk
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.actors.hero.perks.Assassin
import com.egoal.darkestpixeldungeon.actors.hero.perks.Fearless
import com.egoal.darkestpixeldungeon.actors.hero.perks.Optimistic
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.Astrolabe
import com.egoal.darkestpixeldungeon.items.artifacts.UrnOfShadow
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndChooseWay
import com.egoal.darkestpixeldungeon.effects.SpellSprite
import com.watabou.noosa.audio.Sample

import java.util.ArrayList

class TomeOfMastery : Item() {
    init {
        stackable = false
        image = ItemSpriteSheet.MASTERY

        unique = true
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_READ) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_READ) {

            curUser = hero

            val twoWay = when (hero.heroClass) {
                HeroClass.WARRIOR -> Pair(HeroSubClass.GLADIATOR, HeroSubClass.BERSERKER)
                HeroClass.MAGE -> Pair(HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK)
                HeroClass.ROGUE -> Pair(HeroSubClass.FREERUNNER, HeroSubClass.ASSASSIN)
                HeroClass.HUNTRESS -> Pair(HeroSubClass.SNIPER, HeroSubClass.WARDEN)
                HeroClass.SORCERESS -> Pair(HeroSubClass.STARGAZER, HeroSubClass.WITCH)
                HeroClass.EXILE -> Pair(HeroSubClass.LANCER, HeroSubClass.WINEBIBBER)
            }

            GameScene.show(WndChooseWay(this, twoWay.first, twoWay.second))
        }
    }

    override fun doPickUp(hero: Hero): Boolean {
        Badges.validateMastery()
        return super.doPickUp(hero)
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    fun choose(way: HeroSubClass) {
        detach(curUser.belongings.backpack)

        with(curUser) {
            spend(TIME_TO_READ)
            busy()

            subClass = way
            sprite.operate(pos)
        }

        Sample.INSTANCE.play(Assets.SND_MASTERY)
        SpellSprite.show(curUser, SpellSprite.MASTERY)
        curUser.sprite.emitter().burst(Speck.factory(Speck.MASTERY), 12)

        GLog.w(Messages.get(this, "way", way.title()))

        // on choose
        when (way) {
            HeroSubClass.BERSERKER -> {
                Buff.affect(curUser, Berserk::class.java)
                curUser.heroPerk.add(Fearless())
            }
            HeroSubClass.ASSASSIN -> curUser.heroPerk.add(Assassin())
            HeroSubClass.WARLOCK -> {
                val uos = UrnOfShadow().identify()
                if (uos.doPickUp(curUser)) GLog.w(Messages.get(curUser, "you_now_have", uos.name()))
                else Dungeon.level.drop(uos, curUser.pos).sprite.drop()
            }
            HeroSubClass.WITCH -> {
                curUser.belongings.getItem(ExtractionFlask::class.java)?.reinforce()
                //^ may lose perk
            }
            HeroSubClass.STARGAZER -> {
                curUser.heroPerk.add(Optimistic())

                val a = Astrolabe().identify()
                if (a.doPickUp(curUser)) GLog.w(Messages.get(curUser, "you_now_have", a.name()))
                else Dungeon.level.drop(a, curUser.pos).sprite.drop()
            }
        }

    }

    companion object {

        private const val TIME_TO_READ = 10f

        private const val AC_READ = "READ"
    }
}
