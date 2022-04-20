package com.egoal.darkestpixeldungeon.actors.mobs.abilities

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.StenchGas
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.utils.Random

class EnchantDefendAbility(private val prob: Float, private val enchantClass: Class<out Enchantment>, private val duration: Float) : Ability() {
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

class ReleaseGasDefendAbility(private val gas: Class<out Blob>) : Ability() {
    override fun onReady(belonger: Mob) {
        belonger.immunities.add(gas)
    }

    override fun onDefend(belonger: Mob, damage: Damage) {
        GameScene.add(Blob.seed(belonger.pos, 20, gas))
    }
}
