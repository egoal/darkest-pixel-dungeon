package com.egoal.darkestpixeldungeon.actors.mobs.abilities

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class EnchantDefend(private val prob: Float, private val enchantClass: Class<out Enchantment>, private val duration: Float) : Ability() {
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

class ReleaseGasDefend : Ability() {
    lateinit var gas: Class<out Blob>

    override fun onReady(belonger: Mob) {
        belonger.immunities.add(gas)
    }

    override fun onDefend(belonger: Mob, damage: Damage) {
        GameScene.add(Blob.seed(belonger.pos, 20, gas))
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put("gas", gas)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        gas = bundle.getClass("gas") as Class<out Blob>
    }
}

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

        if (belonger.isAlive && belonger.HP < belonger.HT * 2 / 5) {
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
