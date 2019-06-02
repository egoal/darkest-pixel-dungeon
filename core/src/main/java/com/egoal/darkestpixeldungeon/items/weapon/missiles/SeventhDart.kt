package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class SeventhDart(number: Int = 1) : MissileWeapon(3) {
    init {
        image = ItemSpriteSheet.DART_SEVENTH

        quantity = number
    }

    override fun max(lvl: Int): Int = 2 + 2 * tier

    override fun breakChance(): Float = super.breakChance() * 0.8f

    override fun proc(dmg: Damage): Damage {
        Buff.affect(dmg.to as Char, Bleeding::class.java).set((dmg.to as Char).HT / 10)
        return super.proc(dmg)
    }
}