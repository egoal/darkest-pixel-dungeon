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
package com.egoal.darkestpixeldungeon.items.scrolls

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.hero.perks.IntendedTransportation
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.HeroSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.tweeners.AlphaTweener

class ScrollOfTeleportation : Scroll() {
    init {
        initials = 9
    }

    override fun doRead() {
        Sample.INSTANCE.play(Assets.SND_READ)
        Invisibility.dispel()

        setKnown()

        if (Dungeon.bossLevel()) {
            GLog.w(Messages.get(this, "no_tele"))
            return
        }

        if (Item.curUser.heroPerk.has(IntendedTransportation::class.java))
            IntendTeleportHero(Item.curUser)
        else {
            teleportHero(Item.curUser)
            readAnimation()
        }
    }

    override fun price(): Int = if (isKnown) 30 * quantity else super.price()

    companion object {

        fun teleportHero(hero: Hero) {
            if (Dungeon.bossLevel()) {
                GLog.w(M.L(ScrollOfTeleportation::class.java, "no_tele"))
                return
            }

            var pos = -1
            for (i in 1..10) {
                pos = Dungeon.level.randomRespawnCell()
                if (pos != -1) break
            }

            if (pos == -1)
                GLog.w(M.L(ScrollOfTeleportation::class.java, "no_tele"))
            else {

                appear(hero, pos)
                Dungeon.level.press(pos, hero)
                Dungeon.observe()
                GameScene.updateFog()

                GLog.i(M.L(ScrollOfTeleportation::class.java, "tele"))

            }
        }

        fun IntendTeleportHero(hero: Hero) {
            GameScene.selectCell(selectorDst)
        }

        private val selectorDst = object : CellSelector.Listener {
            override fun onSelect(cell: Int?) {
                if (cell == null)
                    teleportHero(Item.curUser)
                else if (Dungeon.level.visited[cell] || Dungeon.level.mapped[cell]) {
                    if (Level.solid[cell] || Actor.findChar(cell) != null) return

                    appear(Item.curUser, cell)
                    Dungeon.level.press(cell, Item.curUser)
                    Dungeon.observe()
                    GameScene.updateFog()

                    GLog.i(M.L(ScrollOfTeleportation::class.java, "tele"))

                    // read animation...
                    Item.curUser.spend(Scroll.TIME_TO_READ)
                    Item.curUser.busy()
                    (Item.curUser.sprite as HeroSprite).read()
                }
            }

            override fun prompt(): String {
                return M.L(ScrollOfTeleportation::class.java, "select-destination")
            }
        }

        fun appear(ch: Char, pos: Int) {
            ch.sprite.interruptMotion()

            ch.move(pos)
            ch.sprite.place(pos)

            if (ch.invisible == 0) {
                if (ch.sprite.parent != null) {
                    // ^^^ null check: the char may be removed by a WarpingTrap or sth, in the ch.move call
                    ch.sprite.alpha(0f)
                    ch.sprite.parent.add(AlphaTweener(ch.sprite, 1f, 0.4f))
                }
            }

            ch.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.2f, 3)
            Sample.INSTANCE.play(Assets.SND_TELEPORT)
        }
    }
}
