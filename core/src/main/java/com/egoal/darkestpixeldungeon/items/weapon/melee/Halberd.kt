package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.PathFinder
import kotlin.math.round

class Halberd : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.HALBERD

        tier = 4
        DLY = 1.5f
        RCH = 2
        ACC = 0.75f
    }

    // x1.5
    override fun max(lvl: Int): Int = super.max(lvl) * 3 / 2

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) + 1

    override fun proc(dmg: Damage): Damage {
        val pos = (dmg.to as Char).pos
        for (i in PathFinder.NEIGHBOURS8) {
            val value = round(dmg.value * 0.4f).toInt()
            val mob = Dungeon.level.findMobAt(pos + i)
            if (mob != null && mob.camp == Char.Camp.ENEMY)
                mob.takeDamage(mob.defendDamage(Damage(value, dmg.from, dmg.to).type(dmg.type)))
        }

        return super.proc(dmg)
    }
}