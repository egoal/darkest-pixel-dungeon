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
import com.egoal.darkestpixeldungeon.effects.particles.PurpleParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.effects.Enchanting
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.noosa.audio.Sample

import java.util.ArrayList
import javax.microedition.khronos.opengles.GL

class Stylus : Item() {

    private val itemSelector = WndBag.Listener { item ->
        if (item != null) {
            this@Stylus.inscribe(item)
        }
    }

    init {
        image = ItemSpriteSheet.STYLUS

        stackable = true

        bones = true
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_INSCRIBE) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_INSCRIBE) {
            curUser = hero
            GameScene.selectItem(itemSelector, WndBag.Mode.ENCHANTABLE, Messages.get(this, "prompt"))
        }
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    private fun inscribe(item: Item) {
        if (!item.isIdentified) {
            GLog.w(M.L(this, "identify"))
        } else if (item.cursed ||
                (item is Armor && item.hasCurseGlyph()) ||
                (item is Weapon && item.hasCurseInscription())) {
            GLog.w(M.L(this, "cursed"))
        } else {
            detach(curUser.belongings.backpack)

            when (item) {
                is Armor -> inscribeArmor(item)
                is Weapon -> inscribeWeapon(item)
                is Wand -> inscribeWand(item)
            }

            curUser.sprite.operate(curUser.pos)
            curUser.sprite.centerEmitter().start(PurpleParticle.BURST, 0.05f, 10)
            Enchanting.show(curUser, item)

            curUser.spend(TIME_TO_INSCRIBE)
            curUser.busy()

            Sample.INSTANCE.play(Assets.SND_BURNING)
            GLog.w(M.L(this, "inscribed", item.name()))
        }
    }

    private fun inscribeArmor(armor: Armor) {
        armor.inscribe()
    }

    private fun inscribeWeapon(weapon: Weapon) {
        weapon.inscribe()
    }

    private fun inscribeWand(wand: Wand) {
        if (wand.isInscribed) GLog.w(M.L(Wand::class.java, "inscribed"))
        else wand.inscribe()
    }

    override fun price(): Int = 30 * quantity

    companion object {
        private const val TIME_TO_INSCRIBE = 2f

        private const val AC_INSCRIBE = "INSCRIBE"
    }
}
