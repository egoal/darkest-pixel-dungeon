package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.ViewMark
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite

class Tracking : Enchantment() {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon, 0.5f)

        Buff.prolong(damage.to as Char, ViewMark::class.java, 5f + weapon.level()).observer = (damage.from as Char).id()

        return damage
    }

    override fun glowing(): ItemSprite.Glowing = COLOR

    companion object {
        private val COLOR = ItemSprite.Glowing(0xbd3d8a)
    }
}