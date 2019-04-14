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
package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag

import java.util.ArrayList

class Sword : MeleeWeapon() {

    init {
        image = ItemSpriteSheet.SWORD

        tier = 3
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isIdentified && !cursed)
            actions.add(AC_DUAL)

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_DUAL)
            GameScene.selectItem(itemSelector, WndBag.Mode.WEAPON, Messages.get(this, "select_other"))
    }

    companion object {

        private const val AC_DUAL = "dual"

        private val itemSelector = WndBag.Listener { item ->
            if (item is Sword && Item.curItem !== item) {
                if (item.isIdentified && !item.cursed) {
                    val ps = PairSwords(Item.curItem as Sword, item)
                    if (!ps.doPickUp(Dungeon.hero))
                        Dungeon.level.drop(ps, Dungeon.hero.pos).sprite.drop()

                    if (Item.curItem.isEquipped(Dungeon.hero))
                        (Item.curItem as Sword).doUnequip(Dungeon.hero, false)
                    Item.curItem.detach(Dungeon.hero.belongings.backpack)

                    if (item.isEquipped(Dungeon.hero))
                        item.doUnequip(Dungeon.hero, false)
                    item.detach(Dungeon.hero.belongings.backpack)

                    GLog.p(Messages.get(Sword::class.java, "paired"))

                } else {
                    GLog.w(Messages.get(Sword::class.java, "not_familiar"))
                }
            }
        }
    }

}
