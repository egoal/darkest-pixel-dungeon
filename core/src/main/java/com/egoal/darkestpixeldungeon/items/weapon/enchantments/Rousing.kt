package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite

class Rousing: Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon)

        damage.addElement(Damage.Element.HOLY)
        return damage
    }

    override fun glowing(): ItemSprite.Glowing = COLOR

    companion object {
        private val COLOR = ItemSprite.Glowing(0xebe08d)
    }
}