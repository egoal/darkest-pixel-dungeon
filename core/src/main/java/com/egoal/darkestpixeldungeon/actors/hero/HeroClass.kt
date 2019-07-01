package com.egoal.darkestpixeldungeon.actors.hero

import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.armor.ClothArmor
import com.egoal.darkestpixeldungeon.items.armor.LeatherArmor
import com.egoal.darkestpixeldungeon.items.armor.PlateArmor
import com.egoal.darkestpixeldungeon.items.artifacts.*
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch
import com.egoal.darkestpixeldungeon.items.books.textbook.CallysDiary
import com.egoal.darkestpixeldungeon.items.books.textbook.WardenSmithNotes
import com.egoal.darkestpixeldungeon.items.books.textbook.YvettesDiary
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.food.Wine
import com.egoal.darkestpixeldungeon.items.helmets.CrownOfDwarf
import com.egoal.darkestpixeldungeon.items.helmets.MaskOfClown
import com.egoal.darkestpixeldungeon.items.potions.*
import com.egoal.darkestpixeldungeon.items.scrolls.*
import com.egoal.darkestpixeldungeon.items.unclassified.*
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.items.wands.WandOfMagicMissile
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.*
import com.egoal.darkestpixeldungeon.items.weapon.missiles.*
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Icecap
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.min

enum class HeroClass(private val title: String) {
    WARRIOR("warrior") {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_WARRIOR
        override fun spritesheet(): String = Assets.WARRIOR
        override fun perks(): List<String> = (1..5).map { Messages.get(HeroClass::class.java, "warrior_perk$it") }

        override fun initHeroStatus(hero: Hero) {
            super.initHeroStatus(hero)
            hero.HP += 5 // 25
            hero.HT += 5
        }

        override fun upgradeHero(hero: Hero) {
            super.upgradeHero(hero)
            hero.HT += 1
            hero.HP += 1
        }

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)
            hero.belongings.weapon = WornShortsword().identify() as Weapon

            val darts = Dart(3)
            darts.identify().collect()

            Wine().collect()

            if (Badges.isUnlocked(Badges.Badge.TUTORIAL_WARRIOR)) {
                hero.belongings.armor.affixSeal(BrokenSeal())
                Dungeon.quickslot.setSlot(0, darts)
            } else {
                val seal = BrokenSeal()
                seal.collect()
                Dungeon.quickslot.setSlot(0, seal)
                Dungeon.quickslot.setSlot(1, darts)
            }

            hero.heroPerk.add(HeroPerk.Perk.DRUNKARD)

            PotionOfHealing().setKnown()

            // resists
            hero.addResistances(Damage.Element.FIRE, 1.1f)
            hero.addResistances(Damage.Element.LIGHT, .9f)
            hero.addResistances(Damage.Element.SHADOW, .8f, .9f)
        }
    },

    MAGE("mage") {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_MAGE
        override fun spritesheet(): String = Assets.MAGE
        override fun perks(): List<String> = (1..5).map { Messages.get(HeroClass::class.java, "mage_perk$it") }

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)

            val staff =
                    if (Badges.isUnlocked(Badges.Badge.TUTORIAL_MAGE)) MagesStaff(WandOfMagicMissile())
                    else {
                        WandOfMagicMissile().identify().collect()
                        MagesStaff()
                    }

            hero.belongings.weapon = staff
            staff.identify()
            staff.activate(hero)

            Dungeon.quickslot.setSlot(0, staff)

            ScrollOfUpgrade().setKnown()

            hero.addResistances(Damage.Element.FIRE, 1f, 1.2f)
            hero.addResistances(Damage.Element.POISON, .8f)
            hero.addResistances(Damage.Element.LIGHT, 1.1f)
        }
    },

    ROGUE("rogue") {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_ROGUE
        override fun spritesheet(): String = Assets.ROGUE
        override fun perks(): List<String> = (1..6).map { Messages.get(HeroClass::class.java, "rogue_perk$it") }

        override fun upgradeHero(hero: Hero) {
            super.upgradeHero(hero)
            hero.criticalChance += 0.1f / 100f
        }

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)

            hero.belongings.weapon = Dagger().identify() as Weapon

            val cloak = CloakOfShadows()
            hero.belongings.misc1 = cloak
            cloak.identify()
            cloak.activate(hero)

            val darts = Dart(8)
            darts.identify().collect()

            Dungeon.quickslot.setSlot(0, cloak)
            Dungeon.quickslot.setSlot(1, darts)

            hero.heroPerk.add(HeroPerk.Perk.CRITICAL_STRIKE)
            hero.heroPerk.add(HeroPerk.Perk.KEEN)

            ScrollOfMagicMapping().setKnown()

            hero.addResistances(Damage.Element.POISON, 1.2f)
            hero.addResistances(Damage.Element.ICE, .8f, .9f)
            hero.addResistances(Damage.Element.SHADOW, .9f, 1.1f)
        }
    },

    HUNTRESS("huntress") {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_HUNTRESS
        override fun spritesheet(): String = Assets.HUNTRESS
        override fun perks(): List<String> = (1..5).map { Messages.get(HeroClass::class.java, "huntress_perk$it") }

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)

            hero.belongings.weapon = Knuckles().identify() as Weapon

            val b = Boomerang()
            b.identify().collect()
            // b.enchantByBlueprint()
            Dungeon.quickslot.setSlot(0, b)

            hero.heroPerk.add(HeroPerk.Perk.NIGHT_VISION)
            hero.heroPerk.add(HeroPerk.Perk.SHOOTER)

            PotionOfMindVision().setKnown()

            hero.addResistances(Damage.Element.POISON, 1.2f)
            hero.addResistances(Damage.Element.ICE, .9f)
            hero.addResistances(Damage.Element.ACID, .9f)
        }
    },

    SORCERESS("sorceress") {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_SORCERESS
        override fun spritesheet(): String = Assets.DPD_SORCERESS
        override fun perks(): List<String> = (1..5).map { Messages.get(HeroClass::class.java, "sorceress_perk$it") }

        override fun upgradeHero(hero: Hero) {
            super.upgradeHero(hero)
            hero.HT -= 1
            hero.HP -= 1
            hero.regeneration += 0.003f
        }

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)

            hero.belongings.weapon = SorceressWand().identify() as Weapon

            val flask = ExtractionFlask()
            flask.identify().collect()
            // Dungeon.quickslot.setSlot(0, flask)

            val darts = Dart(6)
            darts.identify().collect()
            Dungeon.quickslot.setSlot(0, darts)

            PotionOfToxicGas().identify().collect()

            hero.heroPerk.add(HeroPerk.Perk.SHREWD)
            hero.heroPerk.add(HeroPerk.Perk.POSITIVE)

            // resists and extra resists to poison
            for (i in 0 until Damage.Element.ELEMENT_COUNT)
                hero.addResistances(1 shl i, 1.25f, 1f)
            hero.addResistances(Damage.Element.POISON, 2f)
        }
    };

    fun initHero(hero: Hero) {
        hero.heroClass = this

        initHeroStatus(hero)
        initHeroClass(hero)

        if (DarkestPixelDungeon.debug()) initDebug(hero)

        initPerks(hero)
        hero.updateAwareness()
    }

    fun title(): String = Messages.get(HeroClass::class.java, title)

    abstract fun masteryBadge(): Badges.Badge
    abstract fun spritesheet(): String
    abstract fun perks(): List<String>

    fun storeInBundle(bundle: Bundle) {
        bundle.put(CLASS, toString())
    }

    // common
    protected open fun initHeroStatus(hero: Hero) {}

    protected open fun initHeroClass(hero: Hero) {
        hero.belongings.armor = ClothArmor().identify() as Armor

        if (!Dungeon.isChallenged(Challenges.NO_FOOD)) Food().identify().collect()

        Torch().identify().collect()

        ScrollOfIdentify().setKnown()

        SeedPouch().identify().collect()
        Dungeon.limitedDrops.seedBag.drop()

//        GreatBlueprint().identify().collect()
//        WandOfBlastWave().identify().collect()
//        ArmorKit().collect()
//        LeatherArmor().identify().collect()
//        CallysDiary().identify().collect()
//        WardenSmithNotes().identify().collect()
    }

    // called when hero level up
    open fun upgradeHero(hero: Hero) {
        hero.apply {
            lvl++
            HT += 5
            HP += 5

            atkSkill++
            defSkill++

            criticalChance += 0.4f / 100f
            regeneration += 0.015f

            if (lvl < 10) updateAwareness()

            recoverSanity(min(Random.NormalIntRange(1, lvl * 3 / 4).toFloat(),
                    buff(Pressure::class.java)!!.pressure * 0.3f))
        }
    }

    private fun initDebug(hero: Hero) {
        hero.apply {
            HT = 1000
            HP = 1
            STR = 16
            // lvl = 20
        }

        hero.heroPerk.add(HeroPerk.Perk.INTENDED_TRANSPORTATION)

        Dungeon.quickslot.setSlot(5, ScrollOfMagicMapping().apply {
            quantity(99).identify().collect()
        })
        Dungeon.quickslot.setSlot(4, ScrollOfTeleportation().apply {
            quantity(99).identify().collect()
        })
        Dungeon.quickslot.setSlot(3, PotionOfExperience().apply {
            quantity(99).identify().collect()
        })

        PotionOfHealing().quantity(99).identify().collect()
        PotionOfMindVision().quantity(99).identify().collect()
        PotionOfLiquidFlame().quantity(99).identify().collect()

        Torch().quantity(99).identify().collect()

        PlateArmor().identify().upgrade(6).collect()
        Claymore().identify().upgrade(6).collect()

        GreatBlueprint().collect()

        Amulet().collect()
    }

    private fun initPerks(hero: Hero) {
        if (hero.heroPerk.contain(HeroPerk.Perk.CRITICAL_STRIKE))
            hero.criticalChance += 0.05f
    }

    companion object {
        private const val CLASS = "class"

        fun RestoreFromBundle(bundle: Bundle): HeroClass {
            val value = bundle.getString(CLASS)
            return if (value.isNotEmpty()) valueOf(value) else ROGUE
        }
    }

}