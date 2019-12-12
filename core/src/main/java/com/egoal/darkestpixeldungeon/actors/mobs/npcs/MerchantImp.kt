package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.effects.Speck

import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.unclassified.Torch
import com.egoal.darkestpixeldungeon.items.armor.PlateArmor
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfIdentify
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse
import com.egoal.darkestpixeldungeon.items.weapon.melee.Claymore
import com.egoal.darkestpixeldungeon.items.weapon.melee.WarHammer
import com.egoal.darkestpixeldungeon.sprites.ImpSprite
import com.watabou.utils.Random

/**
 * Created by 93942 on 8/21/2018.
 */

class MerchantImp : Merchant() {
    init {
        spriteClass = ImpSprite::class.java
    }

    override fun initSellItems() {
        // devil would be place by painter, here, add extra items
        repeat(2) {
            addItemToSell(Generator.POTION.generate())
        }

        addItemToSell(ScrollOfRemoveCurse())
        addItemToSell(ScrollOfMagicMapping())
        addItemToSell(Generator.SCROLL.generate())

        repeat(2) {
            addItemToSell(if (Random.Int(2) == 0) Generator.POTION.generate()
            else Generator.SCROLL.generate())
        }

        addItemToSell(Generator.WEAPON.MELEE.T5.generate().identify())

        repeat(2) {
            addItemToSell(Generator.WEAPON.MISSSILE.generate())
        }

        addItemToSell(PlateArmor().identify())

        repeat(3) { addItemToSell(Torch()) }
    }

    override fun flee() {
        destroy()

        sprite.emitter().burst(Speck.factory(Speck.WOOL), 15)
        sprite.killAndErase()
    }
}
