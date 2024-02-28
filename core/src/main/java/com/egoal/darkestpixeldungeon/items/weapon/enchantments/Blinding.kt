package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

class Blinding : Enchantment() {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon)

        val defender = damage.to as Char

        if (Random.Float() < 0.35) {
            Buff.prolong(defender, Blindness::class.java, Random.Float(2f, 3f))
            if (defender is Mob) {
                if (defender.state == defender.HUNTING) defender.state = defender.WANDERING
                defender.beckon(Dungeon.level.randomDestination())
            }
        }

        return damage.setAdditionalDamage(Damage.Element.SHADOW, Random.Int(2, damage.value / 5))
    }

    override fun glowing(): ItemSprite.Glowing = COLOR

    companion object {
        private val COLOR = ItemSprite.Glowing(0x3a2561)
    }
}