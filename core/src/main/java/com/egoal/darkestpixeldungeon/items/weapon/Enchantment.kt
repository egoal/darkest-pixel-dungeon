package com.egoal.darkestpixeldungeon.items.weapon

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.SpellSprite
import com.egoal.darkestpixeldungeon.items.potions.*
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.*
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

abstract class Enchantment : Bundlable {
    var left = 0f

    open fun name(): String = M.L(this, "name")

    fun desc(): String = selfDesc() + M.L(Enchantment::class.java, "left_time", left.toInt())

    protected open fun selfDesc(): String = M.L(this, "desc")

    abstract fun proc(weapon: Weapon, damage: Damage): Damage

    protected fun use(weapon: Weapon, amount: Float = 1f) {
        left -= amount
        if (left <= 0f) {
            weapon.enchantment = null
            GLog.w(M.L(Enchantment::class.java, "no_effect", name()))

            QuickSlotButton.refresh()
        }
    }

    abstract fun glowing(): ItemSprite.Glowing

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(LEFT, left)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        left = bundle.getFloat(LEFT)
    }

    companion object {
        private const val LEFT = "left"

        private val potionEnchantments = mapOf<Class<out Potion>, Class<out Enchantment>>(
                PotionOfExperience::class.java to Sophisticated::class.java,
                PotionOfFrost::class.java to Chilling::class.java,
                PotionOfHealing::class.java to Healing::class.java,
                PotionOfInvisibility::class.java to Blinding::class.java,
                PotionOfLevitation::class.java to Shocking::class.java,
                PotionOfLiquidFlame::class.java to Blazing::class.java,
                PotionOfMagicalFog::class.java to Magical::class.java,
                PotionOfMight::class.java to Bashing::class.java,
                PotionOfMindVision::class.java to Tracking::class.java,
                PotionOfParalyticGas::class.java to StunningEcht::class.java,
                PotionOfPhysique::class.java to Unstable::class.java, //
                PotionOfPurity::class.java to Unstable::class.java, //
                PotionOfStrength::class.java to Bashing::class.java,
                PotionOfToxicGas::class.java to Venomous::class.java
        )

        fun RandomEnchantment(): Class<out Enchantment> = potionEnchantments.values.random()

        fun ForPotion(potion: Class<out Potion>): Class<out Enchantment> = potionEnchantments[potion]
                ?: Unstable::class.java

        fun DoEnchant(hero: Hero, weapon: Weapon, potion: Class<out Potion>, duration: Float) {
            weapon.enchant(ForPotion(potion), duration)

            SpellSprite.show(hero, SpellSprite.ENCHANT)
            hero.spend(1f)
            hero.busy()
            hero.sprite.operate(hero.pos)
        }
    }
}
