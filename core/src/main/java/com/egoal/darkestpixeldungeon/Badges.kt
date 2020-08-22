/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon

import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.actors.mobs.Albino
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.QuickFiringGun
import com.egoal.darkestpixeldungeon.actors.mobs.Shielded
import com.egoal.darkestpixeldungeon.items.artifacts.Artifact
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.actors.mobs.Acidic
import com.egoal.darkestpixeldungeon.actors.mobs.Bandit
import com.egoal.darkestpixeldungeon.actors.mobs.Senior
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.bags.PotionBandolier
import com.egoal.darkestpixeldungeon.items.bags.ScrollHolder
import com.egoal.darkestpixeldungeon.items.bags.WandHolster
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.noosa.Game
import com.watabou.utils.Bundle
import com.watabou.utils.Callback

import java.io.IOException
import java.util.ArrayList
import java.util.Collections
import java.util.HashSet

// import kotlin.collections.HashSet

object Badges {

    private lateinit var global: HashSet<Badge>
    private var local: HashSet<Badge> = HashSet()

    private var saveNeeded = false

    var loadingListener: Callback? = null

    private const val BADGES_FILE = "badges.dat"
    private const val BADGES = "badges"

    enum class Badge(var image: Int = -1, var meta: Boolean = false) {
        MONSTERS_SLAIN_1(0),
        MONSTERS_SLAIN_2(1),
        MONSTERS_SLAIN_3(2),
        MONSTERS_SLAIN_4(3),
        GOLD_COLLECTED_1(4),
        GOLD_COLLECTED_2(5),
        GOLD_COLLECTED_3(6),
        GOLD_COLLECTED_4(7),
        LEVEL_REACHED_1(8),
        LEVEL_REACHED_2(9),
        LEVEL_REACHED_3(10),
        LEVEL_REACHED_4(11),
        ALL_POTIONS_IDENTIFIED(16),
        ALL_SCROLLS_IDENTIFIED(17),
        ALL_RINGS_IDENTIFIED(18),
        // ALL_WANDS_IDENTIFIED(19),
        ALL_ITEMS_IDENTIFIED(35, true),
        BAG_BOUGHT_SEED_POUCH,
        BAG_BOUGHT_SCROLL_HOLDER,
        BAG_BOUGHT_POTION_BANDOLIER,
        BAG_BOUGHT_WAND_HOLSTER,
        ALL_BAGS_BOUGHT(23),
        DEATH_FROM_FIRE(24),
        DEATH_FROM_POISON(25),
        DEATH_FROM_GAS(26),
        DEATH_FROM_HUNGER(27),
        DEATH_FROM_GLYPH(57),
        DEATH_FROM_FALLING(59),
        YASD(34, true),
        BOSS_SLAIN_1_WARRIOR,
        BOSS_SLAIN_1_MAGE,
        BOSS_SLAIN_1_ROGUE,
        BOSS_SLAIN_1_HUNTRESS,
        BOSS_SLAIN_1_SORCERESS,
        BOSS_SLAIN_1(12),
        BOSS_SLAIN_2(13),
        BOSS_SLAIN_3(14),
        BOSS_SLAIN_4(15),
        BOSS_SLAIN_1_ALL_CLASSES(32, true),
        BOSS_SLAIN_3_GLADIATOR,
        BOSS_SLAIN_3_BERSERKER,
        BOSS_SLAIN_3_WARLOCK,
        BOSS_SLAIN_3_BATTLEMAGE,
        BOSS_SLAIN_3_FREERUNNER,
        BOSS_SLAIN_3_ASSASSIN,
        BOSS_SLAIN_3_SNIPER,
        BOSS_SLAIN_3_WARDEN,
        BOSS_SLAIN_3_STARGAZER,
        BOSS_SLAIN_3_WITCH,
        BOSS_SLAIN_3_ALL_SUBCLASSES(33, true),
        //        RING_OF_HAGGLER(20),
//        RING_OF_THORNS(21),
        STRENGTH_ATTAINED_1(40),
        STRENGTH_ATTAINED_2(41),
        STRENGTH_ATTAINED_3(42),
        STRENGTH_ATTAINED_4(43),
        FOOD_EATEN_1(44),
        FOOD_EATEN_2(45),
        FOOD_EATEN_3(46),
        FOOD_EATEN_4(47),
        MASTERY_WARRIOR,
        MASTERY_MAGE,
        MASTERY_ROGUE,
        MASTERY_HUNTRESS,
        MASTERY_SORCERESS,
        MASTERY_EXILE,
        ITEM_LEVEL_1(48),
        ITEM_LEVEL_2(49),
        ITEM_LEVEL_3(50),
        ITEM_LEVEL_4(51),
        RARE_ALBINO,
        RARE_BANDIT,
        RARE_SHIELDED,
        RARE_SENIOR,
        RARE_ACIDIC,
        RARE_QUICK_FIRING_GUN,
        RARE(37, true),
        TUTORIAL_WARRIOR,
        TUTORIAL_MAGE,
        VICTORY_WARRIOR,
        VICTORY_MAGE,
        VICTORY_ROGUE,
        VICTORY_HUNTRESS,
        VICTORY_SORCERESS,
        VICTORY(22),
        VICTORY_ALL_CLASSES(36, true),
        MASTERY_COMBO(56),
        POTIONS_COOKED_1(52),
        POTIONS_COOKED_2(53),
        POTIONS_COOKED_3(54),
        POTIONS_COOKED_4(55),
        NO_MONSTERS_SLAIN(28),
        GRIM_WEAPON(29),
        PIRANHAS(30),
        NIGHT_HUNTER(58),
        PERK_GAIN_1(64),
        PERK_GAIN_2(65),
        PERK_GAIN_3(66),
        PERK_NONE(67, true),
        PERK_EMPTY(68, true), //err, this should be none, but it already used...
        SUICIDE(69, true),
        GAMES_PLAYED_1(60, true),
        GAMES_PLAYED_2(61, true),
        GAMES_PLAYED_3(62, true),
        GAMES_PLAYED_4(63, true),
        HAPPY_END(38, true),
        CHAMPION(39, true),
        SUPPORTER(31, true);

        fun desc(): String = M.L(this, name)
    }

    fun reset() {
        allVisiableBadges()
        local.clear()
        loadGlobal()
    }

    private fun restore(bundle: Bundle): HashSet<Badge> {
        val badges = HashSet<Badge>()
        badges.addAll(bundle.getStringArray(BADGES).map { Badge.valueOf(it) })
        return badges
    }

    private fun store(bundle: Bundle, badges: HashSet<Badge>) {
        bundle.put(BADGES, badges.map { it.toString() }.toTypedArray())
    }

    fun loadLocal(bundle: Bundle) {
        local = restore(bundle)
    }

    fun saveLocal(bundle: Bundle) {
        store(bundle, local)
    }

    fun loadGlobal() {
        if (::global.isInitialized) return

        try {
            val input = Game.instance.openFileInput(BADGES_FILE)
            val bundle = Bundle.read(input)
            input.close()

            global = restore(bundle)

        } catch (e: IOException) {
            global = HashSet()
        }
    }

    fun saveGlobal() {
        if (saveNeeded) {

            val bundle = Bundle()
            store(bundle, global!!)

            try {
                val output = Game.instance.openFileOutput(BADGES_FILE, Game
                        .MODE_PRIVATE)
                Bundle.write(bundle, output)
                output.close()
                saveNeeded = false
            } catch (e: IOException) {
                DarkestPixelDungeon.reportException(e)
            }

        }
    }

    // validations
    fun validateMonstersSlain() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null
        if (!local.contains(Badge.MONSTERS_SLAIN_1) && Statistics.EnemiesSlain >= 10) {
            badge = Badge.MONSTERS_SLAIN_1
            local.add(badge)
        }
        if (!local.contains(Badge.MONSTERS_SLAIN_2) && Statistics.EnemiesSlain >= 50) {
            badge = Badge.MONSTERS_SLAIN_2
            local.add(badge)
        }
        if (!local.contains(Badge.MONSTERS_SLAIN_3) && Statistics.EnemiesSlain >= 150) {
            badge = Badge.MONSTERS_SLAIN_3
            local.add(badge)
        }
        if (!local.contains(Badge.MONSTERS_SLAIN_4) && Statistics.EnemiesSlain >= 250) {
            badge = Badge.MONSTERS_SLAIN_4
            local.add(badge)
        }

        displayBadge(badge)
    }

    fun validateGoldCollected() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null

        if (!local.contains(Badge.GOLD_COLLECTED_1) && Statistics.GoldCollected >= 500) {
            badge = Badge.GOLD_COLLECTED_1
            local.add(badge)
        }
        if (!local.contains(Badge.GOLD_COLLECTED_2) && Statistics.GoldCollected >= 1000) {
            badge = Badge.GOLD_COLLECTED_2
            local.add(badge)
        }
        if (!local.contains(Badge.GOLD_COLLECTED_3) && Statistics.GoldCollected >= 3000) {
            badge = Badge.GOLD_COLLECTED_3
            local.add(badge)
        }
        if (!local.contains(Badge.GOLD_COLLECTED_4) && Statistics.GoldCollected >= 10000) {
            badge = Badge.GOLD_COLLECTED_4
            local.add(badge)
        }

        displayBadge(badge)
    }

    fun validateLevelReached() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null

        val lvl = Dungeon.hero.lvl

        if (!local.contains(Badge.LEVEL_REACHED_1) && lvl >= 6) {
            badge = Badge.LEVEL_REACHED_1
            local.add(badge)
        }
        if (!local.contains(Badge.LEVEL_REACHED_2) && lvl >= 12) {
            badge = Badge.LEVEL_REACHED_2
            local.add(badge)
        }
        if (!local.contains(Badge.LEVEL_REACHED_3) && lvl >= 18) {
            badge = Badge.LEVEL_REACHED_3
            local.add(badge)
        }
        if (!local.contains(Badge.LEVEL_REACHED_4) && lvl >= 24) {
            badge = Badge.LEVEL_REACHED_4
            local.add(badge)
        }

        displayBadge(badge)
    }

    fun validateStrengthAttained() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null

        val str = Dungeon.hero.STR

        if (!local.contains(Badge.STRENGTH_ATTAINED_1) && str >= 13) {
            badge = Badge.STRENGTH_ATTAINED_1
            local.add(badge)
        }
        if (!local.contains(Badge.STRENGTH_ATTAINED_2) && str >= 15) {
            badge = Badge.STRENGTH_ATTAINED_2
            local.add(badge)
        }
        if (!local.contains(Badge.STRENGTH_ATTAINED_3) && str >= 17) {
            badge = Badge.STRENGTH_ATTAINED_3
            local.add(badge)
        }
        if (!local.contains(Badge.STRENGTH_ATTAINED_4) && str >= 19) {
            badge = Badge.STRENGTH_ATTAINED_4
            local.add(badge)
        }

        displayBadge(badge)
    }

    fun validateFoodEaten() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null

        if (!local.contains(Badge.FOOD_EATEN_1) && Statistics.FoodEaten >= 10) {
            badge = Badge.FOOD_EATEN_1
            local.add(badge)
        }
        if (!local.contains(Badge.FOOD_EATEN_2) && Statistics.FoodEaten >= 20) {
            badge = Badge.FOOD_EATEN_2
            local.add(badge)
        }
        if (!local.contains(Badge.FOOD_EATEN_3) && Statistics.FoodEaten >= 30) {
            badge = Badge.FOOD_EATEN_3
            local.add(badge)
        }
        if (!local.contains(Badge.FOOD_EATEN_4) && Statistics.FoodEaten >= 40) {
            badge = Badge.FOOD_EATEN_4
            local.add(badge)
        }

        displayBadge(badge)
    }

    fun validatePotionsCooked() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null

        if (!local.contains(Badge.POTIONS_COOKED_1) && Statistics.PotionsCooked >= 3) {
            badge = Badge.POTIONS_COOKED_1
            local.add(badge)
        }
        if (!local.contains(Badge.POTIONS_COOKED_2) && Statistics.PotionsCooked >= 6) {
            badge = Badge.POTIONS_COOKED_2
            local.add(badge)
        }
        if (!local.contains(Badge.POTIONS_COOKED_3) && Statistics.PotionsCooked >= 9) {
            badge = Badge.POTIONS_COOKED_3
            local.add(badge)
        }
        if (!local.contains(Badge.POTIONS_COOKED_4) && Statistics.PotionsCooked >= 12) {
            badge = Badge.POTIONS_COOKED_4
            local.add(badge)
        }

        displayBadge(badge)
    }

    fun validatePiranhasKilled() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null

        if (!local.contains(Badge.PIRANHAS) && Statistics.PiranhasKilled >= 6) {
            badge = Badge.PIRANHAS
            local.add(badge)
        }

        displayBadge(badge)
    }

    fun validateItemLevelAquired(item: Item) {
        if (Dungeon.IsChallenged()) return

        // This method should be called:
        // 1) When an item is obtained (Item.collect)
        // 2) When an item is upgraded (ScrollOfUpgrade, ScrollOfWeaponUpgrade, 
        // ShortSword, WandOfMagicMissile)
        // 3) When an item is identified

        // Note that artifacts should never trigger this badge as they are 
        // alternatively upgraded
        if (!item.levelKnown || item is Artifact) {
            return
        }

        var badge: Badge? = null
        if (!local.contains(Badge.ITEM_LEVEL_1) && item.level() >= 3) {
            badge = Badge.ITEM_LEVEL_1
            local.add(badge)
        }
        if (!local.contains(Badge.ITEM_LEVEL_2) && item.level() >= 6) {
            badge = Badge.ITEM_LEVEL_2
            local.add(badge)
        }
        if (!local.contains(Badge.ITEM_LEVEL_3) && item.level() >= 9) {
            badge = Badge.ITEM_LEVEL_3
            local.add(badge)
        }
        if (!local.contains(Badge.ITEM_LEVEL_4) && item.level() >= 12) {
            badge = Badge.ITEM_LEVEL_4
            local.add(badge)
        }

        displayBadge(badge)
    }

    fun validateAllPotionsIdentified() {
        if (Dungeon.IsChallenged()) return

        if (Dungeon.hero != null && Dungeon.hero.isAlive &&
                !local.contains(Badge.ALL_POTIONS_IDENTIFIED) && Potion
                        .allKnown()) {

            val badge = Badge.ALL_POTIONS_IDENTIFIED
            local.add(badge)
            displayBadge(badge)

            validateAllItemsIdentified()
        }
    }

    fun validateAllScrollsIdentified() {
        if (Dungeon.IsChallenged()) return

        if (Dungeon.hero != null && Dungeon.hero.isAlive &&
                !local.contains(Badge.ALL_SCROLLS_IDENTIFIED) && Scroll.allKnown()) {

            val badge = Badge.ALL_SCROLLS_IDENTIFIED
            local.add(badge)
            displayBadge(badge)

            validateAllItemsIdentified()
        }
    }

    fun validateAllRingsIdentified() {
        if (Dungeon.IsChallenged()) return

        if (Dungeon.hero != null && Dungeon.hero.isAlive &&
                !local.contains(Badge.ALL_RINGS_IDENTIFIED) && Ring.allKnown()) {

            val badge = Badge.ALL_RINGS_IDENTIFIED
            local.add(badge)
            displayBadge(badge)

            validateAllItemsIdentified()
        }
    }

    //TODO: no longer in use, deal with new wand related badges in the badge 
    // rework.

    /**
     * public static void validateAllWandsIdentified() {
     * if (Dungeon.hero != null && Dungeon.hero.isAlive() &&
     * !local.contains( Badge.ALL_WANDS_IDENTIFIED ) && Wand.allKnown()) {
     *
     *
     * Badge badge = Badge.ALL_WANDS_IDENTIFIED;
     * local.add( badge );
     * displayBadge( badge );
     *
     *
     * validateAllItemsIdentified();
     * }
     * }
     */

    fun validateAllBagsBought(bag: Item) {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null
        if (bag is SeedPouch) {
            badge = Badge.BAG_BOUGHT_SEED_POUCH
        } else if (bag is ScrollHolder) {
            badge = Badge.BAG_BOUGHT_SCROLL_HOLDER
        } else if (bag is PotionBandolier) {
            badge = Badge.BAG_BOUGHT_POTION_BANDOLIER
        } else if (bag is WandHolster) {
            badge = Badge.BAG_BOUGHT_WAND_HOLSTER
        }

        if (badge != null) {

            local.add(badge)

            if (!local.contains(Badge.ALL_BAGS_BOUGHT) &&
                    local.contains(Badge.BAG_BOUGHT_SEED_POUCH) &&
                    local.contains(Badge.BAG_BOUGHT_SCROLL_HOLDER) &&
                    local.contains(Badge.BAG_BOUGHT_POTION_BANDOLIER) &&
                    local.contains(Badge.BAG_BOUGHT_WAND_HOLSTER)) {

                badge = Badge.ALL_BAGS_BOUGHT
                local.add(badge)
                displayBadge(badge)
            }
        }
    }

    fun validateAllItemsIdentified() {
        if (Dungeon.IsChallenged()) return

        if (!global!!.contains(Badge.ALL_ITEMS_IDENTIFIED) &&
                global!!.contains(Badge.ALL_POTIONS_IDENTIFIED) &&
                global!!.contains(Badge.ALL_SCROLLS_IDENTIFIED) &&
                global!!.contains(Badge.ALL_RINGS_IDENTIFIED)) {
            //global.contains( Badge.ALL_WANDS_IDENTIFIED )) {

            val badge = Badge.ALL_ITEMS_IDENTIFIED
            displayBadge(badge)
        }
    }

    fun validateDeathFromFire() {
        if (Dungeon.IsChallenged()) return

        val badge = Badge.DEATH_FROM_FIRE
        local.add(badge)
        displayBadge(badge)

        validateYASD()
    }

    fun validateDeathFromPoison() {
        if (Dungeon.IsChallenged()) return

        val badge = Badge.DEATH_FROM_POISON
        local.add(badge)
        displayBadge(badge)

        validateYASD()
    }

    fun validateDeathFromGas() {
        if (Dungeon.IsChallenged()) return

        val badge = Badge.DEATH_FROM_GAS
        local.add(badge)
        displayBadge(badge)

        validateYASD()
    }

    fun validateDeathFromHunger() {
        if (Dungeon.IsChallenged()) return

        val badge = Badge.DEATH_FROM_HUNGER
        local.add(badge)
        displayBadge(badge)

        validateYASD()
    }

    fun validateDeathFromGlyph() {
        if (Dungeon.IsChallenged()) return

        val badge = Badge.DEATH_FROM_GLYPH
        local.add(badge)
        displayBadge(badge)
    }

    fun validateDeathFromFalling() {
        if (Dungeon.IsChallenged()) return

        val badge = Badge.DEATH_FROM_FALLING
        local.add(badge)
        displayBadge(badge)
    }

    private fun validateYASD() {
        if (Dungeon.IsChallenged()) return

        if (global!!.contains(Badge.DEATH_FROM_FIRE) &&
                global!!.contains(Badge.DEATH_FROM_POISON) &&
                global!!.contains(Badge.DEATH_FROM_GAS) &&
                global!!.contains(Badge.DEATH_FROM_HUNGER)) {

            val badge = Badge.YASD
            local.add(badge)
            displayBadge(badge)
        }
    }

    fun validateBossSlain() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null
        when (Dungeon.depth) {
            5 -> badge = Badge.BOSS_SLAIN_1
            10 -> badge = Badge.BOSS_SLAIN_2
            15 -> badge = Badge.BOSS_SLAIN_3
            20 -> badge = Badge.BOSS_SLAIN_4
        }

        if (badge != null) {
            local.add(badge)
            displayBadge(badge)

            if (badge == Badge.BOSS_SLAIN_1) {
                when (Dungeon.hero.heroClass) {
                    HeroClass.WARRIOR -> badge = Badge.BOSS_SLAIN_1_WARRIOR
                    HeroClass.MAGE -> badge = Badge.BOSS_SLAIN_1_MAGE
                    HeroClass.ROGUE -> badge = Badge.BOSS_SLAIN_1_ROGUE
                    HeroClass.HUNTRESS -> badge = Badge.BOSS_SLAIN_1_HUNTRESS
                    HeroClass.SORCERESS -> badge = Badge.BOSS_SLAIN_1_SORCERESS
                }
                local.add(badge)
                if (!global!!.contains(badge)) {
                    global!!.add(badge)
                    saveNeeded = true
                }

                if (global!!.contains(Badge.BOSS_SLAIN_1_WARRIOR) &&
                        global!!.contains(Badge.BOSS_SLAIN_1_MAGE) &&
                        global!!.contains(Badge.BOSS_SLAIN_1_ROGUE) &&
                        global!!.contains(Badge.BOSS_SLAIN_1_HUNTRESS) &&
                        global!!.contains(Badge.BOSS_SLAIN_1_SORCERESS)) {

                    badge = Badge.BOSS_SLAIN_1_ALL_CLASSES
                    if (!global!!.contains(badge)) {
                        displayBadge(badge)
                        global!!.add(badge)
                        saveNeeded = true
                    }
                }
            } else if (badge == Badge.BOSS_SLAIN_3) {
                when (Dungeon.hero.subClass) {
                    HeroSubClass.GLADIATOR -> badge = Badge.BOSS_SLAIN_3_GLADIATOR
                    HeroSubClass.BERSERKER -> badge = Badge.BOSS_SLAIN_3_BERSERKER
                    HeroSubClass.WARLOCK -> badge = Badge.BOSS_SLAIN_3_WARLOCK
                    HeroSubClass.BATTLEMAGE -> badge = Badge.BOSS_SLAIN_3_BATTLEMAGE
                    HeroSubClass.FREERUNNER -> badge = Badge.BOSS_SLAIN_3_FREERUNNER
                    HeroSubClass.ASSASSIN -> badge = Badge.BOSS_SLAIN_3_ASSASSIN
                    HeroSubClass.SNIPER -> badge = Badge.BOSS_SLAIN_3_SNIPER
                    HeroSubClass.WARDEN -> badge = Badge.BOSS_SLAIN_3_WARDEN
                    HeroSubClass.STARGAZER -> badge = Badge.BOSS_SLAIN_3_STARGAZER
                    HeroSubClass.WITCH -> badge = Badge.BOSS_SLAIN_3_WITCH
                    else -> return
                }
                local.add(badge)
                if (!global!!.contains(badge)) {
                    global!!.add(badge)
                    saveNeeded = true
                }

                if (global!!.contains(Badge.BOSS_SLAIN_3_GLADIATOR) &&
                        global!!.contains(Badge.BOSS_SLAIN_3_BERSERKER) &&
                        global!!.contains(Badge.BOSS_SLAIN_3_WARLOCK) &&
                        global!!.contains(Badge.BOSS_SLAIN_3_BATTLEMAGE) &&
                        global!!.contains(Badge.BOSS_SLAIN_3_FREERUNNER) &&
                        global!!.contains(Badge.BOSS_SLAIN_3_ASSASSIN) &&
                        global!!.contains(Badge.BOSS_SLAIN_3_SNIPER) &&
                        global!!.contains(Badge.BOSS_SLAIN_3_WARDEN) &&
                        global!!.contains(Badge.BOSS_SLAIN_3_STARGAZER) &&
                        global!!.contains(Badge.BOSS_SLAIN_3_WITCH)) {

                    badge = Badge.BOSS_SLAIN_3_ALL_SUBCLASSES
                    if (!global!!.contains(badge)) {
                        displayBadge(badge)
                        global!!.add(badge)
                        saveNeeded = true
                    }
                }
            }
        }
    }

    fun validateMastery() {
        if (Dungeon.IsChallenged()) return

        val badge = Dungeon.hero.heroClass.masteryBadge()

        if (!global.contains(badge)) {
            global.add(badge)
            saveNeeded = true
        }
    }

    fun validateMasteryCombo(n: Int) {
        if (Dungeon.IsChallenged()) return

        if (!local.contains(Badge.MASTERY_COMBO) && n == 10) {
            val badge = Badge.MASTERY_COMBO
            local.add(badge)
            displayBadge(badge)
        }
    }

    fun validateRare(mob: Mob) {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null
        if (mob is Albino) {
            badge = Badge.RARE_ALBINO
        } else if (mob is Bandit) {
            badge = Badge.RARE_BANDIT
        } else if (mob is Shielded) {
            badge = Badge.RARE_SHIELDED
        } else if (mob is Senior) {
            badge = Badge.RARE_SENIOR
        } else if (mob is Acidic) {
            badge = Badge.RARE_ACIDIC
        } else if (mob is QuickFiringGun)
            badge = Badge.RARE_QUICK_FIRING_GUN
        else return

        if (!global!!.contains(badge)) {
            global!!.add(badge!!)
            saveNeeded = true
        }

        if (global!!.contains(Badge.RARE_ALBINO) &&
                global!!.contains(Badge.RARE_BANDIT) &&
                global!!.contains(Badge.RARE_SHIELDED) &&
                global!!.contains(Badge.RARE_SENIOR) &&
                global!!.contains(Badge.RARE_ACIDIC) &&
                global!!.contains(Badge.RARE_QUICK_FIRING_GUN)) {

            badge = Badge.RARE
            displayBadge(badge)
        }
    }

    fun validateVictory() {
        if (Dungeon.IsChallenged()) return

        var badge = Badge.VICTORY
        displayBadge(badge)

        when (Dungeon.hero.heroClass) {
            HeroClass.WARRIOR -> badge = Badge.VICTORY_WARRIOR
            HeroClass.MAGE -> badge = Badge.VICTORY_MAGE
            HeroClass.ROGUE -> badge = Badge.VICTORY_ROGUE
            HeroClass.HUNTRESS -> badge = Badge.VICTORY_HUNTRESS
            HeroClass.SORCERESS -> badge = Badge.VICTORY_SORCERESS
        }
        local.add(badge)
        if (!global!!.contains(badge)) {
            global!!.add(badge)
            saveNeeded = true
        }

        if (global!!.contains(Badge.VICTORY_WARRIOR) &&
                global!!.contains(Badge.VICTORY_MAGE) &&
                global!!.contains(Badge.VICTORY_ROGUE) &&
                global!!.contains(Badge.VICTORY_HUNTRESS) &&
                global!!.contains(Badge.VICTORY_SORCERESS)) {

            badge = Badge.VICTORY_ALL_CLASSES
            displayBadge(badge)
        }
    }

    fun validateTutorial() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null
        when (Dungeon.hero.heroClass) {
            HeroClass.WARRIOR -> badge = Badge.TUTORIAL_WARRIOR
            HeroClass.MAGE -> badge = Badge.TUTORIAL_MAGE
            else -> {
            }
        }

        if (badge != null) {
            local.add(badge)
            if (!global!!.contains(badge)) {
                global!!.add(badge)
                saveNeeded = true
            }
        }
    }

    fun validateNoKilling() {
        if (Dungeon.IsChallenged()) return

        if (!local.contains(Badge.NO_MONSTERS_SLAIN) && Statistics
                        .CompletedWithNoKilling) {
            val badge = Badge.NO_MONSTERS_SLAIN
            local.add(badge)
            displayBadge(badge)
        }
    }

    fun validateGrimWeapon() {
        if (Dungeon.IsChallenged()) return

        if (!local.contains(Badge.GRIM_WEAPON)) {
            val badge = Badge.GRIM_WEAPON
            local.add(badge)
            displayBadge(badge)
        }
    }

    fun validateNightHunter() {
        if (Dungeon.IsChallenged()) return

        if (!local.contains(Badge.NIGHT_HUNTER) && Statistics
                        .NightHunt >= 15) {
            val badge = Badge.NIGHT_HUNTER
            local.add(badge)
            displayBadge(badge)
        }
    }

    fun validateSupporter() {
        if (Dungeon.IsChallenged()) return

        global!!.add(Badge.SUPPORTER)
        saveNeeded = true

        PixelScene.showBadge(Badge.SUPPORTER)
    }

    fun validateGamesPlayed() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null
        if (Rankings.totalNumber >= 10) {
            badge = Badge.GAMES_PLAYED_1
        }
        if (Rankings.totalNumber >= 100) {
            badge = Badge.GAMES_PLAYED_2
        }
        if (Rankings.totalNumber >= 500) {
            badge = Badge.GAMES_PLAYED_3
        }
        if (Rankings.totalNumber >= 2000) {
            badge = Badge.GAMES_PLAYED_4
        }

        displayBadge(badge)
    }

    fun validateGainPerk() {
        if (Dungeon.IsChallenged()) return

        var badge: Badge? = null
        if (!local.contains(Badge.PERK_GAIN_1) && Dungeon.hero.perkGained >= 3) {
            badge = Badge.PERK_GAIN_1
            local.add(Badge.PERK_GAIN_1)
        }
        if (!local.contains(Badge.PERK_GAIN_2) && Dungeon.hero.perkGained >= 6) {
            badge = Badge.PERK_GAIN_2
            local.add(Badge.PERK_GAIN_2)
        }
        if (!local.contains(Badge.PERK_GAIN_3) && Dungeon.hero.perkGained >= 9) {
            badge = Badge.PERK_GAIN_3
            local.add(Badge.PERK_GAIN_3)
        }

        displayBadge(badge)
    }

    fun validateNoPerk() {
        if (Dungeon.IsChallenged()) return

        if (Dungeon.hero.heroPerk.perks.isEmpty()) displayBadge(Badge.PERK_EMPTY)
    }

    fun validateNeverGainPerk() {
        if (Dungeon.IsChallenged()) return

        if (Dungeon.hero.perkGained == 0) displayBadge(Badge.PERK_NONE)
    }

    fun validateSuicide() {
        if (Dungeon.IsChallenged()) return

        displayBadge(Badge.SUICIDE)
    }

    fun validateHappyEnd() {
        if (Dungeon.IsChallenged()) return

        displayBadge(Badge.HAPPY_END)
    }

    fun validateChampion() {
//        if (Dungeon.IsChallenged()) return
        if (Dungeon.hero.challenge != null && Dungeon.hero.challenge != Challenge.LowPressure)
            displayBadge(Badge.CHAMPION)
    }

    private fun displayBadge(badge: Badge?) {
        if (badge == null) {
            return
        }

        if (global.contains(badge)) {

            if (!badge.meta) {
                GLog.h(Messages.get(Badges::class.java, "endorsed", badge.desc()))
            }

        } else {
            global.add(badge)
            saveNeeded = true

            if (badge.meta) {
                GLog.h(Messages.get(Badges::class.java, "new_super", badge.desc()))
            } else {
                GLog.h(Messages.get(Badges::class.java, "new", badge.desc()))
            }
            PixelScene.showBadge(badge)
        }
    }

    ///
    fun isUnlocked(badge: Badge): Boolean = global.contains(badge)

    fun disown(badge: Badge) {
        loadGlobal()
        global.remove(badge)
        saveNeeded = true
    }

    //todo: you still need to refactor badge...
    fun allVisiableBadges(): HashSet<Badge> {
        val badges = Badge.values().filter { it.image != -1 }.toHashSet()

        keep(badges, Badge.MONSTERS_SLAIN_1, Badge.MONSTERS_SLAIN_2, Badge.MONSTERS_SLAIN_3, Badge.MONSTERS_SLAIN_4)
        keep(badges, Badge.GOLD_COLLECTED_1, Badge.GOLD_COLLECTED_2, Badge.GOLD_COLLECTED_3, Badge.GOLD_COLLECTED_4)
        keep(badges, Badge.BOSS_SLAIN_1, Badge.BOSS_SLAIN_2, Badge.BOSS_SLAIN_3, Badge.BOSS_SLAIN_4)
        keep(badges, Badge.LEVEL_REACHED_1, Badge.LEVEL_REACHED_2, Badge.LEVEL_REACHED_3, Badge.LEVEL_REACHED_4)
        keep(badges, Badge.STRENGTH_ATTAINED_1, Badge.STRENGTH_ATTAINED_2,
                Badge.STRENGTH_ATTAINED_3, Badge.STRENGTH_ATTAINED_4)
        keep(badges, Badge.FOOD_EATEN_1, Badge.FOOD_EATEN_2, Badge
                .FOOD_EATEN_3, Badge.FOOD_EATEN_4)
        keep(badges, Badge.ITEM_LEVEL_1, Badge.ITEM_LEVEL_2, Badge
                .ITEM_LEVEL_3, Badge.ITEM_LEVEL_4)
        keep(badges, Badge.POTIONS_COOKED_1, Badge.POTIONS_COOKED_2, Badge
                .POTIONS_COOKED_3, Badge.POTIONS_COOKED_4)
        keep(badges, Badge.BOSS_SLAIN_1_ALL_CLASSES, Badge
                .BOSS_SLAIN_3_ALL_SUBCLASSES)
        keep(badges, Badge.DEATH_FROM_FIRE, Badge.YASD)
        keep(badges, Badge.DEATH_FROM_GAS, Badge.YASD)
        keep(badges, Badge.DEATH_FROM_HUNGER, Badge.YASD)
        keep(badges, Badge.DEATH_FROM_POISON, Badge.YASD)
        keep(badges, Badge.ALL_POTIONS_IDENTIFIED, Badge
                .ALL_ITEMS_IDENTIFIED)
        keep(badges, Badge.ALL_SCROLLS_IDENTIFIED, Badge
                .ALL_ITEMS_IDENTIFIED)
        keep(badges, Badge.ALL_RINGS_IDENTIFIED, Badge.ALL_ITEMS_IDENTIFIED)
        // keep(badges, Badge.ALL_WANDS_IDENTIFIED, Badge.ALL_ITEMS_IDENTIFIED)
        keep(badges, Badge.VICTORY, Badge.VICTORY_ALL_CLASSES)
        keep(badges, Badge.VICTORY, Badge.HAPPY_END)
        keep(badges, Badge.VICTORY, Badge.CHAMPION)
        keep(badges, Badge.GAMES_PLAYED_1, Badge.GAMES_PLAYED_2, Badge
                .GAMES_PLAYED_3, Badge.GAMES_PLAYED_4)
        keep(badges, Badge.PERK_GAIN_1, Badge.PERK_GAIN_2, Badge.PERK_GAIN_3)

        return badges
    }

    private fun keep(list: HashSet<Badge>, vararg badges: Badge) {
        for (i in badges.size - 1 downTo 1) {
            if (global.contains(badges[i])) {
                for (j in 0 until i) list.remove(badges[j])
                return
            }
            list.remove(badges[i])
        }
    }

    fun filtered(global: Boolean): List<Badge> {
        val filtered = HashSet(if (global) Badges.global else Badges.local)

        val iterator = filtered.iterator()
        while (iterator.hasNext()) {
            val badge = iterator.next()
            if ((!global && badge.meta) || badge.image == -1) {
                iterator.remove()
            }
        }

        leaveBest(filtered, Badge.MONSTERS_SLAIN_1, Badge.MONSTERS_SLAIN_2, Badge.MONSTERS_SLAIN_3, Badge.MONSTERS_SLAIN_4)
        leaveBest(filtered, Badge.GOLD_COLLECTED_1, Badge.GOLD_COLLECTED_2, Badge.GOLD_COLLECTED_3, Badge.GOLD_COLLECTED_4)
        leaveBest(filtered, Badge.BOSS_SLAIN_1, Badge.BOSS_SLAIN_2, Badge.BOSS_SLAIN_3, Badge.BOSS_SLAIN_4)
        leaveBest(filtered, Badge.LEVEL_REACHED_1, Badge.LEVEL_REACHED_2, Badge.LEVEL_REACHED_3, Badge.LEVEL_REACHED_4)
        leaveBest(filtered, Badge.STRENGTH_ATTAINED_1, Badge.STRENGTH_ATTAINED_2,
                Badge.STRENGTH_ATTAINED_3, Badge.STRENGTH_ATTAINED_4)
        leaveBest(filtered, Badge.FOOD_EATEN_1, Badge.FOOD_EATEN_2, Badge
                .FOOD_EATEN_3, Badge.FOOD_EATEN_4)
        leaveBest(filtered, Badge.ITEM_LEVEL_1, Badge.ITEM_LEVEL_2, Badge
                .ITEM_LEVEL_3, Badge.ITEM_LEVEL_4)
        leaveBest(filtered, Badge.POTIONS_COOKED_1, Badge.POTIONS_COOKED_2, Badge
                .POTIONS_COOKED_3, Badge.POTIONS_COOKED_4)
        leaveBest(filtered, Badge.BOSS_SLAIN_1_ALL_CLASSES, Badge
                .BOSS_SLAIN_3_ALL_SUBCLASSES)
        leaveBest(filtered, Badge.DEATH_FROM_FIRE, Badge.YASD)
        leaveBest(filtered, Badge.DEATH_FROM_GAS, Badge.YASD)
        leaveBest(filtered, Badge.DEATH_FROM_HUNGER, Badge.YASD)
        leaveBest(filtered, Badge.DEATH_FROM_POISON, Badge.YASD)
        leaveBest(filtered, Badge.ALL_POTIONS_IDENTIFIED, Badge
                .ALL_ITEMS_IDENTIFIED)
        leaveBest(filtered, Badge.ALL_SCROLLS_IDENTIFIED, Badge
                .ALL_ITEMS_IDENTIFIED)
        leaveBest(filtered, Badge.ALL_RINGS_IDENTIFIED, Badge.ALL_ITEMS_IDENTIFIED)
        // leaveBest(filtered, Badge.ALL_WANDS_IDENTIFIED, Badge.ALL_ITEMS_IDENTIFIED)
        leaveBest(filtered, Badge.VICTORY, Badge.VICTORY_ALL_CLASSES)
        leaveBest(filtered, Badge.VICTORY, Badge.HAPPY_END)
        leaveBest(filtered, Badge.VICTORY, Badge.CHAMPION)
        leaveBest(filtered, Badge.GAMES_PLAYED_1, Badge.GAMES_PLAYED_2, Badge
                .GAMES_PLAYED_3, Badge.GAMES_PLAYED_4)
        leaveBest(filtered, Badge.PERK_GAIN_1, Badge.PERK_GAIN_2, Badge.PERK_GAIN_3)

        return filtered.sorted()
    }

    private fun leaveBest(list: HashSet<Badge>, vararg badges: Badge) {
        for (i in badges.size - 1 downTo 1) {
            if (list.contains(badges[i])) {
                for (j in 0 until i) {
                    list.remove(badges[j])
                }
                break
            }
        }
    }
}
