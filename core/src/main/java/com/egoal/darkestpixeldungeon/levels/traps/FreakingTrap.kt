package com.egoal.darkestpixeldungeon.levels.traps

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.watabou.utils.Random

class FreakingTrap : Trap() {
    init {
        color = TrapSprite.RED
        shape = TrapSprite.GRILL
    }

    override fun activate() {
        val ch = Actor.findChar(pos)
        if (ch === Dungeon.hero && ch.isAlive) {
            ch.takeDamage(Damage(Random.NormalIntRange(3, 9), this, ch).type(Damage.Type.MENTAL))

            ch.sayShort(HeroLines.BAD_NOISE)
        }
    }
}