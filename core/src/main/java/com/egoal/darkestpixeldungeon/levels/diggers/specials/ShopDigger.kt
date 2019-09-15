package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Belongings
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.DPDImpShopkeeper
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.DPDShopKeeper
import com.egoal.darkestpixeldungeon.items.unclassified.Ankh
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.unclassified.Stylus
import com.egoal.darkestpixeldungeon.items.unclassified.Torch
import com.egoal.darkestpixeldungeon.items.unclassified.Weightstone
import com.egoal.darkestpixeldungeon.items.armor.LeatherArmor
import com.egoal.darkestpixeldungeon.items.armor.MailArmor
import com.egoal.darkestpixeldungeon.items.armor.ScaleArmor
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass
import com.egoal.darkestpixeldungeon.items.bags.PotionBandolier
import com.egoal.darkestpixeldungeon.items.bags.ScrollHolder
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch
import com.egoal.darkestpixeldungeon.items.bags.WandHolster
import com.egoal.darkestpixeldungeon.items.food.BrownAle
import com.egoal.darkestpixeldungeon.items.food.OverpricedRation
import com.egoal.darkestpixeldungeon.items.food.Wine
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.items.weapon.melee.BattleAxe
import com.egoal.darkestpixeldungeon.items.weapon.melee.HandAxe
import com.egoal.darkestpixeldungeon.items.weapon.melee.Longsword
import com.egoal.darkestpixeldungeon.items.weapon.melee.Mace
import com.egoal.darkestpixeldungeon.items.weapon.melee.ShortSword
import com.egoal.darkestpixeldungeon.items.weapon.melee.Sword
import com.egoal.darkestpixeldungeon.items.weapon.missiles.*
import com.egoal.darkestpixeldungeon.levels.LastShopLevel
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.egoal.darkestpixeldungeon.plants.Plant
import com.watabou.utils.PathFinder
import com.watabou.utils.Point
import com.watabou.utils.Random

import java.util.ArrayList

/**
 * Created by 93942 on 2018/12/8.
 */

class ShopDigger : RectDigger() {
    private lateinit var shopkeeper: DPDShopKeeper

    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(3, 6), Random.IntRange(3, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY_SP)
        val enter = overlappedWall(wall, rect)
        Fill(level, enter, Terrain.EMPTY)
        if (enter.area > 1)
            Set(level, enter.random(), Terrain.SIGN)

        if (!::shopkeeper.isInitialized) {
            // generate items, place shopkeeper, this would call only once.
            shopkeeper = (if (level is LastShopLevel) DPDImpShopkeeper() else DPDShopKeeper()).initSellItems()
            for (item in GenerateItems())
                shopkeeper.addItemToSell(item)
        }

        shopkeeper.pos = level.pointToCell(rect.center)
        level.mobs.add(shopkeeper)

        if (shopkeeper is DPDImpShopkeeper)
            for (i in PathFinder.NEIGHBOURS9)
                Set(level, shopkeeper.pos + i, Terrain.WATER)

        return DigResult(rect, DigResult.Type.Special)
    }

    private fun GenerateItems(): ArrayList<Item> {
        val itemsToSpawn = ArrayList<Item>()

        // potion of healing and scroll of remove curse is preferred if identified
        run {
            val s = ScrollOfRemoveCurse()
            if (s.isKnown && Random.Float() < .5f)
                itemsToSpawn.add(s)
            else
                itemsToSpawn.add(Generator.SCROLL.generate())

            val p = PotionOfHealing()
            if (p.isKnown && Random.Float() < .5f)
                itemsToSpawn.add(p)
            else
                itemsToSpawn.add(Generator.POTION.generate())
        }

        // armors and weapons 
        when (Dungeon.depth) {
            6 -> {
                itemsToSpawn.add(if (Random.Int(2) == 0) ShortSword().identify() else HandAxe().identify())
                itemsToSpawn.add(if (Random.Int(2) == 0) IncendiaryDart().quantity(Random.NormalIntRange(2, 4))
                else CurareDart().quantity(Random.NormalIntRange(1, 3)))
                itemsToSpawn.add(LeatherArmor().identify())
                itemsToSpawn.add(Torch())
            }

            11 -> {
                itemsToSpawn.add((if (Random.Int(2) == 0) Sword().identify() else Mace()).identify())
                itemsToSpawn.add(if (Random.Int(2) == 0) CurareDart().quantity(Random.NormalIntRange(2, 5))
                else Shuriken().quantity(Random.NormalIntRange(3, 6)))
                itemsToSpawn.add(MailArmor().identify())
                itemsToSpawn.add(Torch())
            }

            16 -> {
                itemsToSpawn.add((if (Random.Int(2) == 0) Longsword().identify() else BattleAxe()).identify())
                itemsToSpawn.add(if (Random.Int(2) == 0) Shuriken().quantity(Random.NormalIntRange(4, 7))
                else Javelin().quantity(Random.NormalIntRange(3, 6)))
                itemsToSpawn.add(ScaleArmor().identify())
                itemsToSpawn.add(Torch())
            }
        }
        itemsToSpawn.add(Generator.HELMET.generate().apply { cursed = false })
        itemsToSpawn.add(Torch().quantity(if (Random.Int(5) == 0) 2 else 1))

        if (Random.Float() < (Dungeon.depth / 5) * 0.2f)
            itemsToSpawn.add(SmokeSparks().quantity(Random.NormalIntRange(1, 4)))

        ChooseBag(Dungeon.hero.belongings)?.let { itemsToSpawn.add(it) }

        itemsToSpawn.add(OverpricedRation())
        itemsToSpawn.add(OverpricedRation())
        itemsToSpawn.add(Wine())
        itemsToSpawn.add(BrownAle())

        // no bombs anymore

        if (Dungeon.depth == 6) {
            itemsToSpawn.add(Ankh())
            itemsToSpawn.add(Weightstone())
        } else {
            itemsToSpawn.add(if (Random.Int(2) == 0) Ankh() else Weightstone())
        }

        // specials
        val rare = when (Random.Int(10)) {
            0 -> Generator.WAND.generate()
            1 -> Generator.RING.generate()
            else -> Stylus()
        }.apply {
            cursedKnown = false
            cursed = cursedKnown
        }

        itemsToSpawn.add(rare)

        val hourglass = Dungeon.hero.belongings.getItem(TimekeepersHourglass::class.java)
        if (hourglass != null) {
            var bags = 0
            //creates the given float percent of the remaining bags to be dropped.
            //this way players who get the hourglass late can still max it, usually.
            when (Dungeon.depth) {
                6 -> bags = Math.ceil(((5 - hourglass.sandBags) * 0.20)).toInt()
                11 -> bags = Math.ceil(((5 - hourglass.sandBags) * 0.25)).toInt()
                16 -> bags = Math.ceil(((5 - hourglass.sandBags) * 0.50)).toInt()
                21 -> bags = Math.ceil(((5 - hourglass.sandBags) * 0.80)).toInt()
            }

            for (i in 1..bags) {
                itemsToSpawn.add(TimekeepersHourglass.Companion.SandBag())
                hourglass.sandBags++
            }
        }

        return itemsToSpawn
    }

    private fun ChooseBag(pack: Belongings): Item? {
        var seeds = 0
        var scrolls = 0
        var potions = 0
        var wands = 0

        for (item in pack.backpack.items) {
            if (!Dungeon.limitedDrops.seedBag.dropped() && item is Plant.Seed)
                ++seeds
            else if (!Dungeon.limitedDrops.scrollBag.dropped() && item is Scroll)
                ++scrolls
            else if (!Dungeon.limitedDrops.potionBag.dropped() && item is Potion)
                ++potions
            else if (!Dungeon.limitedDrops.wandBag.dropped() && item is Wand)
                ++wands
        }

        //then pick whichever valid bag has the most items available to put into it.
        //note that the order here gives a perference if counts are otherwise equal
        if (seeds >= scrolls && seeds >= potions && seeds >= wands && !Dungeon
                        .limitedDrops.seedBag.dropped()) {
            Dungeon.limitedDrops.seedBag.drop()
            return SeedPouch()

        } else if (scrolls >= potions && scrolls >= wands && 
                !Dungeon.limitedDrops.scrollBag.dropped()) {
            Dungeon.limitedDrops.scrollBag.drop()
            return ScrollHolder()
        } else if (potions >= wands && !Dungeon.limitedDrops.potionBag.dropped()) {
            Dungeon.limitedDrops.potionBag.drop()
            return PotionBandolier()
        } else if (!Dungeon.limitedDrops.wandBag.dropped()) {
            Dungeon.limitedDrops.wandBag.drop()
            return WandHolster()
        }

        return null
    }
}
