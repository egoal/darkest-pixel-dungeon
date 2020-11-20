package com.egoal.darkestpixeldungeon.items.weapon.inscriptions

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Inscription
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Bundle
import kotlin.math.max

class Storming : Inscription(6) {

    private var id = -1
    private var acc = 0

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        val defender = damage.to as Char

        if (defender.id() == id) {
            // this enchant may usually apply on fast weapon, so this fix should be powerful enough.
            acc += max(1, weapon.level())
            damage.value += acc
        } else {
            id = defender.id()
            acc = 0
        }

        return damage
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ID, id)
        bundle.put(ACC, acc)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        id = bundle.getInt(ID)
        acc = bundle.getInt(ACC)
    }

    companion object {
        private const val ID = "id"
        private const val ACC = "acc"
    }
}