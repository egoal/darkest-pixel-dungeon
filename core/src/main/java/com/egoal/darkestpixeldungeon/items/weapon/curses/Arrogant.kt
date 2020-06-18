package com.egoal.darkestpixeldungeon.items.weapon.curses

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

/**
 * Created by 93942 on 10/13/2018.
 */

class Arrogant : Weapon.Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        if (Random.Int(10) == 0)
            (damage.from as Char).takeDamage(Damage(Random.Int(1, 3), damage.from, damage.from).type(Damage.Type.MENTAL))

        return damage
    }

    override fun curse(): Boolean = true

    override fun glowing(): ItemSprite.Glowing = BLACK

    companion object {
        private val BLACK = ItemSprite.Glowing(0x000000)
    }
}
