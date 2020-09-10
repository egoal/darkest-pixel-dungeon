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
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.BattleGloves
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.IconTitle
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.audio.Sample

import java.util.ArrayList

class Weightstone : Item() {

    private val itemSelector = WndBag.Listener { item ->
        if (item != null) {
            GameScene.show(WndBalance(item as Weapon))
        }
    }

    init {
        image = ItemSpriteSheet.WEIGHT

        stackable = true

        bones = true
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_APPLY)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_APPLY) {
            GameScene.selectItem(itemSelector, M.L(this, "select"), WndBag.Filter {
                WndBag.FilterByMode(it, WndBag.Mode.WEAPON) && it !is BattleGloves
            })
        }
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    private fun apply(weapon: Weapon, forSpeed: Boolean) {

        detach(curUser.belongings.backpack)

        if (forSpeed) {
            weapon.imbue = if (weapon.imbue == Weapon.Imbue.HEAVY) Weapon.Imbue.NONE else Weapon.Imbue.LIGHT
            GLog.p(Messages.get(this, "light"))
        } else {
            weapon.imbue = if (weapon.imbue == Weapon.Imbue.LIGHT) Weapon.Imbue.NONE else Weapon.Imbue.HEAVY
            GLog.p(Messages.get(this, "heavy"))
        }

        curUser.sprite.operate(curUser.pos)
        Sample.INSTANCE.play(Assets.SND_MISS)

        curUser.spend(TIME_TO_APPLY)
        curUser.busy()
    }

    override fun price(): Int {
        return 50 * quantity
    }

    inner class WndBalance(weapon: Weapon) : Window() {
        init {
            val titlebar = IconTitle(weapon)
            titlebar.setRect(0f, 0f, WIDTH.toFloat(), 0f)
            add(titlebar)

            val tfMesage = PixelScene.renderMultiline(Messages
                    .get(this, "choice"), 8)
            tfMesage.maxWidth(WIDTH - MARGIN * 2)
            tfMesage.setPos(MARGIN.toFloat(), titlebar.bottom() + MARGIN)
            add(tfMesage)

            var pos = tfMesage.top() + tfMesage.height()

            if (weapon.imbue != Weapon.Imbue.LIGHT) {
                val btnSpeed = object : RedButton(Messages.get(this, "light")) {
                    override fun onClick() {
                        hide()
                        this@Weightstone.apply(weapon, true)
                    }
                }
                btnSpeed.setRect(MARGIN.toFloat(), pos + MARGIN, BUTTON_WIDTH.toFloat(), BUTTON_HEIGHT.toFloat())
                add(btnSpeed)

                pos = btnSpeed.bottom()
            }

            if (weapon.imbue != Weapon.Imbue.HEAVY) {
                val btnAccuracy = object : RedButton(Messages.get(this, "heavy")) {
                    override fun onClick() {
                        hide()
                        this@Weightstone.apply(weapon, false)
                    }
                }
                btnAccuracy.setRect(MARGIN.toFloat(), pos + MARGIN, BUTTON_WIDTH.toFloat(), BUTTON_HEIGHT.toFloat())
                add(btnAccuracy)

                pos = btnAccuracy.bottom()
            }

            val btnCancel = object : RedButton(Messages.get(this, "cancel")) {
                override fun onClick() {
                    hide()
                }
            }
            btnCancel.setRect(MARGIN.toFloat(), pos + MARGIN, BUTTON_WIDTH.toFloat(), BUTTON_HEIGHT.toFloat())
            add(btnCancel)

            resize(WIDTH, btnCancel.bottom().toInt() + MARGIN)
        }
    }

    companion object {
        private const val WIDTH = 120
        private const val MARGIN = 2
        private const val BUTTON_WIDTH = WIDTH - MARGIN * 2
        private const val BUTTON_HEIGHT = 20

        private const val TIME_TO_APPLY = 2f

        private const val AC_APPLY = "APPLY"
    }
}