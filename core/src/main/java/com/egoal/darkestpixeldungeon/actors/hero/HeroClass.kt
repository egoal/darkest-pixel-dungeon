package com.egoal.darkestpixeldungeon.actors.hero

import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.hero.perks.*
import com.egoal.darkestpixeldungeon.effects.PerkGain
import com.egoal.darkestpixeldungeon.items.armor.*
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Peaceful
import com.egoal.darkestpixeldungeon.items.artifacts.*
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch
import com.egoal.darkestpixeldungeon.items.bags.SkillTree
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.food.Wine
import com.egoal.darkestpixeldungeon.items.helmets.CollarOfSlave
import com.egoal.darkestpixeldungeon.items.helmets.Headgear
import com.egoal.darkestpixeldungeon.items.helmets.RangerHat
import com.egoal.darkestpixeldungeon.items.potions.*
import com.egoal.darkestpixeldungeon.items.scrolls.*
import com.egoal.darkestpixeldungeon.items.specials.Astrolabe
import com.egoal.darkestpixeldungeon.items.specials.Penetration
import com.egoal.darkestpixeldungeon.items.specials.Shadowmoon
import com.egoal.darkestpixeldungeon.items.specials.UrnOfShadow
import com.egoal.darkestpixeldungeon.items.unclassified.*
import com.egoal.darkestpixeldungeon.items.wands.*
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.*
import com.egoal.darkestpixeldungeon.items.weapon.missiles.*
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.CorrodeCyan
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.min

enum class HeroClass(private val title: String, vararg subclasses: HeroSubClass) {
    WARRIOR("warrior", HeroSubClass.GLADIATOR, HeroSubClass.BERSERKER, HeroSubClass.KNIGHTT) {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_WARRIOR
        override fun spritesheet(): String = Assets.WARRIOR
        override fun perks(): List<String> = (1..3).map { Messages.get(HeroClass::class.java, "warrior_perk$it") }
        override fun initialPerks(): List<Perk> = listOf(Drunkard(), GoodAppetite(), RavenousAppetite())

        override fun initHeroStatus(hero: Hero) {
            super.initHeroStatus(hero)
//            hero.HP += 5 // 25
//            hero.HT += 5
            hero.MSHLD += 3
        }

        override fun onHeroUpgraded(hero: Hero) {
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
                hero.belongings.armor!!.affixSeal(BrokenSeal())
                Dungeon.quickslot.setSlot(0, darts)
            } else {
                val seal = BrokenSeal()
                seal.collect()
                Dungeon.quickslot.setSlot(0, seal)
                Dungeon.quickslot.setSlot(1, darts)
            }

            PotionOfStrength().setKnown()

            // resists
            hero.addResistances(Damage.Element.FIRE, 0.1f)
            hero.addResistances(Damage.Element.LIGHT, -0.1f)
            hero.addResistances(Damage.Element.SHADOW, -0.2f)
        }
    },

    MAGE("mage", HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK, HeroSubClass.ARCHMAGE) {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_MAGE
        override fun spritesheet(): String = Assets.MAGE
        override fun perks(): List<String> = (1..3).map { Messages.get(HeroClass::class.java, "mage_perk$it") }
        override fun initialPerks(): List<Perk> = listOf(GoodAppetite(), WandPerception(), PreheatedZap())

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)

//            val staff =
//                    if (Badges.isUnlocked(Badges.Badge.TUTORIAL_MAGE)) MagesStaff(WandOfMagicMissile())
//                    else {
//                        WandOfMagicMissile().identify().collect()
//                        MagesStaff()
//                    }
//
//            hero.belongings.weapon = staff
//            staff.identify()
//            staff.activate(hero)
            hero.belongings.weapon = ParryingDagger().identify() as Weapon

            val staff = WandOfMagicMissile().identify()
            staff.collect()
            Dungeon.quickslot.setSlot(0, staff)

            ScrollOfUpgrade().setKnown()

            hero.addResistances(Damage.Element.FIRE, 0.1f)
            hero.addResistances(Damage.Element.POISON, -0.2f)
            hero.addResistances(Damage.Element.LIGHT, -0.1f)
            hero.addResistances(Damage.Element.SHADOW, 0.1f)
        }

        override fun upgradeHero(hero: Hero) {
            super.upgradeHero(hero)
            if (hero.lvl == 12) {
                val p = WandPerception()
                if (p.isAcquireAllowed(hero)) {
                    hero.heroPerk.add(p)
                    PerkGain.Show(hero, p)

                    GLog.p(M.L(p, "gain_level_2"))
                }
            }
        }
    },

    ROGUE("rogue", HeroSubClass.FREERUNNER, HeroSubClass.ASSASSIN) {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_ROGUE
        override fun spritesheet(): String = Assets.ROGUE
        override fun perks(): List<String> = (1..4).map { Messages.get(HeroClass::class.java, "rogue_perk$it") }
        override fun initialPerks(): List<Perk> = listOf(LowWeightDexterous(), Dieting(), ExtraCritProbability(), Keen())

        override fun onHeroUpgraded(hero: Hero) {
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

            ScrollOfMagicMapping().setKnown()

            hero.addResistances(Damage.Element.POISON, 0.2f)
            hero.addResistances(Damage.Element.ICE, -0.2f)
            hero.addResistances(Damage.Element.SHADOW, -0.1f)
        }
    },

    HUNTRESS("huntress", HeroSubClass.SNIPER, HeroSubClass.WARDEN, HeroSubClass.MOONRIDER) {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_HUNTRESS
        override fun spritesheet(): String = Assets.HUNTRESS
        override fun perks(): List<String> = (1..4).map { Messages.get(HeroClass::class.java, "huntress_perk$it") }
        override fun initialPerks(): List<Perk> = listOf(NightVision(), Telepath())

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)

            hero.belongings.weapon = Knuckles().identify() as Weapon

//            val rh = RangersHook()
//            hero.belongings.misc1 = rh
//            rh.identify()
//            rh.activate(hero)
//            Dungeon.quickslot.setSlot(0, rh)
;
            val b = Boomerang()
            b.identify().collect()
            Dungeon.quickslot.setSlot(0, b)

            PotionOfMindVision().setKnown()

            hero.addResistances(Damage.Element.POISON, 0.2f)
            hero.addResistances(Damage.Element.ICE, -0.1f)
            hero.addResistances(Damage.Element.FIRE, -0.2f)
        }
    },

    SORCERESS("sorceress", HeroSubClass.STARGAZER, HeroSubClass.WITCH) {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_SORCERESS
        override fun spritesheet(): String = Assets.DPD_SORCERESS
        override fun perks(): List<String> = (1..4).map { Messages.get(HeroClass::class.java, "sorceress_perk$it") }
        override fun initialPerks(): List<Perk> = listOf(GoodAppetite(), Discount(), Optimistic())

        override fun onHeroUpgraded(hero: Hero) {
            hero.HT -= 1
            hero.HP -= 1
            hero.regeneration += 0.01f
        }

        override fun initHeroStatus(hero: Hero) {
            super.initHeroStatus(hero)

            hero.regeneration += 0.02f
            hero.magicalResistance = 0.15f
            hero.addResistances(Damage.Element.all(), 0.1f)
            hero.addResistances(Damage.Element.POISON, 0.5f)
        }

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)

            hero.belongings.weapon = SorceressWand().identify() as Weapon

            val flask = ExtractionFlask()
            flask.identify().collect()
            Dungeon.quickslot.setSlot(0, flask)

            val darts = Salt(6)
            darts.identify().collect()
            Dungeon.quickslot.setSlot(1, darts)

            PotionOfToxicGas().identify().collect()
        }
    },

    EXILE("exile", HeroSubClass.LANCER, HeroSubClass.WINEBIBBER) {
        override fun masteryBadge(): Badges.Badge = Badges.Badge.MASTERY_EXILE
        override fun spritesheet(): String = Assets.EXILE
        override fun perks(): List<String> = (1..3).map { Messages.get(HeroClass::class.java, "exile_perk$it") }
        override fun initialPerks(): List<Perk> = listOf(LowHealthRegeneration(), Discount().apply { level = -1 }, PolearmMaster())

        override fun initHeroStatus(hero: Hero) {
            super.initHeroStatus(hero)

            hero.magicalResistance = 0.04f
            hero.addResistances(Damage.Element.ICE, 0.25f)
            hero.addResistances(Damage.Element.FIRE, -0.2f)
            hero.addResistances(Damage.Element.SHADOW, 0.2f)
            hero.addResistances(Damage.Element.LIGHT, -0.1f)
        }

        override fun initHeroClass(hero: Hero) {
            super.initHeroClass(hero)

            hero.belongings.weapon = ShortSpear().identify() as Weapon

            val gourd = Gourd()
            gourd.identify().collect()
            Dungeon.quickslot.setSlot(0, gourd)

            val fc = FlyCutter(3)
            fc.identify().collect()
            Dungeon.quickslot.setSlot(1, fc)

            ScrollOfRage().identify()
        }
    }
    ;

    fun initHero(hero: Hero) {
        hero.heroClass = this

        initHeroStatus(hero)
        initHeroClass(hero)

        if (DarkestPixelDungeon.debug()) initDebug(hero)
    }

    fun title(): String = Messages.get(HeroClass::class.java, title)

    fun desc(): String = M.L(HeroClass::class.java, "${title}_desc")

    val subClasses = subclasses.toList()

    abstract fun masteryBadge(): Badges.Badge
    abstract fun spritesheet(): String
    abstract fun perks(): List<String>
    abstract fun initialPerks(): List<Perk>

    fun storeInBundle(bundle: Bundle) {
        bundle.put(CLASS, toString())
    }

    // common
    protected open fun initHeroStatus(hero: Hero) {
        hero.atkSkill = 10f
        hero.defSkill = 5f
        hero.magicalResistance = 0.08f
    }

    protected open fun initHeroClass(hero: Hero) {
        hero.belongings.armor = ClothArmor().identify() as Armor

        Food().collect()
        Dungeon.torch += 1f

        ScrollOfIdentify().setKnown()

        SeedPouch().identify().collect()
        Dungeon.limitedDrops.seedBag.drop()

        for (p in initialPerks()) hero.heroPerk.add(p)

        SkillTree().identify().collect()

//        ChaliceOfBlood().collect()


//        initDebug(hero)
    }

    // called when hero level up
    open fun upgradeHero(hero: Hero) {
        val ht = hero.HT
        val hp = hero.HP

        hero.apply {
            lvl++
            HT += 5
            HP += 5

            atkSkill++
            defSkill += 0.75f

            criticalChance += 0.4f / 100f
            regeneration += 0.03f

            recoverSanity(min(Random.NormalIntRange(1, lvl * 3 / 4).toFloat(),
                    buff(Pressure::class.java)!!.pressure * 0.3f))
        }

        hero.heroPerk.get(StrongConstitution::class.java)?.upgradeHero(hero)
        hero.heroPerk.get(ExtraDexterousGrowth::class.java)?.upgradeHero(hero)

        onHeroUpgraded(hero)

        if (hero.challenge == Challenge.Faith || hero.challenge == Challenge.Immortality) {
            hero.HP = hp
            hero.HT = ht
        }
        if (hero.challenge == Challenge.Faith)
            Ankh().apply { isBlessed = true }.collect()
    }

    protected open fun onHeroUpgraded(hero: Hero) {}

    private fun initDebug(hero: Hero) {
        // for (i in 1..20) upgradeHero(hero)
        hero.atkSkill = 30f
        hero.defSkill = 30f
        hero.STR = 20
        hero.HT = 1000
        hero.HP = hero.HT

        hero.heroPerk.add(IntendedTransportation())

        Dungeon.quickslot.setSlot(6, FlyCutter().apply {
            quantity(99).identify().collect()
        })
        Dungeon.quickslot.setSlot(5, ScrollOfMagicMapping().apply {
            quantity(99).identify().collect()
        })
        Dungeon.quickslot.setSlot(4, ScrollOfTeleportation().apply {
            quantity(99).identify().collect()
        })
        Dungeon.quickslot.setSlot(3, PotionOfMindVision().apply {
            quantity(99).identify().collect()
        })

        PotionOfHealing().quantity(99).identify().collect()
        PotionOfLiquidFlame().quantity(99).identify().collect()
        PotionOfExperience().quantity(99).identify().collect()

        PlateArmor().identify().upgrade(6).collect()
        Claymore().identify().upgrade(6).collect()

        Headgear().collect()

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