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
package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.watabou.noosa.Group

class WndClass(private val cl: HeroClass) : WndTabbed() {
    private val tabPerks: PerksTab
    private var tabMastery: MasteryTab? = null

    init {
        tabPerks = PerksTab()
        add(tabPerks)

        var tab = RankingTab(cl.title().toUpperCase(), tabPerks)
        tab.setSize(TAB_WIDTH.toFloat(), tabHeight().toFloat())
        add(tab)

        if (Badges.isUnlocked(cl.masteryBadge())) {
            tabMastery = MasteryTab()
            add(tabMastery)

            tab = RankingTab(Messages.get(this, "mastery"), tabMastery)
            add(tab)

            resize(Math.max(tabPerks.width, tabMastery!!.width).toInt(),
                    Math.max(tabPerks.height, tabMastery!!.height).toInt())
        } else {
            resize(tabPerks.width.toInt(), tabPerks.height.toInt())
        }

        layoutTabs()

        select(0)
    }

    private inner class RankingTab(label: String, private val page: Group?) : WndTabbed.LabeledTab(label) {

        override fun select(value: Boolean) {
            super.select(value)
            if (page != null) {
                page.active = selected
                page.visible = page.active
            }
        }
    }

    private inner class PerksTab : Group() {

        var height: Float = 0.toFloat()
        var width: Float = 0.toFloat()

        init {

            var dotWidth = 0f

            val items = cl.perks()
            var pos = MARGIN.toFloat()

            for (i in items.indices) {

                if (i > 0) {
                    pos += GAP.toFloat()
                }

                val dot = PixelScene.createText("-", 6f)
                dot.x = MARGIN.toFloat()
                dot.y = pos
                if (dotWidth == 0f) {
                    dot.measure()
                    dotWidth = dot.width()
                }
                add(dot)

                val item = PixelScene.renderMultiline(items[i], 6)
                item.maxWidth((WIDTH.toFloat() - (MARGIN * 2).toFloat() - dotWidth).toInt())
                item.setPos(dot.x + dotWidth, pos)
                add(item)

                pos += item.height()
                val w = item.width()
                if (w > width) {
                    width = w
                }
            }

            width += MARGIN + dotWidth
            height = pos + MARGIN
        }
    }

    private inner class MasteryTab : Group() {
        var height: Float = 0f
        var width: Float = 0f

        init {

            val message = when (cl) {
                HeroClass.WARRIOR -> HeroSubClass.GLADIATOR.desc() + "\n\n" + HeroSubClass.BERSERKER.desc()
                HeroClass.MAGE -> HeroSubClass.BATTLEMAGE.desc() + "\n\n" + HeroSubClass.WARLOCK.desc()
                HeroClass.ROGUE -> HeroSubClass.FREERUNNER.desc() + "\n\n" + HeroSubClass.ASSASSIN.desc()
                HeroClass.HUNTRESS -> HeroSubClass.SNIPER.desc() + "\n\n" + HeroSubClass.WARDEN.desc()
                HeroClass.SORCERESS -> HeroSubClass.STARGAZER.desc() + "\n\n" + HeroSubClass.WITCH.desc()
            }

            val text = PixelScene.renderMultiline(6)
            text.text(message, WIDTH - MARGIN * 2)
            text.setPos(MARGIN.toFloat(), MARGIN.toFloat())
            add(text)

            height = text.bottom() + MARGIN
            width = text.right() + MARGIN
        }
    }

    companion object {
        private const val MARGIN = 4
        private const val GAP = 4
        private const val WIDTH = 110
        private const val TAB_WIDTH = 50
    }
}