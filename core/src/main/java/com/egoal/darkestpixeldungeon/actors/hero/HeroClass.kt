package com.egoal.darkestpixeldungeon.actors.hero

import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.hero.perks.*
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.armor.ClothArmor
import com.egoal.darkestpixeldungeon.items.armor.PlateArmor
import com.egoal.darkestpixeldungeon.items.artifacts.*
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.food.Wine
import com.egoal.darkestpixeldungeon.items.potions.*
import com.egoal.darkestpixeldungeon.items.scrolls.*
import com.egoal.darkestpixeldungeon.items.unclassified.*
import com.egoal.darkestpixeldungeon.items.wands.WandOfMagicMissile
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.*
import com.egoal.darkestpixeldungeon.items.weapon.missiles.*
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
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
            hero.HP += 1 // extra +1
            hero.HT += 1
        }

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)
            hero.belongings.weapon = WornShortsword().identify() as Weapon

            val darts = Dart(4)
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

            PotionOfHealing().setKnown()

            // perks
            hero.heroPerk.add(Drunkard())
            hero.heroPerk.add(GoodAppetite())
            hero.heroPerk.add(RavenousAppetite())
            // hero.heroPerk.add(StrongConstitution())

            // resists
            hero.addResistances(Damage.Element.FIRE, 0.1f)
            hero.addResistances(Damage.Element.LIGHT, -0.1f)
            hero.addResistances(Damage.Element.SHADOW, -0.2f)
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

            hero.heroPerk.add(GoodAppetite())
            hero.heroPerk.add(WandPerception())

            hero.addResistances(Damage.Element.FIRE, 0.1f)
            hero.addResistances(Damage.Element.POISON, -0.2f)
            hero.addResistances(Damage.Element.LIGHT, -0.1f)
            hero.addResistances(Damage.Element.SHADOW, 0.1f)
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

            hero.heroPerk.add(ExtraCritProbability())
            hero.heroPerk.add(Keen())

            ScrollOfMagicMapping().setKnown()

            hero.addResistances(Damage.Element.POISON, 0.2f)
            hero.addResistances(Damage.Element.ICE, -0.2f)
            hero.addResistances(Damage.Element.SHADOW, -0.1f)
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
            Dungeon.quickslot.setSlot(0, b)

            hero.heroPerk.add(NightVision())
            hero.heroPerk.add(Telepath())

            PotionOfMindVision().setKnown()

            hero.addResistances(Damage.Element.POISON, 0.2f)
            hero.addResistances(Damage.Element.ICE, -0.1f)
            hero.addResistances(Damage.Element.FIRE, -0.2f)
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
            hero.regeneration += 0.005f
        }

        override fun initHeroStatus(hero: Hero) {
            super.initHeroStatus(hero)

            hero.magicalResistance = 0.2f
            hero.addResistances(Damage.Element.all(), 0.1f)
            hero.addResistances(Damage.Element.POISON, 0.5f)
        }

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)

            hero.belongings.weapon = SorceressWand().identify() as Weapon

            val flask = ExtractionFlask()
            flask.identify().collect()

            val darts = Dart(6)
            darts.identify().collect()
            Dungeon.quickslot.setSlot(0, darts)

            PotionOfToxicGas().identify().collect()

            hero.heroPerk.add(Discount())
            hero.heroPerk.add(Optimistic())
        }
    };

    fun initHero(hero: Hero) {
        hero.heroClass = this

        initHeroStatus(hero)
        initHeroClass(hero)

        if (DarkestPixelDungeon.debug()) initDebug(hero)

        hero.updateAwareness()
    }

    fun title(): String = Messages.get(HeroClass::class.java, title)

    fun description(): String = M.L(HeroClass::class.java, "$title-desc")

    abstract fun masteryBadge(): Badges.Badge
    abstract fun spritesheet(): String
    abstract fun perks(): List<String>

    fun storeInBundle(bundle: Bundle) {
        bundle.put(CLASS, toString())
    }

    // common
    protected open fun initHeroStatus(hero: Hero) {
        hero.atkSkill = 10f
        hero.defSkill = 5f
        hero.magicalResistance = 0.15f
    }

    protected open fun initHeroClass(hero: Hero) {
        hero.belongings.armor = ClothArmor().identify() as Armor

        if (!Dungeon.isChallenged(Challenges.NO_FOOD)) Food().identify().collect()

        Torch().identify().collect()

        ScrollOfIdentify().setKnown()

        SeedPouch().identify().collect()
        Dungeon.limitedDrops.seedBag.drop()

//        hero.heroPerk.add(FinishingShot())
//        Sickle().identify().collect()
//        CeremonialDagger().quantity(30).collect()
//        CrackedCoin().identify().collect()
//        Dungeon.gold += 200
//        MasterThievesArmband().collect()
//        hero.atkSkill += 10f
//        Spear().identify().collect()
//        RoundShield().identify().collect()
//        AssassinsBlade().identify().collect()
//        Claymore().identify().collect()
//        Lance().identify().collect()
//        hero.defSkill += 10f
//        hero.STR += 4
//        Sword().upgrade().collect()
//        MailArmor().upgrade().collect()
//        EyeballOfTheElder.Left().collect()
//        EyeballOfTheElder.Right().collect()
//        EyeballOfTheElder().collect()
//        WandOfAbel().identify().collect()
//        hero.heroPerk.add(FinishingShot())
//        hero.heroPerk.add(ExtraPerkChoice())
//        TomeOfPerk().identify().collect()
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
            regeneration += 0.02f

            if (lvl < 10) updateAwareness()

            recoverSanity(min(Random.NormalIntRange(1, lvl * 3 / 4).toFloat(),
                    buff(Pressure::class.java)!!.pressure * 0.3f))
        }

        hero.heroPerk.get(StrongConstitution::class.java)?.upgradeHero(hero)
        hero.heroPerk.get(ExtraDexterousGrowth::class.java)?.upgradeHero(hero)
    }

    private fun initDebug(hero: Hero) {
        hero.apply {
            HT = 1000
            HP = 1
            STR = 16
            // lvl = 20
        }

        hero.heroPerk.add(IntendedTransportation())

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

    companion object {
        private const val CLASS = "class"

        fun RestoreFromBundle(bundle: Bundle): HeroClass {
            val value = bundle.getString(CLASS)
            return if (value.isNotEmpty()) valueOf(value) else ROGUE
        }
    }

}