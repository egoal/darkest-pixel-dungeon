package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

class Rousing: Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon)
        return damage.setAdditionalDamage(Damage.Element.HOLY, Random.Int(2, damage.value / 5))
    }

    override fun glowing(): ItemSprite.Glowing = COLOR

    companion object {
        private val COLOR = ItemSprite.Glowing(0xebe08d)
    }
}