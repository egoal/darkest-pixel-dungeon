package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

class Bashing : Enchantment() {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon)

        damage.value += Random.Int(1, damage.value)
        if (!damage.isFeatured(Damage.Feature.CRITICAL) && Random.Float() < (0.1f + 0.05f * weapon.level()))
            damage.addFeature(Damage.Feature.CRITICAL)

        return damage
    }

    override fun glowing(): ItemSprite.Glowing = RED

    companion object {
        private val RED = ItemSprite.Glowing(0xb3001e)
    }
}