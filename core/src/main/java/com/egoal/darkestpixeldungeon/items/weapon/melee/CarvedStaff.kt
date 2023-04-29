package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class CarvedStaff : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.CARVED_STAFF
        tier = 3
    }

    override fun max(lvl: Int): Int = 5 * tier + lvl * tier

    override fun speedFactor(hero: Hero): Float {
        val ratio = if (enchantment == null) 1f else 0.8f
        return super.speedFactor(hero) * ratio
    }

    override fun proc(dmg: Damage): Damage {
        var dmg = dmg
        if (enchantment != null) dmg = enchantment!!.proc(this, dmg)
        return super.proc(dmg)
    }
}