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
package com.egoal.darkestpixeldungeon.actors.hero

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Berserk
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Circulation
import com.egoal.darkestpixeldungeon.actors.buffs.Penetration
import com.egoal.darkestpixeldungeon.actors.hero.perks.Assassin
import com.egoal.darkestpixeldungeon.actors.hero.perks.Fearless
import com.egoal.darkestpixeldungeon.actors.hero.perks.Optimistic
import com.egoal.darkestpixeldungeon.actors.hero.perks.PolearmMaster
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.SpellSprite
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.Astrolabe
import com.egoal.darkestpixeldungeon.items.special.StrengthOffering
import com.egoal.darkestpixeldungeon.items.special.UrnOfShadow
import com.egoal.darkestpixeldungeon.items.unclassified.ExtractionFlask
import com.egoal.darkestpixeldungeon.items.unclassified.TomeOfMastery
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle

enum class HeroSubClass(private val title: String) {
    NONE(""),

    GLADIATOR("gladiator"),
    BERSERKER("berserker"),

    WARLOCK("warlock"),
    BATTLEMAGE("battlemage"),
    ARCHMAGE("archmage"),

    ASSASSIN("assassin"),
    FREERUNNER("freerunner"),

    SNIPER("sniper"),
    WARDEN("warden"),

    STARGAZER("stargazer"),
    WITCH("witch"),

    LANCER("lancer"),
    WINEBIBBER("winebibber");

    fun title(): String = Messages.get(this, title)

    fun desc(): String = Messages.get(this, title + "_desc")

    fun storeInBundle(bundle: Bundle) {
        bundle.put(SUBCLASS, toString())
    }

    companion object {

        private const val SUBCLASS = "subClass"

        fun RestoreFromBundle(bundle: Bundle): HeroSubClass = valueOf(bundle.getString(SUBCLASS))

        fun Choose(hero: Hero, way: HeroSubClass) {
            hero.subClass = way

            Sample.INSTANCE.play(Assets.SND_MASTERY)
            SpellSprite.show(hero, SpellSprite.MASTERY)
            hero.sprite.emitter().burst(Speck.factory(Speck.MASTERY), 12)

            GLog.w(M.L(TomeOfMastery::class.java, "way", way.title()))

            // on choose
            when (way) {
                BERSERKER -> {
                    Buff.affect(hero, Berserk::class.java)
                    hero.heroPerk.add(Fearless())
                }
                ASSASSIN -> hero.heroPerk.add(Assassin())
                WARLOCK -> {
                    val uos = UrnOfShadow().identify()
                    uos.collect()
                    GLog.w(Messages.get(hero, "you_now_have", uos.name()))
                }
                BATTLEMAGE -> Buff.affect(hero, Circulation::class.java)
                ARCHMAGE -> {
                    val so = StrengthOffering().identify()
                    so.collect()
                    GLog.w(Messages.get(hero, "you_now_have", so.name()))
                }
                WITCH -> {
                    hero.belongings.getItem(ExtractionFlask::class.java)?.reinforce()
                    //^ may lose perk
                }
                STARGAZER -> {
                    hero.heroPerk.add(Optimistic())

                    val a = Astrolabe().identify()
                    if (a.doPickUp(hero)) GLog.w(Messages.get(hero, "you_now_have", a.name()))
                    else Dungeon.level.drop(a, hero.pos).sprite.drop()
                }
                LANCER -> {
                    hero.heroPerk.add(PolearmMaster())
                    Buff.affect(hero, Penetration::class.java)
                }
            }
        }
    }

}
