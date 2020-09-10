/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.mobs.Bee
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.tweeners.AlphaTweener
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.ArrayList

class Honeypot : Item() {

    override val isUpgradable: Boolean
        get() = false

    override val isIdentified: Boolean
        get() = true

    init {
        image = ItemSpriteSheet.HONEYPOT

        defaultAction = AC_THROW
        usesTargeting = true

        stackable = true
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_SHATTER)
        return actions
    }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action == AC_SHATTER) {

            hero.sprite.zap(hero.pos)

            detach(hero.belongings.backpack)

            shatter(hero, hero.pos).collect()

            hero.next()

        }
    }

    override fun onThrow(cell: Int) {
        if (Level.pit[cell]) {
            super.onThrow(cell)
        } else {
            Dungeon.level.drop(shatter(null, cell), cell)
        }
    }

    fun shatter(owner: Char?, pos: Int): Item {

        if (Dungeon.visible[pos]) {
            Sample.INSTANCE.play(Assets.SND_SHATTER)
            Splash.at(pos, 0xffd500, 5)
        }

        var newPos = pos
        if (Actor.findChar(pos) != null) {
            val candidates = ArrayList<Int>()
            val passable = Level.passable

            for (n in PathFinder.NEIGHBOURS4) {
                val c = pos + n
                if (passable[c] && Actor.findChar(c) == null) {
                    candidates.add(c)
                }
            }

            newPos = if (candidates.size > 0) Random.element(candidates) else -1
        }

        if (newPos != -1) {
            val bee = Bee()
            bee.spawn(Dungeon.depth)
            bee.setPotInfo(pos, owner)
            bee.HP = bee.HT
            bee.pos = newPos

            GameScene.add(bee)
            Actor.addDelayed(Pushing(bee, pos, newPos), -1f)

            bee.sprite.alpha(0f)
            bee.sprite.parent.add(AlphaTweener(bee.sprite, 1f, 0.15f))

            Sample.INSTANCE.play(Assets.SND_BEE)
            return ShatteredPot().setBee(bee)
        } else {
            return this
        }
    }

    override fun price(): Int {
        return 30 * quantity
    }

    //The bee's broken 'home', all this item does is let its bee know where it
    // is, and who owns it (if anyone).
    class ShatteredPot : Item() {

        private var myBee: Int = 0
        private var beeDepth: Int = 0

        override val isUpgradable: Boolean
            get() = false

        override val isIdentified: Boolean
            get() = true

        init {
            image = ItemSpriteSheet.SHATTPOT
            stackable = false
        }

        fun setBee(bee: Char): Item {
            myBee = bee.id()
            beeDepth = Dungeon.depth
            return this
        }

        override fun doPickUp(hero: Hero): Boolean {
            if (super.doPickUp(hero)) {
                setHolder(hero)
                return true
            } else
                return false
        }

        override fun doDrop(hero: Hero) {
            super.doDrop(hero)
            updateBee(hero.pos, null)
        }

        override fun onThrow(cell: Int) {
            super.onThrow(cell)
            updateBee(cell, null)
        }

        fun setHolder(holder: Char) {
            updateBee(holder.pos, holder)
        }

        fun goAway() {
            updateBee(-1, null)
        }

        private fun updateBee(cell: Int, holder: Char?) {
            //important, as ids are not unique between depths.
            if (Dungeon.depth != beeDepth)
                return

            val bee = Actor.findById(myBee) as Bee?
            bee?.setPotInfo(cell, holder)
        }

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put(MYBEE, myBee)
            bundle.put(BEEDEPTH, beeDepth)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            myBee = bundle.getInt(MYBEE)
            beeDepth = bundle.getInt(BEEDEPTH)
        }

        companion object {
            private const val MYBEE = "mybee"
            private const val BEEDEPTH = "beedepth"
        }
    }

    companion object {
        const val AC_SHATTER = "SHATTER"
    }
}
