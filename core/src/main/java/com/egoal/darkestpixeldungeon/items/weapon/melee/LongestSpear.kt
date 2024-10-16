package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random
import kotlin.math.pow
import kotlin.math.round

class LongestSpear : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.LONGEST_SPEAR

        tier = 4
        DLY = 1.5f
        RCH = 3
    }

    override fun max(lvl: Int): Int = round(super.max(lvl) * 1.2).toInt()

    override fun proc(dmg: Damage): Damage {
        if (dmg.to is Char) {
            val attacker = dmg.from as Char
            val defender = dmg.to as Char

            val dis = Dungeon.level.distance(defender.pos, attacker.pos)
            if (dis > 1) {
                val chance = 0.1f + (dis - 1) * .2f + 0.5f * (1f - .7f.pow(level() / 2f))
                if (Random.Float() < chance) {
                    if (defender.SHLD > 0) defender.SHLD = 0
                    dmg.addFeature(Damage.Feature.PURE)

                    defender.sprite.showStatus(CharSprite.WARNING, M.L(this, "crit"))
                }
            }
        }

        return super.proc(dmg)
    }

    override fun evasionFactor(hero: Hero, target: Char): Float =
            if (Dungeon.level.adjacent(hero.pos, target.pos)) 0.5f else 1f

}