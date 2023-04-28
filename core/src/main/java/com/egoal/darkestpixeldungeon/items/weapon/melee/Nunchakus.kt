package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class Nunchakus : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.NUNCHAKUS
        tier = 2
        DLY = 0.75f
    }

    override fun min(lvl: Int): Int = tier + lvl
    override fun max(lvl: Int): Int = 3 * (tier + 1) + lvl * tier

    override fun giveDamage(hero: Hero, target: Char): Damage {
        val dmg = super.giveDamage(hero, target)
        if (Random.Float() < hero.evasionProbability()) {
            dmg.value *= 2
            dmg.addFeature(Damage.Feature.CRITICAL)
        }
        return dmg
    }
}