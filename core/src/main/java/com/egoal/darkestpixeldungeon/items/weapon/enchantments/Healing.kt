package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite

class Healing : Enchantment() {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon)

        val defender = damage.to as Char

        if (defender.properties().contains(Char.Property.UNDEAD) ||
                defender.properties().contains(Char.Property.DEMONIC)) {
            damage.value += damage.value / 4

            defender.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10)
        } else
            defender.recoverHP(damage.value / 4, damage.from)

        return damage
    }

    override fun glowing(): ItemSprite.Glowing = GREEN

    companion object {
        private val GREEN = ItemSprite.Glowing(0x00FF00)
    }
}