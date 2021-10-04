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
import com.egoal.darkestpixeldungeon.items.food.OrchidRoot
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
import kotlin.reflect.KClass

object Generator {
    fun CurrentFloorSet(): Int = Dungeon.depth / 5

    abstract class ItemGenerator {
        abstract fun generate(): Item
        abstract fun reset()
    }

    open class ClassMapGenerator(val probMap: HashMap<KClass<out Item>, Float>) : ItemGenerator() {
        override fun generate(): Item = (Random.chances(probMap).java.newInstance() as Item).random()
        override fun reset() {}
    }

    open class BalancedClassMapGenerator(val initialProbs: HashMap<KClass<out Item>, Float>) : ItemGenerator() {
        private val currentProbs = hashMapOf<KClass<out Item>, Float>()

        init {
            reset()
        }

        override fun generate(): Item {
            val it = Random.chances(currentProbs)
            currentProbs[it] = currentProbs[it]!! / 2f // lower its prob, this is the "balanced"
            return it.java.newInstance().random()
        }

        override fun reset() {
            currentProbs.clear()
            for (pr in initialProbs) currentProbs[pr.key] = pr.value
        }
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
        override fun reset() {}

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
            object T1 : BalancedClassMapGenerator(hashMapOf(
                    WornShortsword::class to 0.5f,
                    Knuckles::class to 1f,
                    Dagger::class to 0.5f,
                    MagesStaff::class to 0f,
                    SorceressWand::class to 0f,
                    BattleGloves::class to 1f,
                    RedHandleDagger::class to 0.5f,
                    ShortSpear::class to 1f,
                    ParryingDagger::class to 0.5f
            ))

            object T2 : BalancedClassMapGenerator(hashMapOf(
                    Dirk::class to 5f,
                    ShortSword::class to 6f,
                    HandAxe::class to 5f,
                    Spear::class to 5f,
                    Quarterstaff::class to 4f,
                    Sickle::class to 5f,
                    DriedLeg::class to 5f,
                    Tulwar::class to 4f,
                    CeremonialSword::class to 4f,
                    ButchersKnife::class to 5f
            ))

            object T3 : BalancedClassMapGenerator(hashMapOf(
                    Sword::class to 6f,
                    Mace::class to 5f,
                    Scimitar::class to 5f,
                    RoundShield::class to 4f,
                    Sai::class to 4f,
                    Whip::class to 4f,
                    CrystalsSwords::class to 4f,
                    DaggerAxe::class to 5f,
                    InvisibleBlade::class to 5f,
                    BoethiahsBlade::class to 0f // by statuary
            ))

            object T4 : BalancedClassMapGenerator(hashMapOf(
                    Longsword::class to 6f,
                    BattleAxe::class to 5f,
                    Flail::class to 5f,
                    RunicBlade::class to 4f,
                    AssassinsBlade::class to 4f,
                    SpikeShield::class to 5f,
                    Pitchfork::class to 5f,
                    Halberd::class to 4f,
                    Scythe::class to 4f
            ))

            object T5 : BalancedClassMapGenerator(hashMapOf(
                    Claymore::class to 6f,
                    WarHammer::class to 5f,
                    Glaive::class to 5f,
                    Greataxe::class to 4f,
                    Greatshield::class to 4f,
                    Lance::class to 5f
            ))

            private val Ts = arrayOf(T1, T2, T3, T4, T5)

            override fun generate(): Item = random(CurrentFloorSet())

            fun random(floorSet: Int): Weapon {
                val fs = GameMath.clamp(floorSet, 0, floorSetTierProbs.size - 1)
                return Ts[Random.chances(floorSetTierProbs[fs])].generate() as Weapon
            }

            fun tier(t: Int): ItemGenerator = Ts[t - 1]

            override fun reset() {
                for (t in Ts) t.reset()
            }
        }

        object MISSSILE : BalancedClassMapGenerator(hashMapOf(
                // 1
                Boomerang::class to 0f,
                Dart::class to 8f,
                SmokeSparks::class to 6f,
                Salt::class to 4f,
                // 2
                Shuriken::class to 10f,
                SwallowDart::class to 10f,
                IncendiaryDart::class to 1f,
                CurareDart::class to 1f,
                CeremonialDagger::class to 1f,
                // 3
                FlyCutter::class to 8f,
                SeventhDart::class to 8f,
                RefinedSalt::class to 6f,
                // 4
                Javelin::class to 6f,
                // 5
                Tamahawk::class to 4f
        ))

        override fun generate(): Item = random(CurrentFloorSet())

        override fun reset() {
            MELEE.reset()
            MISSSILE.reset()
        }

        fun random(floorSet: Int): Weapon = (if (Random.Float() < 0.24f) MISSSILE.generate()
        else MELEE.random(floorSet)) as Weapon
    }

    object POTION : BalancedClassMapGenerator(hashMapOf(
            PotionOfHealing::class to 20f,
            PotionOfExperience::class to 4f,
            PotionOfToxicGas::class to 15f,
            PotionOfParalyticGas::class to 10f,
            PotionOfLiquidFlame::class to 15f,
            PotionOfLevitation::class to 10f,
            PotionOfStrength::class to 0f,
            PotionOfMindVision::class to 18f,
            PotionOfPhysique::class to 4f,
            PotionOfPurity::class to 12f,
            PotionOfInvisibility::class to 10f,
            PotionOfMight::class to 0f,
            PotionOfFrost::class to 10f
    ))

    object SCROLL : BalancedClassMapGenerator(hashMapOf(
            ScrollOfIdentify::class to 30f,
            ScrollOfTeleportation::class to 10f,
            ScrollOfRemoveCurse::class to 20f,
            ScrollOfUpgrade::class to 0f,
            ScrollOfRecharging::class to 15f,
            ScrollOfMagicMapping::class to 15f,
            ScrollOfRage::class to 12f,
            ScrollOfTerror::class to 8f,
            ScrollOfLullaby::class to 8f,
            // ScrollOfEnchanting::class to 6f,
            ScrollOfPsionicBlast::class to 4f,
            ScrollOfMirrorImage::class to 10f,
            ScrollOfCurse::class to 4f,
            ScrollOfLight::class to 6f
    ))

    object WAND : BalancedClassMapGenerator(hashMapOf(
            WandOfMagicMissile::class to 5f,
            WandOfLightning::class to 4f,
            WandOfDisintegration::class to 4f,
            WandOfFireblast::class to 4f,
            WandOfVenom::class to 4f,
            WandOfBlastWave::class to 3f,
            WandOfFrost::class to 3f,
            WandOfPrismaticLight::class to 3f,
            // WandOfTransfusion::class to 3f,
            WandOfAbel::class to 3f,
            WandOfCorruption::class to 3f,
            WandOfRegrowth::class to 3f,
            WandOfHypnosis::class to 3f
    ))

    object RING : BalancedClassMapGenerator(hashMapOf(
            // RingOfAccuracy::class to 1f,
            RingOfArcane::class to 1f,
            RingOfEvasion::class to 1f,
            RingOfResistance::class to 1f,
            RingOfForce::class to 1f,
            RingOfFuror::class to 1f,
            RingOfHaste::class to 1f,
            RingOfCritical::class to 1f,
            RingOfMight::class to 1f,
            RingOfSharpshooting::class to 1f,
            RingOfHealth::class to 1f,
            RingOfWealth::class to 1f
    ))

    object SEED : BalancedClassMapGenerator(hashMapOf(
            Firebloom.Seed::class to 12f,
            Icecap.Seed::class to 12f,
            Sorrowmoss.Seed::class to 12f,
            Blindweed.Seed::class to 12f,
            CorrodeCyan.Seed::class to 12f,
            Sungrass.Seed::class to 12f,
            Earthroot.Seed::class to 12f,
            Fadeleaf.Seed::class to 12f,
            Rotberry.Seed::class to 0f,
            BlandfruitBush.Seed::class to 2f,
            Dreamfoil.Seed::class to 12f,
            Stormvine.Seed::class to 12f,
            Starflower.Seed::class to 1f
    ))

    object FOOD : BalancedClassMapGenerator(hashMapOf(
            Food::class to 5f,
            Pasty::class to 1f,
            MysteryMeat::class to 0f,
            OrchidRoot::class to 2f
    ))

    object GOLD : ClassMapGenerator(hashMapOf(
            Gold::class to 1f
    ))

    // artifact is uniquely dropping
    val INITIAL_ARTIFACT_PROBS = hashMapOf(
            CapeOfThorns::class to 1f,
            ChaliceOfBlood::class to 0f,  // by statuary
            CloakOfShadows::class to 0f, // for rouge
            CrackedCoin::class to 1f,
            HornOfPlenty::class to 1f,
            MasterThievesArmband::class to 0f, // by thief
            SandalsOfNature::class to 1f,
            TalismanOfForesight::class to 1f,
            TimekeepersHourglass::class to 1f,
            UnstableSpellbook::class to 1f,
            AlchemistsToolkit::class to 0f, // currently removed from drop tables,
            DriedRose::class to 0f, // starts with no chance of spawning, chance is set directly after beating ghost quest.
            LloydsBeacon::class to 0f, // by goo
            EtherealChains::class to 1f,
            RiemannianManifoldShield::class to 1f,
            GoldPlatedStatue::class to 1f,
            HandOfTheElder::class to 0f, // by undead
            HandleOfAbyss::class to 1f,
            HeartOfSatan::class to 1f,
            CloakOfSheep::class to 1f,
            EyeballOfTheElder.Right::class to 1f,
            EyeballOfTheElder.Left::class to 1f,
            // HomurasShield::class to 0.5f,
            DragonsSquama::class to 1f,
            GoddessRadiance::class to 0f
    )

    object ARTIFACT : ClassMapGenerator(HashMap()), Bundlable {
        private val spawned = ArrayList<String>()

        init {
            updateProbabilities()
        }

        override fun generate(): Item {
            // run out of artifacts, give a ring
            val cls = Random.chances(probMap) ?: return RING.generate()

            // be unique
            probMap[cls] = 0f
            spawned.add(cls.java.simpleName)
            return cls.java.newInstance().random()
        }

        private val lastProbMap = HashMap<KClass<out Item>, Float>()
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

        override fun reset() {
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
                probMap[pr.key] = if (spawned.contains(pr.key.java.simpleName)) 0f else pr.value
        }

        // save the probs
        private const val SPAWNED_ARTIFACTS = "spawned-artifacts"

        override fun storeInBundle(bundle: Bundle) {
            bundle.put(SPAWNED_ARTIFACTS, spawned.toTypedArray())
        }

        override fun restoreFromBundle(bundle: Bundle) {
            if (Ghost.Quest.completed()) probMap[DriedRose::class] = 1f

            if (bundle.contains(SPAWNED_ARTIFACTS)) {
                spawned.addAll(bundle.getStringArray(SPAWNED_ARTIFACTS))

                updateProbabilities()
            }
        }
    }

    object HELMET : BalancedClassMapGenerator(hashMapOf(
            HelmetBarbarian::class to 1f,
            HelmetCrusader::class to 1f,
            HoodApprentice::class to 1f,
            LittlePail::class to 1f,
            CircletEmerald::class to 1f,
            CrownOfDwarf::class to 0f, // by king 
            HeaddressRegeneration::class to 1f,
            WizardHat::class to 1f,
            MaskOfHorror::class to 1f,
            MaskOfClown::class to 1f,
            RangerHat::class to 0.2f, // very rare, so Yvette counts.
            MaskOfMadness::class to 0f, // compose
            TurtleScarf::class to 1f,
            MaskOfLider::class to 0.1f,
            GuardHelmet::class to 0.1f,
            StrawHat::class to 0.1f,
            Mantilla::class to 1f
    ))

    object BOOK : BalancedClassMapGenerator(hashMapOf(
            CallysDiary::class to 0f,
            WardenSmithNotes::class to 0f
    ))

    object RUNE : BalancedClassMapGenerator(hashMapOf(
            RegenerationRune::class to 1f,
            MendingRune::class to 1f,
            CriticalRune::class to 0.5f,
            BrightRune::class to 1f,
            HasteRune::class to 1f,
            TreasureRune::class to 1f,
            BloodRune::class to 0f // from unholy blood
    ))

    // 
    private val InitCategoryMap = hashMapOf(
            WEAPON to 100f,
            ARMOR to 60f,
            POTION to 400f,
            SCROLL to 300f,
            WAND to 40f,
            RING to 15f,
            ARTIFACT to 10f,
            SEED to 50f,
            FOOD to 0f,
            GOLD to 400f,
            BOOK to 0f,
            HELMET to 4f,
            RUNE to 0f
    )

    private val categoryMap = HashMap<ItemGenerator, Float>()

    init {
        resetCategoryProbs()
    }

    fun resetCategoryProbs() {
        categoryMap.clear()
        for (pr in InitCategoryMap) categoryMap[pr.key] = pr.value
    }

    fun generate(): Item {
        val cat = Random.chances(categoryMap)
        categoryMap[cat] = categoryMap[cat]!! / 2f // simply lower its probs

        return cat.generate()
    }

    // reset all prob: for start a new game
    fun reset() {
        ARMOR.reset()
        WEAPON.reset()
        POTION.reset()
        SCROLL.reset()
        WAND.reset()
        RING.reset()
        SEED.reset()
        FOOD.reset()
        GOLD.reset()
        ARTIFACT.reset()
        HELMET.reset()
        BOOK.reset()
        RUNE.reset()
    }

    fun stash() {
        ARTIFACT.push()
        //todo: i may stash the balanced map, this keep the probmap consistent, until the player restart the game instead.
    }

    fun recover() {
        ARTIFACT.pop()
        // currently, each time a new level is generated, the balanced prob map would be reset.
        //fixme: see 'todo' in stash(),

        ARMOR.reset()
        WEAPON.reset()
        POTION.reset()
        SCROLL.reset()
        WAND.reset()
        RING.reset()
        SEED.reset()
        FOOD.reset()
        GOLD.reset()
        // ARTIFACT.reset()
        HELMET.reset()
        BOOK.reset()
        RUNE.reset()
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


