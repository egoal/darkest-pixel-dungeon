package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Belongings
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Merchant
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.MerchantImp
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.LeatherArmor
import com.egoal.darkestpixeldungeon.items.armor.MailArmor
import com.egoal.darkestpixeldungeon.items.armor.ScaleArmor
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass
import com.egoal.darkestpixeldungeon.items.bags.PotionBandolier
import com.egoal.darkestpixeldungeon.items.bags.ScrollHolder
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch
import com.egoal.darkestpixeldungeon.items.bags.WandHolster
import com.egoal.darkestpixeldungeon.items.food.*
import com.egoal.darkestpixeldungeon.items.helmets.StrawHat
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.items.potions.Reagent
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse
import com.egoal.darkestpixeldungeon.items.unclassified.*
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.items.weapon.melee.*
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
import java.util.*
import kotlin.math.ceil

/**
 * Created by 93942 on 2018/12/8.
 */

class MerchantDigger : RectDigger() {
    private lateinit var shopkeeper: Merchant

    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(3, 6), Random.IntRange(3, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY_SP)
        val enter = overlappedWall(wall, rect)
        Fill(level, enter, Terrain.EMPTY)
        if (enter.area > 1)
            Set(level, enter.random(), Terrain.SIGN)

        if (!::shopkeeper.isInitialized) {
            // generate items, place shopkeeper, this would call only once.
            shopkeeper = if (level is LastShopLevel) MerchantImp() else Merchant()
            shopkeeper.initSellItems()
            for (item in GenerateItems())
                shopkeeper.addItemToSell(item)
        }

        shopkeeper.pos = level.pointToCell(rect.center)
        level.mobs.add(shopkeeper)

        if (shopkeeper is MerchantImp)
            for (i in PathFinder.NEIGHBOURS9)
                Set(level, shopkeeper.pos + i, Terrain.WATER)

        return DigResult(rect, DigResult.Type.Special)
    }

    private fun GenerateItems(): ArrayList<Item> {
        val itemsToSpawn = ArrayList<Item>()

        // potion of healing and scroll of remove curse is preferred if identified
        run {
            val s = ScrollOfRemoveCurse()
            if ((s.isKnown || Dungeon.depth >= 10) && Random.Float() < .5f)
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
                itemsToSpawn.add(MoonStone())
                itemsToSpawn.add((if (Random.Int(2) == 0) Sword().identify() else Mace()).identify())
                itemsToSpawn.add(if (Random.Int(2) == 0) CurareDart().quantity(Random.NormalIntRange(2, 5))
                else Shuriken().quantity(Random.NormalIntRange(3, 6)))
                itemsToSpawn.add(MailArmor().identify())
                itemsToSpawn.add(Torch())
            }

            16 -> {
                itemsToSpawn.add(MoonStone())
                itemsToSpawn.add((if (Random.Int(2) == 0) Longsword().identify() else BattleAxe()).identify())
                itemsToSpawn.add(if (Random.Int(2) == 0) Shuriken().quantity(Random.NormalIntRange(4, 7))
                else Javelin().quantity(Random.NormalIntRange(3, 6)))
                itemsToSpawn.add(ScaleArmor().identify())
                itemsToSpawn.add(Torch())
            }
        }
        val helmet = Generator.HELMET.generate().apply { cursed = false }
        if (helmet !is StrawHat) itemsToSpawn.add(helmet)
        itemsToSpawn.add(Torch().quantity(if (Random.Int(5) == 0) 2 else 1))

        if (Random.Float() < (Dungeon.depth / 5) * 0.2f)
            itemsToSpawn.add(SmokeSparks().quantity(Random.NormalIntRange(1, 4)))

        chooseBag(Dungeon.hero.belongings)?.let { itemsToSpawn.add(it) }

        itemsToSpawn.add(OverpricedRation())
        itemsToSpawn.add(OverpricedRation())
        repeat(2) {
            val p = Random.Float()
            val wine = when {
                p < 0.4f -> Wine()
                p < 0.7f -> BrownAle()
                p < 0.5f -> MeadWine()
                else -> RiceWine()
            }
            itemsToSpawn.add(wine)
        }
        itemsToSpawn.add(OrchidRoot())

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

        Dungeon.hero.belongings.getItem(TimekeepersHourglass::class.java)?.let {
            //creates the given float percent of the remaining bags to be dropped.
            //this way players who get the hourglass late can still max it, usually.
            val bags = when (Dungeon.depth) {
                6 -> ceil((5 - it.sandBags) * .2f).toInt()
                11 -> ceil((5 - it.sandBags) * .25f).toInt()
                16 -> ceil((5 - it.sandBags) * .5f).toInt()
                21 -> ceil((5 - it.sandBags) * .8f).toInt()
                else -> 0
            }
            repeat(bags) { i ->
                itemsToSpawn.add(TimekeepersHourglass.Companion.SandBag())
                it.sandBags++
            }
        }

        return itemsToSpawn
    }

    private fun chooseBag(pack: Belongings): Item? {
        val seeds = if (Dungeon.limitedDrops.seedBag.dropped()) -1 else pack.backpack.items.count { it is Plant.Seed }
        val scrolls = if (Dungeon.limitedDrops.scrollBag.dropped()) -1 else pack.backpack.items.count { it is Scroll }
        val potions = if (Dungeon.limitedDrops.potionBag.dropped()) -1 else pack.backpack.items.count { it is Potion || it is Reagent }
        val wands = if (Dungeon.limitedDrops.wandBag.dropped()) -1 else pack.backpack.items.count { it is Wand }

        //then pick whichever valid bag has the most items available to put into it.
        //note that the order here gives a perference if counts are otherwise equal
        if (seeds >= scrolls && seeds >= potions && seeds >= wands && seeds >= 0) {
            Dungeon.limitedDrops.seedBag.drop()
            return SeedPouch()
        } else if (scrolls >= potions && scrolls >= wands && scrolls >= 0) {
            Dungeon.limitedDrops.scrollBag.drop()
            return ScrollHolder()
        } else if (potions >= wands && potions >= 0) {
            Dungeon.limitedDrops.potionBag.drop()
            return PotionBandolier()
        } else if (wands >= 0) {
            Dungeon.limitedDrops.wandBag.drop()
            return WandHolster()
        }

        return null
    }
}
