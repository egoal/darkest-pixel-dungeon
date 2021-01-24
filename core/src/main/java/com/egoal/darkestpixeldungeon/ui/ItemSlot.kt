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
package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.BitmapText
import com.watabou.noosa.Image
import com.watabou.noosa.ui.Button

open class ItemSlot() : Button() {

    protected lateinit var icon: ItemSprite
    protected var item: Item? = null
    private lateinit var topLeft: BitmapText
    private lateinit var topRight: BitmapText
    private lateinit var bottomRight: BitmapText
    private var bottomRightIcon: Image? = null
    private var iconVisible = true

    init {
        icon.visible(false)
        enable(false)
    }

    constructor(item: Item?) : this() {
        item(item)
    }

    override fun createChildren() {

        super.createChildren()

        icon = ItemSprite()
        add(icon)

        topLeft = BitmapText(PixelScene.pixelFont)
        add(topLeft)

        topRight = BitmapText(PixelScene.pixelFont)
        add(topRight)

        bottomRight = BitmapText(PixelScene.pixelFont)
        add(bottomRight)
    }

    override fun layout() {
        super.layout()

        icon.x = x + (width - icon.width) / 2
        icon.y = y + (height - icon.height) / 2

        topLeft.x = x
        topLeft.y = y

        topRight.x = x + (width - topRight.width())
        topRight.y = y

        bottomRight.x = x + (width - bottomRight.width())
        bottomRight.y = y + (height - bottomRight.height())

        if (bottomRightIcon != null) {
            bottomRightIcon!!.x = bottomRight.x - bottomRightIcon!!.width() - 2
            bottomRightIcon!!.y = y + (height - bottomRightIcon!!.height())
        }
    }

    open fun item(item: Item?) {
        if (this.item === item) {
            if (item != null) icon.frame(item.image())
            updateText()
            return
        }

        this.item = item

        if (item == null) {

            enable(false)
            icon.visible(false)

            updateText()

        } else {
            enable(true)
            icon.visible(true)

            icon.view(item)
            updateText()
        }
    }

    private fun updateText() {
        if (bottomRightIcon != null) {
            remove(bottomRightIcon)
            bottomRightIcon = null
        }

        if (item == null) {
            bottomRight.visible = false
            topRight.visible = bottomRight.visible
            topLeft.visible = topRight.visible
            return
        } else {
            bottomRight.visible = true
            topRight.visible = bottomRight.visible
            topLeft.visible = topRight.visible
        }

        topLeft.text(item!!.status())

        if (item is Weapon) updateWeaponText(item as Weapon)
        else if (item is Armor) updateArmorText(item as Armor)
        else {
            topRight.text(null)

            val level = item!!.visiblyUpgraded()

            if (level != 0) {
                bottomRight.text(Messages.format(TXT_LEVEL, level))
                bottomRight.hardlight(if (level > 0) UPGRADED else DEGRADED)
                bottomRight.measure()
            } else if (item is Scroll || item is Potion) {
                // scroll and potion gain a icon
                bottomRight.text(null)

                val iconInt: Int? = if (item is Scroll) {
                    (item as Scroll).initials()
                } else {
                    (item as Potion).initials()
                }

                if (iconInt != null && iconVisible) {
                    bottomRightIcon = consumeIcon(if (item is Potion) 0 else 1, iconInt)
                    add(bottomRightIcon!!)
                }

            } else {
                bottomRight.text(null)
            }
        }

        layout()
    }

    private fun updateWeaponText(weapon: Weapon) {
        // str
        if (weapon.levelKnown || weapon !is MeleeWeapon) {
            val str = weapon.STRReq()
            topRight.text(":$str")
            if (str > Dungeon.hero.STR()) topRight.hardlight(DEGRADED)
            else topRight.resetColor()
        } else {
            val str = weapon.STRReq(0)
            topRight.text("$str?")
            topRight.hardlight(WARNING)
        }

        topRight.measure()

        // inscription
        if (weapon.inscription != null && (weapon.cursedKnown || !weapon.inscription!!.curse)) {
            val icon = weapon.inscription!!.icon
            if (icon >= 0) {
                bottomRightIcon = consumeIcon(3 + icon / 18, icon % 18)
                add(bottomRightIcon!!)
            }
        }

        // level
        val level = weapon.visiblyUpgraded()
        if (level != 0) {
            bottomRight.text(Messages.format(TXT_LEVEL, level))
            bottomRight.hardlight(if (level > 0) UPGRADED else DEGRADED)
            bottomRight.measure()
        } else bottomRight.text(null)
    }

    private fun updateArmorText(armor: Armor) {
        // str
        if (armor.levelKnown) {
            val str = armor.STRReq()
            topRight.text(":$str")
            if (str > Dungeon.hero.STR()) topRight.hardlight(DEGRADED)
            else topRight.resetColor()
        } else {
            val str = armor.STRReq(0)
            topRight.text("$str?")
            topRight.hardlight(WARNING)
        }

        topRight.measure()

        // level
        val level = armor.visiblyUpgraded()
        if (level != 0) {
            bottomRight.text(Messages.format(TXT_LEVEL, level))
            bottomRight.hardlight(if (level > 0) UPGRADED else DEGRADED)
            bottomRight.measure()
        } else bottomRight.text(null)
    }

    private fun consumeIcon(row: Int, col: Int): Image = Image(Assets.DPD_CONS_ICONS).apply { frame(col * 7, row * 8, 7, 8) }

    fun enable(value: Boolean) {

        active = value

        val alpha = if (value) ENABLED else DISABLED
        icon.alpha(alpha)
        topLeft.alpha(alpha)
        topRight.alpha(alpha)
        bottomRight.alpha(alpha)
        if (bottomRightIcon != null) bottomRightIcon!!.alpha(alpha)
    }

    fun showParams(TL: Boolean, TR: Boolean, BR: Boolean) {
        if (TL)
            add(topLeft)
        else
            remove(topLeft)

        if (TR)
            add(topRight)
        else
            remove(topRight)

        if (BR)
            add(bottomRight)
        else
            remove(bottomRight)
        iconVisible = BR
    }

    companion object {

        const val DEGRADED = 0xFF4444
        const val UPGRADED = 0x44FF44
        const val FADED = 0x999999
        const val WARNING = 0xFF8800

        private const val ENABLED = 1.0f
        private const val DISABLED = 0.3f

        private const val TXT_LEVEL = "%+d"
        private const val TXT_CURSED = ""//"-";

        // Special "virtual items"
        val CHEST: Item = object : Item() {
            override fun image(): Int = ItemSpriteSheet.CHEST
        }
        val LOCKED_CHEST: Item = object : Item() {
            override fun image(): Int = ItemSpriteSheet.LOCKED_CHEST
        }
        val CRYSTAL_CHEST: Item = object : Item() {
            override fun image(): Int = ItemSpriteSheet.CRYSTAL_CHEST
        }
        val TOMB: Item = object : Item() {
            override fun image(): Int = ItemSpriteSheet.TOMB
        }
        val SKELETON: Item = object : Item() {
            override fun image(): Int = ItemSpriteSheet.BONES
        }
        val REMAINS: Item = object : Item() {
            override fun image(): Int = ItemSpriteSheet.REMAINS
        }
    }
}
