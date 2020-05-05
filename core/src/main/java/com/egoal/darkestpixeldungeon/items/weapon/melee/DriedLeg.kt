package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random
import kotlin.math.round

class DriedLeg : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.DRIED_LEG

        tier = 2
        DLY = 1.5f
        //also cannot surprise attack, see Hero.canSurpriseAttack
    }

    override fun proc(dmg: Damage): Damage {
        if (Random.Float() < 0.1f) Buff.prolong(dmg.to as Char, Paralysis::class.java, DLY)

        return super.proc(dmg)
    }

    override fun min(lvl: Int): Int = tier + 2 + round(lvl * 1.33).toInt()

    override fun max(lvl: Int): Int = round(super.max(lvl) * 1.33).toInt() // spear
}