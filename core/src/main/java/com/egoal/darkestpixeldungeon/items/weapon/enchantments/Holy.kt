package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

/**
 * Created by 93942 on 6/2/2018.
 */

class Holy : Weapon.Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        // to undead or demonic
        if (damage.to is Mob) {
            val m = damage.to as Mob
            if (m.properties().contains(Char.Property.UNDEAD) || m.properties().contains(Char.Property.DEMONIC)) {
                // the extra damage is added by their resistance
                m.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10)
            }
        }

        // critical
        if (!damage.isFeatured(Damage.Feature.CRITICAL) && Random.Float() < .1f) {
            damage.value += damage.value / 4
            damage.addFeature(Damage.Feature.CRITICAL)
        }

        return damage.addElement(Damage.Element.HOLY)
    }

    override fun glowing(): ItemSprite.Glowing = LIGHT_YELLOW

    companion object {
        private val LIGHT_YELLOW = ItemSprite.Glowing(0xFFFFA0)
    }
}
