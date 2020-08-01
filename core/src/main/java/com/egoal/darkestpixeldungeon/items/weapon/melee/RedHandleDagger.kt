package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class RedHandleDagger : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.RED_HANDLE_DAGGER

        tier = 1
    }

    override fun proc(dmg: Damage): Damage {
        if (dmg.to is Mob) {
            val defender = dmg.to as Mob
            if (defender.surprisedBy(Dungeon.hero) && Random.Float() < 0.3f) {
                Buff.prolong(defender, Cripple::class.java, 2f + level())
            }
        }

        return super.proc(dmg)
    }
}