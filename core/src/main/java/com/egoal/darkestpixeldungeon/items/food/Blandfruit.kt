package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.*
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import java.lang.RuntimeException

class Blandfruit : Food(Hunger.STARVING / 2, 6) {
    //only applies when blandfruit is cooked
    init {
        stackable = true
        image = ItemSpriteSheet.BLANDFRUIT

        bones = true
    }

    var potionAttrib: Potion? = null
    var potionGlow: ItemSprite.Glowing? = null

    override fun glowing(): ItemSprite.Glowing? = potionGlow

    override fun isSimilar(item: Item): Boolean {
        if (item is Blandfruit) {
            if (potionAttrib == null) return item.potionAttrib == null
            else if (item.potionAttrib != null) return item.potionAttrib!!::class == potionAttrib!!::class
        }
        return false
    }

    override fun execute(hero: Hero, action: String) {
        if (action == AC_EAT && potionAttrib == null) {
            GLog.w(Messages.get(this, "raw"))
            return
        }

        super.execute(hero, action)

        if (action == AC_EAT && potionAttrib != null) {
            when (potionAttrib) {
                is PotionOfFrost -> {
                    GLog.i(Messages.get(this, "ice_msg"))
                    FrozenCarpaccio.effect(hero)
                }
                is PotionOfLiquidFlame -> {
                    GLog.i(Messages.get(this, "fire_msg"))
                    Buff.affect(hero, FireImbue::class.java).set(FireImbue.DURATION)
                }
                is PotionOfToxicGas -> {
                    GLog.i(Messages.get(this, "toxic_msg"))
                    Buff.affect(hero, ToxicImbue::class.java).set(ToxicImbue.DURATION)
                }
                is PotionOfParalyticGas -> {
                    GLog.i(Messages.get(this, "para_msg"))
                    Buff.affect(hero, EarthImbue::class.java, EarthImbue.DURATION)
                }
                else -> potionAttrib!!.apply(hero)
            }
        }
    }

    override fun desc(): String = if (potionAttrib == null) super.desc()
    else Messages.get(this, "desc_cooked")

    override fun price(): Int = 20 * quantity

    fun cook(seed: Plant.Seed): Item = imbuePotion(seed.alchemyClass!!.newInstance() as Potion)

    private fun imbuePotion(potion: Potion): Item {
        potionAttrib = potion
        potionAttrib!!.ownedByFruit = true
        potionAttrib!!.image = ItemSpriteSheet.BLANDFRUIT

        // name, color
        val pr = when (potionAttrib) {
            is PotionOfHealing -> Pair("sunfruit", 0x2EE62E)
            is PotionOfStrength -> Pair("rotfruit", 0xCC0022)
            is PotionOfParalyticGas -> Pair("earthfruit", 0x67583D)
            is PotionOfInvisibility -> Pair("blindfruit", 0xE5D273)
            is PotionOfLiquidFlame -> Pair("firefruit", 0xFF7F00)
            is PotionOfFrost -> Pair("icefruit", 0x66B3FF)
            is PotionOfMindVision -> Pair("fadefruit", 0xB8E6CF)
            is PotionOfToxicGas -> Pair("sorrowfruit", 0xA15CE5)
            is PotionOfLevitation -> Pair("stormfruit", 0x1C3A57)
            is PotionOfPurity -> Pair("dreamfruit", 0x8E2975)
            is PotionOfExperience -> Pair("starfruit", 0xA79400)
            else -> throw RuntimeException("never be here")
        }
        name = Messages.get(this, pr.first)
        potionGlow = ItemSprite.Glowing(pr.second)

        if (potionAttrib!!.canBeReinforced()) potionAttrib!!.reinforce()

        return this
    }

    override fun cast(user: Hero, dst: Int) {
        if (potionAttrib is PotionOfLiquidFlame ||
                potionAttrib is PotionOfToxicGas ||
                potionAttrib is PotionOfParalyticGas ||
                potionAttrib is PotionOfFrost ||
                potionAttrib is PotionOfLevitation ||
                potionAttrib is PotionOfPurity) {
            potionAttrib!!.cast(user, dst)
            detach(user.belongings.backpack)
        } else {
            super.cast(user, dst)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(POTIONATTRIB, potionAttrib)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        if (bundle.contains(POTIONATTRIB)) {
            imbuePotion(bundle.get(POTIONATTRIB) as Potion)
        }
    }

    companion object {
        private const val POTIONATTRIB = "potionattrib"
    }

}