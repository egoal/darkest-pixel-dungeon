package com.egoal.darkestpixeldungeon.actors.mobs.abilities

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random
import kotlin.math.min

class BleedingAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        if (Random.Int(2) == 0) Buff.affect(damage.to as Char, Bleeding::class.java).set(damage.value)
    }
}

class BindnessAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        if (Random.Int(3) == 0) Buff.prolong(damage.to as Char, Blindness::class.java, Random.Float(2f, 5f))
    }
}

class BurningAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        val enemy = damage.to as Char
        if (Random.Int(2) == 0) Buff.affect(enemy, Burning::class.java).reignite(enemy)
    }
}

class OozeAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        if (Random.Int(3) == 0) Buff.affect(damage.to as Char, Ooze::class.java)
    }
}

class PoisonAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        val enemy = damage.to as Char
        if (Random.Int(2) == 0) Buff.affect(enemy, Poison::class.java)
                .set(Random.Int(Dungeon.depth / 2, Dungeon.depth) * Poison.durationFactor(enemy))
    }
}

class CharmAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        val enemy = damage.to as Char
        if (Random.Int(3) == 0) {
            Charm.Attacher(belonger.id(), Random.IntRange(3, 7)).attachTo(enemy)

            enemy.sprite.centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5)
            Sample.INSTANCE.play(Assets.SND_CHARMS)
        }
    }
}

class CrippleAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        val enemy = damage.to as Char
        if (Random.Int(3) == 0 && enemy.buff(Cripple::class.java) == null) Buff.prolong(enemy, Cripple::class.java, 6f)
    }
}

class ParalysisAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        val enemy = damage.to as Char
        if (Random.Int(3) == 0 && enemy.buff(Paralysis::class.java) == null) Buff.prolong(enemy, Paralysis::class.java, 1f)
    }
}

class VampireAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        if (damage.type != Damage.Type.MENTAL) {
            val reg = min(damage.value, belonger.HT - belonger.HP) / 3
            if (reg > 0) {
                belonger.recoverHP(reg)
                belonger.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1)
            }
        }
    }
}

class KnockBackAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        val target = damage.to as Char
        val chance = when (Dungeon.level.distance(belonger.pos, target.pos)) {
            0, 1 -> 0.4f
            in 2..5 -> 0.25f
            else -> 0f
        }

        if (Random.Float() < chance) {
            val opposite = target.pos + (target.pos - belonger.pos)
            WandOfBlastWave.throwChar(target, Ballistica(target.pos, opposite, Ballistica.MAGIC_BOLT), 1)
        }
    }
}

class CritAttack : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        if (!damage.isFeatured(Damage.Feature.CRITICAL) && Random.Float() < .1f)
            damage.value = (damage.value * 1.5f).toInt()

        super.onAttack(belonger, damage)
    }
}