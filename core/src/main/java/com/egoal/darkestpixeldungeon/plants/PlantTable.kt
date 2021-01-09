package com.egoal.darkestpixeldungeon.plants

import com.egoal.darkestpixeldungeon.items.potions.*
import com.egoal.darkestpixeldungeon.items.wands.WandOfRegrowth

object PlantTable {
    data class Row(val plant: Class<out Plant>, val seed: Class<out Plant.Seed>, val potion: Class<out Potion>)

    private val rows: List<Row>

    init {
        rows = listOf(
                Row(BlandfruitBush::class.java, BlandfruitBush.Seed::class.java, PotionOfPhysique::class.java),
                Row(Blindweed::class.java, Blindweed.Seed::class.java, PotionOfInvisibility::class.java),
                Row(CorrodeCyan::class.java, CorrodeCyan.Seed::class.java, PotionOfMagicalFog::class.java),
                Row(Dreamfoil::class.java, Dreamfoil.Seed::class.java, PotionOfPurity::class.java),
                Row(Earthroot::class.java, Earthroot.Seed::class.java, PotionOfParalyticGas::class.java),
                Row(Fadeleaf::class.java, Fadeleaf.Seed::class.java, PotionOfMindVision::class.java),
                Row(Firebloom::class.java, Firebloom.Seed::class.java, PotionOfLiquidFlame::class.java),
                Row(Icecap::class.java, Icecap.Seed::class.java, PotionOfFrost::class.java),
                Row(Rotberry::class.java, Rotberry.Seed::class.java, PotionOfStrength::class.java),
                Row(Sorrowmoss::class.java, Sorrowmoss.Seed::class.java, PotionOfToxicGas::class.java),
                Row(Starflower::class.java, Starflower.Seed::class.java, PotionOfExperience::class.java),
                Row(Stormvine::class.java, Stormvine.Seed::class.java, PotionOfLevitation::class.java),
                Row(Sungrass::class.java, Sungrass.Seed::class.java, PotionOfHealing::class.java),

                //
                Row(WandOfRegrowth.Dewcatcher::class.java, WandOfRegrowth.Dewcatcher.Seed::class.java, PotionOfHealing::class.java),
                Row(WandOfRegrowth.Seedpod::class.java, WandOfRegrowth.Seedpod.Seed::class.java, PotionOfHealing::class.java)
        )
    }

    fun row(filter: (Row) -> Boolean): Row = rows.first(filter)
}