package com.egoal.darkestpixeldungeon.items.armor.glyphs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Shock
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.BlastParticle
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import kotlin.math.max

class ChuNeng : Armor.Glyph() {
    private var acc = 0

    override fun proc(armor: Armor, damage: Damage): Damage {
        val attacker = damage.from as Char
        val defender = damage.to as Char

        if (damage.type != Damage.Type.MENTAL && !damage.isFeatured(Damage.Feature.RANGED)) {
            acc++
            val req = max(10 + armor.tier - armor.level(), 3)
            if (acc >= req) {
                acc -= req
                Sample.INSTANCE.play(Assets.SND_BLAST)
                CellEmitter.center(defender.pos).burst(BlastParticle.FACTORY, 10 + armor.level())

                PathFinder.NEIGHBOURS8
                        .map { Dungeon.level.findMobAt(defender.pos + it) }
                        .filter {
                            it != null && it.camp != Char.Camp.NEUTRAL && it.camp != defender.camp
                        }.forEach {
                            it!!.takeDamage(it.defendDamage(Damage(1, defender, attacker)
                                    .type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE)))
                            Buff.prolong(it, Shock::class.java, 2f + armor.level() * 0.5f)
                        }
            }
        }

        return damage
    }

    override fun glowing(): ItemSprite.Glowing = PINK

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("acc", acc)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        acc = bundle.getInt("acc")
    }

    companion object {
        private val PINK = ItemSprite.Glowing(0xFF4488)
    }
}