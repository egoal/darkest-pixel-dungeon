package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random
import kotlin.math.max

class TengusKatana : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.KATANA;

        tier = 3
    }

    override fun max(lvl: Int): Int = 4 * (tier + 1) + lvl * (tier + 1)

    override fun price(): Int = 40 * tier + 30 * level()

    override fun giveDamage(hero: Hero, target: Char): Damage {
        val dmg = super.giveDamage(hero, target)

        return dmg
    }

    override fun proc(dmg: Damage): Damage {
        val attacker = dmg.from as Char
        val defender = dmg.to as Char
        if (attacker is Hero && defender is Mob && defender.Config.MaxLevel <= attacker.lvl + 5 &&
                !defender.properties().contains(Char.Property.BOSS) && defender.surprisedBy(attacker)) {
            // 10% -> 50%
            val p = (1f + level()) / (10f + 2 * level())
            if (Random.Float() < p) {
                dmg.value = max(defender.HT, dmg.value)
                dmg.addFeature(Damage.Feature.PURE)

                Wound.hit(defender, 30f)
                Wound.hit(defender, -30f)
            }
        }

        return super.proc(dmg)
    }
}