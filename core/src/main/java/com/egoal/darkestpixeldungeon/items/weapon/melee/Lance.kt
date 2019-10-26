package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import kotlin.math.min
import kotlin.math.round

class Lance : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.LANCE
        tier = 5

        DLY = 1.25f
        RCH = 2

        // cannot surprise attack, see Hero::canSurpriseAttack
    }

    // extra 10 base damage
    override fun max(lvl: Int): Int = 5 * (tier + 3) + lvl * (tier + 1)

    override fun giveDamage(hero: Hero, target: Char): Damage {
        return super.giveDamage(hero, target).apply {
            val r = 3f - 2f * Math.pow(0.8, 2.0 * hero.speed() - 2.0)

            value = round(value * r).toInt()
        }
    }
}