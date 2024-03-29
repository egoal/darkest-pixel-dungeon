package com.egoal.darkestpixeldungeon.actors.mobs.abilities

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.StenchGas
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.blobs.WhiteFog
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Blazing
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Unstable
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Venomous
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.round

open class EnchantDefend(private val prob: Float, private val enchantClass: Class<out Enchantment>, private val duration: Float) : Ability() {
    override fun onDefend(belonger: Mob, damage: Damage) {
        if (damage.from is Hero) {
            val hero = damage.from as Hero
            if (Dungeon.level.adjacent(hero.pos, belonger.pos) && Random.Float() < prob) {
                val weapon = hero.belongings.weapon
                if (weapon is MeleeWeapon && weapon.enchantment == null) weapon.enchant(enchantClass, duration)
            }
        }
    }
}

class EnchantDefend_Fire : EnchantDefend(.3333f, Blazing::class.java, 8f)

class EnchantDefend_Venomous : EnchantDefend(.25f, Venomous::class.java, 8f)

class EnchantDefend_Unstable : EnchantDefend(.3333f, Unstable::class.java, 8f)

open class ReleaseGasDefend(private val gas: Class<out Blob>) : Ability() {
    override fun onReady(belonger: Mob) {
        belonger.immunities.add(gas)
    }

    override fun onDefend(belonger: Mob, damage: Damage) {
        GameScene.add(Blob.seed(belonger.pos, 20, gas))
    }
}

class ReleaseGasDefend_Toxic : ReleaseGasDefend(ToxicGas::class.java)

class ReleaseGasDefend_StenchGas : ReleaseGasDefend(StenchGas::class.java)

class ReleaseGasDefend_WhiteFog : ReleaseGasDefend(WhiteFog::class.java)

class EnrageDefend : Ability() {
    private var actived = false

    override fun procGivenDamage(belonger: Mob, damage: Damage) {
        if (actived && !damage.isFeatured(Damage.Feature.CRITICAL)) {
            damage.value = (damage.value * Random.Float(1.25f, 1.75f)).toInt()
            damage.addFeature(Damage.Feature.CRITICAL)
        }
    }

    override fun onDefend(belonger: Mob, damage: Damage) {
        if (actived) return

        if (belonger.isAlive && (belonger.HP - damage.value) < belonger.HT * 2 / 5) {
            actived = true
            if (Dungeon.visible[belonger.pos]) {
                GLog.w(M.L(this, "enraged-info", belonger.name))
                belonger.sprite.showStatus(CharSprite.NEGATIVE, M.L(this, "enraged"))
            }

            //* take 1 to enrage.
            belonger.spend(1f)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put("actived", actived)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        actived = bundle.getBoolean("actived")
    }
}

class FeedbackDefend : Ability() {
    override fun onDefend(belonger: Mob, damage: Damage) {
        val feedback = Random.NormalIntRange(0, damage.value)
        if (feedback > 0) (damage.from as Char).takeDamage(Damage(feedback, belonger, damage.from))
    }
}

class AntiMagicDefend : Ability() {
    override fun onDefend(belonger: Mob, damage: Damage) {
        if (damage.type == Damage.Type.MAGICAL) damage.value /= 2
    }
}

class CounterDefend : Ability() {
    override fun onDefend(belonger: Mob, damage: Damage) {
        val enemy = damage.to as Char?
        if (enemy == null) return
        if (damage.type == Damage.Type.NORMAL &&
                !damage.isFeatured(Damage.Feature.ACCURATE or Damage.Feature.RANGED) &&
                Dungeon.level.adjacent(belonger.pos, enemy.pos) &&
                belonger.buff(Paralysis::class.java) == null && Random.Float() < .175f) {
            damage.value = 0
            enemy.takeDamage(enemy.defendDamage(belonger.giveDamage(enemy)))

            belonger.sprite.showStatus(CharSprite.WARNING, M.L(this, "counter"))
        }
    }
}

//open class TileDefend(private val tile: Int, private val ratio: Float) : Ability() {
//    override fun onDefend(belonger: Mob, damage: Damage) {
//        if (!belonger.flying && Dungeon.level.map[belonger.pos] == tile)
//            damage.value = round(damage.value * (1f - ratio)).toInt()
//    }
//}

class TileDefend_Water : Ability() {
    override fun onDefend(belonger: Mob, damage: Damage) {
        if (!belonger.flying && Level.water[belonger.pos]) damage.value = round(damage.value * .5f).toInt()
    }
}

class TileDefend_Grass : Ability() {
    override fun onDefend(belonger: Mob, damage: Damage) {
        if (!belonger.flying &&
                (Dungeon.level.map[belonger.pos] == Terrain.HIGH_GRASS_COLLECTED || Dungeon.level.map[belonger.pos] == Terrain.HIGH_GRASS))
            damage.value = round(damage.value * .5f).toInt()
    }
}
