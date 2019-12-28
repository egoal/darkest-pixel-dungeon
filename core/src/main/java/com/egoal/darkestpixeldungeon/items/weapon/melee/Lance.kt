package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

class Lance : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.LANCE
        tier = 5

        DLY = 1.25f
        RCH = 2

        // cannot surprise attack, see Hero::canSurpriseAttack
    }

    // extra 5 base damage
    override fun max(lvl: Int): Int = 5 * (tier + 2) + lvl * (tier + 1)

    override fun giveDamage(hero: Hero, target: Char): Damage {
        return super.giveDamage(hero, target).apply {
            val r = 3f - 2f * 0.8f.pow(2f * hero.speed() - 2f)

            value = round(value * r).toInt()
        }
    }
}