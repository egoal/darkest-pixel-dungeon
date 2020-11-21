package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Inscription
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.inscriptions.Dazzling
import com.egoal.darkestpixeldungeon.items.weapon.inscriptions.Stunning
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSprite

abstract class InscribeSimliar(insc: Class<out Inscription>) : Enchantment() {
    private val inscription: Inscription = insc.newInstance()

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon, 0.5f) // last longer
        return inscription.proc(weapon, damage)
    }

    override fun name(): String = inscription.name(M.L(Enchantment::class.java, "name"))

    override fun selfDesc(): String = inscription.desc()
}

class StunningEcht : InscribeSimliar(Stunning::class.java) {
    override fun glowing(): ItemSprite.Glowing = YELLOW

    companion object {
        private val YELLOW = ItemSprite.Glowing(0xCCAA44)
    }
}

class DazzlingEcht : InscribeSimliar(Dazzling::class.java) {
    override fun glowing(): ItemSprite.Glowing = YELLOW

    companion object {
        private val YELLOW = ItemSprite.Glowing(0xFFFF00)
    }
}
