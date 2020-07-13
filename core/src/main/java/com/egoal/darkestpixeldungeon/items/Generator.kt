package com.egoal.darkestpixeldungeon.items

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Ghost
import com.egoal.darkestpixeldungeon.items.armor.*
import com.egoal.darkestpixeldungeon.items.artifacts.*
import com.egoal.darkestpixeldungeon.items.bags.Bag
import com.egoal.darkestpixeldungeon.items.books.Book
import com.egoal.darkestpixeldungeon.items.books.textbook.CallysDiary
import com.egoal.darkestpixeldungeon.items.books.textbook.WardenSmithNotes
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat
import com.egoal.darkestpixeldungeon.items.food.Pasty
import com.egoal.darkestpixeldungeon.items.helmets.*
import com.egoal.darkestpixeldungeon.items.potions.*
import com.egoal.darkestpixeldungeon.items.rings.*
import com.egoal.darkestpixeldungeon.items.scrolls.*
import com.egoal.darkestpixeldungeon.items.wands.*
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.*
import com.egoal.darkestpixeldungeon.items.weapon.missiles.*
import com.egoal.darkestpixeldungeon.plants.*
import com.egoal.darkestpixeldungeon.items.unclassified.*
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.GameMath
import com.watabou.utils.Random

object Generator {
    fun CurrentFloorSet(): Int = Dungeon.depth / 5

    abstract class ItemGenerator {
        abstract fun generate(): Item
    }

    open class ClassMapGenerator<T>(val probMap: HashMap<Class<out T>, Float>) : ItemGenerator() {
        override fun generate(): Item = (Random.chances(probMap).newInstance() as Item).random()
    }

    object ARMOR : ItemGenerator() {
        private val classes = listOf(
                ClothArmor::class.java, LeatherArmor::class.java, MailArmor::class.java,
                ScaleArmor::class.java, PlateArmor::class.java
        )

        private val floorSetProbs = arrayOf(
                floatArrayOf(0f, 70f, 20f, 8f, 2f),
                floatArrayOf(0f, 25f, 50f, 20f, 5f),
                floatArrayOf(0f, 10f, 40f, 40f, 10f),
                floatArrayOf(0f, 5f, 20f, 50f, 25f),
                floatArrayOf(0f, 2f, 8f, 20f, 70f)
        )

        override fun generate(): Item = random(CurrentFloorSet())

        fun random(floorSet: Int): Armor {
            val fs = GameMath.clamp(floorSet, 0, floorSetProbs.size - 1)

            return classes[Random.chances(floorSetProbs[fs])].newInstance().random() as Armor
        }
    }

    object WEAPON : ItemGenerator() {
        private val floorSetTierProbs = arrayOf(
                floatArrayOf(15f, 60f, 10f, 5f, 5f),
                floatArrayOf(10f, 25f, 50f, 20f, 10f),
                floatArrayOf(0f, 10f, 40f, 40f, 20f),
                floatArrayOf(0f, 0f, 20f, 50f, 30f),
                floatArrayOf(0f, 0f, 10f, 20f, 70f)
        )

        object MELEE : ItemGenerator() {
            object T1 : ClassMapGenerator<MeleeWeapon>(hashMapOf(
                    WornShortsword::class.java to 1f,
                    Knuckles::class.java to 1f,
                    Dagger::class.java to 1f,
                    MagesStaff::class.java to 0f,
                    SorceressWand::class.java to 0f,
                    BattleGloves::class.java to 1f,
                    RedHandleDagger::class.java to 0.1f
            ))

            object T2 : ClassMapGenerator<MeleeWeapon>(hashMapOf(
                    Dirk::class.java to 5f,
                    ShortSword::class.java to 6f,
                    HandAxe::class.java to 5f,
                    Spear::class.java to 5f,
                    Quarterstaff::class.java to 4f,
                    Sickle::class.java to 5f,
                    DriedLeg::class.java to 5f
            ))

            object T3 : ClassMapGenerator<MeleeWeapon>(hashMapOf(
                    Sword::class.java to 6f,
                    Mace::class.java to 5f,
                    Scimitar::class.java to 5f,
                    RoundShield::class.java to 4f,
                    Sai::class.java to 4f,
                    Whip::class.java to 4f,
                    CrystalsSwords::class.java to 4f,
                    DaggerAxe::class.java to 5f,
                    InvisibleBlade::class.java to 5f
            ))

            object T4 : ClassMapGenerator<MeleeWeapon>(hashMapOf(
                    Longsword::class.java to 6f,
                    BattleAxe::class.java to 5f,
                    Flail::class.java to 5f,
                    RunicBlade::class.java to 4f,
                    AssassinsBlade::class.java to 4f,
                    SpikeShield::class.java to 5f,
                    Pitchfork::class.java to 5f
            ))

            object T5 : ClassMapGenerator<MeleeWeapon>(hashMapOf(
                    Claymore::class.java to 6f,
                    WarHammer::class.java to 5f,
                    Glaive::class.java to 5f,
                    Greataxe::class.java to 4f,
                    Greatshield::class.java to 4f,
                    Lance::class.java to 5f
            ))

            private val Ts = arrayOf(T1, T2, T3, T4, T5)

            override fun generate(): Item = random(CurrentFloorSet())

            fun random(floorSet: Int): Weapon {
                val fs = GameMath.clamp(floorSet, 0, floorSetTierProbs.size - 1)
                return Ts[Random.chances(floorSetTierProbs[fs])].generate() as Weapon
            }

            fun tier(t: Int): ClassMapGenerator<MeleeWeapon> = Ts[t - 1]
        }

        object MISSSILE : ClassMapGenerator<MissileWeapon>(hashMapOf(
                // 1
                Boomerang::class.java to 0f,
                Dart::class.java to 12f,
                SmokeSparks::class.java to 6f,
                // 2
                Shuriken::class.java to 10f,
                SwallowDart::class.java to 10f,
                IncendiaryDart::class.java to 1f,
                CurareDart::class.java to 1f,
                CeremonialDagger::class.java to 1f,
                // 3
                FlyCutter::class.java to 8f,
                SeventhDart::class.java to 8f,
                // 4
                Javelin::class.java to 6f,
                // 5
                Tamahawk::class.java to 4f
        ))

        override fun generate(): Item = random(CurrentFloorSet())

        fun random(floorSet: Int): Weapon = (if (Random.Float() < 0.24f) MISSSILE.generate()
        else MELEE.random(floorSet)) as Weapon
    }

    object POTION : ClassMapGenerator<Potion>(hashMapOf(
            PotionOfHealing::class.java to 20f,
            PotionOfExperience::class.java to 4f,
            PotionOfToxicGas::class.java to 15f,
            PotionOfParalyticGas::class.java to 10f,
            PotionOfLiquidFlame::class.java to 15f,
            PotionOfLevitation::class.java to 10f,
            PotionOfStrength::class.java to 0f,
            PotionOfMindVision::class.java to 18f,
            PotionOfPhysique::class.java to 4f,
            PotionOfPurity::class.java to 12f,
            PotionOfInvisibility::class.java to 10f,
            PotionOfMight::class.java to 0f,
            PotionOfFrost::class.java to 10f
    ))

    object SCROLL : ClassMapGenerator<Scroll>(hashMapOf(
            ScrollOfIdentify::class.java to 30f,
            ScrollOfTeleportation::class.java to 10f,
            ScrollOfRemoveCurse::class.java to 20f,
            ScrollOfUpgrade::class.java to 0f,
            ScrollOfRecharging::class.java to 15f,
            ScrollOfMagicMapping::class.java to 15f,
            ScrollOfRage::class.java to 12f,
            ScrollOfTerror::class.java to 8f,
            ScrollOfLullaby::class.java to 8f,
            ScrollOfEnchanting::class.java to 6f,
            ScrollOfPsionicBlast::class.java to 4f,
            ScrollOfMirrorImage::class.java to 10f,
            ScrollOfCurse::class.java to 4f,
            ScrollOfLight::class.java to 6f
    ))

    object WAND : ClassMapGenerator<Wand>(hashMapOf(
            WandOfMagicMissile::class.java to 5f,
            WandOfLightning::class.java to 4f,
            WandOfDisintegration::class.java to 4f,
            WandOfFireblast::class.java to 4f,
            WandOfVenom::class.java to 4f,
            WandOfBlastWave::class.java to 3f,
            WandOfFrost::class.java to 3f,
            WandOfPrismaticLight::class.java to 3f,
            // WandOfTransfusion::class.java to 3f,
            WandOfAbel::class.java to 3f,
            WandOfCorruption::class.java to 3f,
            WandOfRegrowth::class.java to 3f,
            WandOfHypnosis::class.java to 3f
    ))

    object RING : ClassMapGenerator<Ring>(hashMapOf(
            // RingOfAccuracy::class.java to 1f,
            RingOfArcane::class.java to 1f,
            RingOfEvasion::class.java to 1f,
            RingOfResistance::class.java to 1f,
            RingOfForce::class.java to 1f,
            RingOfFuror::class.java to 1f,
            RingOfHaste::class.java to 1f,
            RingOfCritical::class.java to 1f,
            RingOfMight::class.java to 1f,
            RingOfSharpshooting::class.java to 1f,
            RingOfHealth::class.java to 1f,
            RingOfWealth::class.java to 1f
    ))

    object SEED : ClassMapGenerator<Plant.Seed>(hashMapOf(
            Firebloom.Seed::class.java to 12f,
            Icecap.Seed::class.java to 12f,
            Sorrowmoss.Seed::class.java to 12f,
            Blindweed.Seed::class.java to 12f,
            Sungrass.Seed::class.java to 12f,
            Earthroot.Seed::class.java to 12f,
            Fadeleaf.Seed::class.java to 12f,
            Rotberry.Seed::class.java to 0f,
            BlandfruitBush.Seed::class.java to 2f,
            Dreamfoil.Seed::class.java to 12f,
            Stormvine.Seed::class.java to 12f,
            Starflower.Seed::class.java to 1f
    ))

    object FOOD : ClassMapGenerator<Food>(hashMapOf(
            Food::class.java to 4f,
            Pasty::class.java to 1.25f,
            MysteryMeat::class.java to 0f
    ))

    object GOLD : ClassMapGenerator<Gold>(hashMapOf(
            Gold::class.java to 1f
    ))

    // artifact is uniquely dropping
    val INITIAL_ARTIFACT_PROBS = hashMapOf(
            CapeOfThorns::class.java to 1f,
            ChaliceOfBlood::class.java to 0f,  // by statuary
            CloakOfShadows::class.java to 0f, // for rouge
            CrackedCoin::class.java to 1f,
            HornOfPlenty::class.java to 1f,
            MasterThievesArmband::class.java to 0f, // by thief
            SandalsOfNature::class.java to 1f,
            TalismanOfForesight::class.java to 1f,
            TimekeepersHourglass::class.java to 1f,
            UnstableSpellbook::class.java to 1f,
            AlchemistsToolkit::class.java to 0f, // currently removed from drop tables,
            DriedRose::class.java to 0f, // starts with no chance of spawning, chance is set directly after beating ghost quest.
            LloydsBeacon::class.java to 0f, // by goo
            EtherealChains::class.java to 1f,
            RiemannianManifoldShield::class.java to 1f,
            GoldPlatedStatue::class.java to 1f,
            HandOfTheElder::class.java to 0f, // by undead
            HandleOfAbyss::class.java to 1f,
            HeartOfSatan::class.java to 1f,
            CloakOfSheep::class.java to 1f,
            EyeballOfTheElder.Right::class.java to 1f,
            EyeballOfTheElder.Left::class.java to 1f,
            HomurasShield::class.java to 0.5f,
            DragonsSquama::class.java to 1f
    )

    object ARTIFACT : ClassMapGenerator<Artifact>(HashMap()), Bundlable {
        private val spawned = ArrayList<String>()

        init {
            updateProbabilities()
        }

        override fun generate(): Item {
            // run out of artifacts, give a ring
            val cls = Random.chances(probMap) ?: return RING.generate()

            // be unique
            probMap[cls] = 0f
            spawned.add(cls.simpleName)
            return cls.newInstance().random()
        }

        private val lastProbMap = HashMap<Class<out Artifact>, Float>()
        private val lastSpawned = ArrayList<String>()
        fun push() {
            probMap.toMap(lastProbMap)
            lastSpawned.clear()
            lastSpawned.addAll(spawned)
        }

        fun pop() {
            lastProbMap.toMap(probMap)
            spawned.clear()
            spawned.addAll(lastSpawned)
        }

        fun reset() {
            spawned.clear()
            updateProbabilities()
        }

        fun left(): Int = probMap.count { it.value > 0f }

        // return: removed
        fun remove(artifact: Artifact): Boolean {
            if (spawned.contains(artifact.javaClass.simpleName)) return false

            spawned.add(artifact.javaClass.simpleName)
            updateProbabilities()
            return true
        }

        private fun updateProbabilities() {
            probMap.clear()
            for (pr in INITIAL_ARTIFACT_PROBS)
                probMap[pr.key] = if (spawned.contains(pr.key.simpleName)) 0f else pr.value
        }

        // save the probs
        private const val SPAWNED_ARTIFACTS = "spawned-artifacts"

        override fun restoreFromBundle(bundle: Bundle) {
            if (Ghost.Quest.completed())
                probMap[DriedRose::class.java] = 1f

            if (bundle.contains(SPAWNED_ARTIFACTS)) {
                spawned.addAll(bundle.getStringArray(SPAWNED_ARTIFACTS))

                updateProbabilities()
            }
        }

        override fun storeInBundle(bundle: Bundle) {
            bundle.put(SPAWNED_ARTIFACTS, spawned.toTypedArray())
        }
    }

    object HELMET : ClassMapGenerator<Helmet>(hashMapOf(
            HelmetBarbarian::class.java to 1f,
            HelmetCrusader::class.java to 1f,
            HoodApprentice::class.java to 1f,
            LittlePail::class.java to 1f,
            CircletEmerald::class.java to 1f,
            CrownOfDwarf::class.java to 0f, // by king 
            HeaddressRegeneration::class.java to 1f,
            WizardHat::class.java to 1f,
            MaskOfHorror::class.java to 1f,
            MaskOfClown::class.java to 1f,
            RangerHat::class.java to 0.2f, // very rare, so Yvette counts.
            MaskOfMadness::class.java to 0f, // compose
            TurtleScarf::class.java to 1f,
            MaskOfLider::class.java to 0.1f,
            GuardHelmet::class.java to 0.2f
    ))

    object BOOK : ClassMapGenerator<Book>(hashMapOf(
            CallysDiary::class.java to 0f,
            WardenSmithNotes::class.java to 0f
    ))

    object RUNE : ClassMapGenerator<Rune>(hashMapOf(
            RegenerationRune::class.java to 1f,
            MendingRune::class.java to 1f,
            CriticalRune::class.java to 0.5f,
            BrightRune::class.java to 1f,
            HasteRune::class.java to 1f,
            TreasureRune::class.java to 1f,
            BloodRune::class.java to 0f // from unholy blood
    ))

    // 
    private val InitCategoryMap = hashMapOf(
            WEAPON to 100f,
            ARMOR to 60f,
            POTION to 500f,
            SCROLL to 400f,
            WAND to 40f,
            RING to 15f,
            ARTIFACT to 15f,
            SEED to 50f,
            FOOD to 0f,
            GOLD to 500f,
            BOOK to 0f,
            HELMET to 4f,
            RUNE to 0f
    )

    private val categoryMap = HashMap<ItemGenerator, Float>()

    init {
        ResetCategoryProbs()
    }

    fun ResetCategoryProbs() {
        categoryMap.clear()
        for (pr in InitCategoryMap) categoryMap[pr.key] = pr.value
    }

    fun generate(): Item {
        val cat = Random.chances(categoryMap)
        categoryMap[cat] = categoryMap[cat]!! / 2f

        return cat.generate()
    }

    fun reset() {
        ARTIFACT.reset()
    }

    fun stash() {
        ARTIFACT.push()
    }

    fun recover() {
        ARTIFACT.pop()
    }

    fun restoreFromBundle(bundle: Bundle) {
        ARTIFACT.restoreFromBundle(bundle)
    }

    fun storeInBundle(bundle: Bundle) {
        ARTIFACT.storeInBundle(bundle)
    }

    // item sort order
    fun ItemOrder(item: Item): Int = when (item) {
        is MeleeWeapon -> 0 + item.tier
        is MissileWeapon -> 100 + item.tier
        is Armor -> 200 + item.tier
        is Helmet -> 300
        is Potion -> 400
        is Scroll -> 500
        is Wand -> 600
        is Ring -> 700
        is Artifact -> 800
        is Plant.Seed -> 900
        is Food -> 1000
        is Book -> 1100
        is Gold -> 1200
        is Bag -> Int.MAX_VALUE
        else -> Int.MAX_VALUE - 1
    }
}


