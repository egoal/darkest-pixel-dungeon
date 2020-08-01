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

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Challenge
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.utils.Random

class ScrollOfUpgrade : InventoryScroll() {

    init {
        initials = 11
        mode = WndBag.Mode.UPGRADEABLE

        bones = true
    }

    public override fun doRead() {
        if (!isKnown) {
            setKnown()
            identifiedByUse = true
        } else {
            identifiedByUse = false
        }

        if (Item.curUser.challenge == Challenge.Gifted || Item.curUser.challenge == Challenge.CastingMaster)
            GLog.n(M.L(Challenge::class.java, "gone", name()))
        else
            GameScene.selectItem(itemSelector, mode, inventoryTitle)
    }

    override fun onItemSelected(item: Item) {
        upgrade(Item.curUser)
        Item.curUser.recoverSanity(Random.Float(0.5f, 3.5f))

        //logic for telling the user when item properties change from upgrades
        //...yes this is rather mess
        when (item) {
            is Weapon -> {
                val enchanted = item.enchantment != null
                item.upgrade()
                if (enchanted && item.enchantment == null)
                    GLog.w(M.L(Weapon::class.java, "incompatible"))
            }
            is Armor -> {
                val wasCursed = item.cursed
                val hadCursedGlyph = item.hasCurseGlyph()
                val hadGoodGlyph = item.hasGoodGlyph()

                item.upgrade()

                if (hadCursedGlyph && !item.hasCurseGlyph()) removeCurse(Dungeon.hero)
                else if (wasCursed && !item.cursed) weakenCurse(Dungeon.hero)
                if (hadGoodGlyph && !item.hasGoodGlyph())
                    GLog.w(Messages.get(Armor::class.java, "incompatible"))
            }
            is Wand -> {
                val wasCursed = item.cursed

                item.upgrade()

                if (wasCursed && !item.cursed) removeCurse(Dungeon.hero)
            }
            is Ring -> {
                val wasCursed = item.cursed

                item.upgrade()

                if (wasCursed && !item.cursed) {
                    if (item.level() < 1) {
                        weakenCurse(Dungeon.hero)
                    } else {
                        removeCurse(Dungeon.hero)
                    }
                }
            }
            else -> item.upgrade()
        }

        Badges.validateItemLevelAquired(item)
    }

    override fun price(): Int {
        return if (isKnown) 50 * quantity else super.price()
    }

    companion object {

        fun upgrade(hero: Hero) {
            hero.sprite.emitter().start(Speck.factory(Speck.UP), 0.2f, 3)
        }

        fun weakenCurse(hero: Hero) {
            GLog.p(Messages.get(ScrollOfUpgrade::class.java, "weaken_curse"))
            hero.sprite.emitter().start(ShadowParticle.UP, 0.05f, 5)
        }

        fun removeCurse(hero: Hero) {
            GLog.p(Messages.get(ScrollOfUpgrade::class.java, "remove_curse"))
            hero.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10)
        }
    }
}
