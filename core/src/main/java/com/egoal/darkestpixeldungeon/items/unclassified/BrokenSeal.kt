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

import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndItem
import com.watabou.noosa.audio.Sample

import java.util.ArrayList

class BrokenSeal : Item() {

    init {
        image = ItemSpriteSheet.SEAL

        levelKnown = true
        cursedKnown = levelKnown
        unique = true
        bones = false

        defaultAction = AC_INFO
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_AFFIX) }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action == AC_AFFIX) {
            curItem = this
            GameScene.selectItem(armorSelector, WndBag.Mode.ARMOR, Messages.get(this, "prompt"))
        } else if (action == AC_INFO) {
            GameScene.show(WndItem(null, this, true))
        }
    }

    // scroll of upgrade can be used directly once, 
    // same as upgrading armor the seal is affixed to then removing it.
    override fun isUpgradable(): Boolean = level() == 0

    class WarriorShield : Buff() {

        private var armor: Armor? = null
        private var partialShield: Float = 0.toFloat()

        override fun act(): Boolean {
            if (armor == null)
                detach()
            else if (armor!!.isEquipped(target as Hero)) {
                if (target.SHLD < maxShield()) {
                    partialShield += (1 / (35 * Math.pow(0.885, (maxShield() - target.SHLD - 1).toDouble()))).toFloat()
                }
            }
            while (partialShield >= 1) {
                target.SHLD++
                partialShield--
            }
            spend(Actor.TICK)
            return true
        }

        fun setArmor(arm: Armor) {
            armor = arm
        }

        fun maxShield(): Int = 3 + armor!!.tier + armor!!.level()
    }

    companion object {
        const val AC_AFFIX = "AFFIX"
        //only to be used from the quickslot, for tutorial purposes mostly.
        const val AC_INFO = "INFO_WINDOW"

        private var armorSelector: WndBag.Listener = WndBag.Listener { item ->
            if (item != null && item is Armor) {
                if (!item.levelKnown) {
                    GLog.w(Messages.get(BrokenSeal::class.java, "unknown_armor"))
                } else if (item.cursed || item.level() < 0) {
                    GLog.w(Messages.get(BrokenSeal::class.java, "degraded_armor"))
                } else {
                    GLog.p(Messages.get(BrokenSeal::class.java, "affix"))
                    Dungeon.hero.sprite.operate(Dungeon.hero.pos)
                    Sample.INSTANCE.play(Assets.SND_UNLOCK)
                    item.affixSeal(curItem as BrokenSeal)
                    Item.curItem.detach(Dungeon.hero.belongings.backpack)
                    Badges.validateTutorial()
                }
            }
        }
    }
}
