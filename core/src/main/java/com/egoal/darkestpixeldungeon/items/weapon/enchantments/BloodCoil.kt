package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random
import kotlin.math.round

class BloodCoil : Enchantment() {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon)

        damage.value += Random.Int(1, round(left * 2f).toInt())
        if (!damage.isFeatured(Damage.Feature.CRITICAL) && Random.Float() < 0.4f) {
            damage.addFeature(Damage.Feature.CRITICAL)
            damage.value = damage.value * 5 / 4
        }

        return damage
    }

    override fun glowing(): ItemSprite.Glowing = COLOR

    companion object {
        private val COLOR = ItemSprite.Glowing(0xcd0000)
    }
}