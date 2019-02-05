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

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle

import java.util.ArrayList

class Ankh : Item() {
    init {
        image = ItemSpriteSheet.ANKH

        // You tell the ankh no, don't revive me, and then it comes back to revive  you again in another run.
        // I'm not sure if that's enthusiasm or passive-aggression.
        bones = true
    }

    var isBlessed = false
        private set

    override fun isUpgradable(): Boolean = false
    override fun isIdentified(): Boolean = true

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)

        hero.belongings.getItem(DewVial::class.java)?.let { vial ->
            if (vial.Volume >= BLESS_CONSUME && !isBlessed)
                actions.add(AC_BLESS)
        }

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_BLESS) {
            hero.belongings.getItem(DewVial::class.java)?.let { vial ->
                isBlessed = true
                vial.Volume = vial.Volume - BLESS_CONSUME
                GLog.p(Messages.get(Ankh::class.java, "bless"))
                hero.spend(1f)
                hero.busy()

                Sample.INSTANCE.play(Assets.SND_DRINK)
                CellEmitter.get(hero.pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3)
                hero.sprite.operate(hero.pos)

                updateQuickslot()
            }
        }
    }

    override fun desc(): String = if (isBlessed) Messages.get(this, "desc_blessed")
    else super.desc()

    override fun glowing(): ItemSprite.Glowing? = if (isBlessed) WHITE else null

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(BLESSED, isBlessed)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        isBlessed = bundle.getBoolean(BLESSED)
    }

    override fun price(): Int = 50 * quantity

    companion object {
        private const val AC_BLESS = "BLESS"
        private const val BLESS_CONSUME = 20

        private val WHITE = ItemSprite.Glowing(0xFFFFCC)

        private const val BLESSED = "blessed"
    }
}
