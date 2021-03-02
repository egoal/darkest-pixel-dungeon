package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

class Sophisticated : Enchantment() {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon, 0.5f)

        // 20%
        if (!damage.isFeatured(Damage.Feature.CRITICAL)) {
            if (Random.Float() < 0.20f) {
                damage.value *= 2
                damage.addFeature(Damage.Feature.CRITICAL)
            } else damage.value += damage.value / 8
        }

        return damage
    }

    override fun glowing(): ItemSprite.Glowing = COLOR

    companion object {
        private val COLOR = ItemSprite.Glowing(0xa79400)
    }
}