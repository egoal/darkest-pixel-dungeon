package com.egoal.darkestpixeldungeon.items.weapon.curses

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

class Bloodthirsty : Weapon.Enchantment() {
    private var thirsty = 0

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        val extra = Random.Int(1, damage.value / 2)
        damage.value += extra // directly add.
        thirsty -= extra
        if (thirsty > 0) {
            val attacker = damage.from as Char
            val dmg = Damage(thirsty, this, attacker) // .addFeature(Damage.Feature.PURE)
            attacker.takeDamage(dmg)
            attacker.sprite.bloodBurstB((damage.to as Char).sprite.center(), dmg.value)
        } else thirsty = extra

        return damage
    }

    override fun curse(): Boolean = true

    override fun glowing(): ItemSprite.Glowing = DARK_RED

    companion object {
        private val DARK_RED = ItemSprite.Glowing(0x902626)
    }
}