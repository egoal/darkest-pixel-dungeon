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
package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.EfficientPotionOfHealing
import com.egoal.darkestpixeldungeon.effects.PerkGain
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import kotlin.math.max
import kotlin.math.min

class PotionOfHealing : Potion() {
    init {
        initials = 2

        bones = true
    }

    override fun canBeReinforced(): Boolean {
        return !reinforced
    }

    override fun apply(hero: Hero) {
        Buff.detach(hero, Bleeding::class.java)

        if (reinforced) {
            hero.HP = min(2 * hero.HT, hero.HT + hero.HP)

            Buff.detach(hero, Poison::class.java)
            Buff.detach(hero, Cripple::class.java)
            Buff.detach(hero, Weakness::class.java)
            Buff.detach(hero, Burning::class.java)

            GLog.p(M.L(this, "heal"))
            hero.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 4)

            setKnown()
            drunk(hero)
        } else
            doDrink(hero)
    }

    override fun drink(hero: Hero) {
        if (!reinforced && isKnown && hero.buff(Decayed::class.java) != null) {
            //todo:
            WndOptions.Confirm(ItemSprite(this), name, M.L(this, "decayed")) {
                detach(hero.belongings.backpack)

                hero.spend(1f)
                hero.busy()

                Buff.detach(hero, Bleeding::class.java)
                doDrink(hero)

                Sample.INSTANCE.play(Assets.SND_DRINK)

                hero.sprite.operate(hero.pos)
            }
        } else
            super.drink(hero)
    }

    private fun doDrink(hero: Hero) {
        setKnown()
        drunk(hero)

        val value = recoverValue(hero)
        // directly recover some health, since buff is act later than chars
        val directRecover = value / 4
        hero.recoverHP(directRecover, this)

        val m = hero.buff(Mending::class.java)
        if (m != null) {
            m.set(m.recoveryValue + value - directRecover)
        } else {
            Buff.affect(hero, Mending::class.java).set(value - directRecover)
        }

        hero.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 4)
    }

    private fun recoverValue(hero: Hero): Int {
        val healinglvl = hero.heroPerk.get(EfficientPotionOfHealing::class.java)?.level ?: 0

        // 1.25, 2, 3
        return when (healinglvl) {
            1 -> hero.HT * 5 / 4
            2 -> hero.HT * 2
            3 -> hero.HT * 3
            else -> max(hero.HT, hero.HT / 3 + 50) // 75
        }
    }

    override fun shatter(cell: Int) {
        super.shatter(cell)

        PathFinder.NEIGHBOURS9.map { Actor.findChar(cell + it) }.filterNotNull().forEach {
            it.recoverHP(recoverValue(Dungeon.hero))
        }
    }

    private fun drunk(hero: Hero) {
        hero.pohDrunk += 1
        if (hero.pohDrunk == 4 || hero.pohDrunk == 9 || hero.pohDrunk == 15) {
            val p = EfficientPotionOfHealing()
            if (p.isAcquireAllowed(hero)) {
                hero.heroPerk.add(p)
                PerkGain.Show(hero, p)

                GLog.p(M.L(this, "perk_gain"))
            }
        }
    }

    override fun price(): Int =
            if (isKnown) (30 * quantity * (if (reinforced) 1.5f else 1f)).toInt()
            else super.price()

    companion object {
        fun heal(hero: Hero) {
            // called in water of healing, so kept
            hero.HP = hero.HT
            Buff.detach(hero, Poison::class.java)
            Buff.detach(hero, Cripple::class.java)
            Buff.detach(hero, Weakness::class.java)
            Buff.detach(hero, Bleeding::class.java)

            hero.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 4)
        }
    }
}
