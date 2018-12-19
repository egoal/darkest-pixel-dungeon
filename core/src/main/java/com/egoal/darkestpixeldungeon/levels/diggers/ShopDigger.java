package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Belongings;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.DPDImpShopkeeper;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.DPDShopKeeper;
import com.egoal.darkestpixeldungeon.items.Ankh;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.Stylus;
import com.egoal.darkestpixeldungeon.items.Torch;
import com.egoal.darkestpixeldungeon.items.Weightstone;
import com.egoal.darkestpixeldungeon.items.armor.LeatherArmor;
import com.egoal.darkestpixeldungeon.items.armor.MailArmor;
import com.egoal.darkestpixeldungeon.items.armor.ScaleArmor;
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.egoal.darkestpixeldungeon.items.bags.PotionBandolier;
import com.egoal.darkestpixeldungeon.items.bags.ScrollHolder;
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch;
import com.egoal.darkestpixeldungeon.items.bags.WandHolster;
import com.egoal.darkestpixeldungeon.items.food.OverpricedRation;
import com.egoal.darkestpixeldungeon.items.food.Wine;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing;
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.egoal.darkestpixeldungeon.items.wands.Wand;
import com.egoal.darkestpixeldungeon.items.weapon.melee.BattleAxe;
import com.egoal.darkestpixeldungeon.items.weapon.melee.HandAxe;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Longsword;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Mace;
import com.egoal.darkestpixeldungeon.items.weapon.melee.NewShortsword;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Sword;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.CurareDart;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.IncendiaryDart;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Javelin;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Shuriken;
import com.egoal.darkestpixeldungeon.levels.LastShopLevel;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.plants.Plant;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 2018/12/8.
 */

public class ShopDigger extends RectDigger {

  private ArrayList<Item> itemsToSpawn;

  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(3, 6), Random.IntRange(3, 6));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY_SP);
    XRect enter = overlapedWall(wall, rect);
    Fill(level, enter, Terrain.EMPTY);
    if (enter.h() > 1 || enter.w() > 1)
      Set(level, enter.random(), Terrain.SIGN);

    // generate items, place shopkeeper
    if (itemsToSpawn == null)
      itemsToSpawn = GenerateItems();

    DPDShopKeeper dsk = level instanceof LastShopLevel ?
            new DPDImpShopkeeper() : new DPDShopKeeper();
    dsk.pos = level.pointToCell(rect.cen());
    level.mobs.add(dsk);

    if (dsk instanceof DPDImpShopkeeper)
      for (int i : PathFinder.NEIGHBOURS9)
        Set(level, ((DPDImpShopkeeper) dsk).pos + i, Terrain.WATER);

    for (Item item : itemsToSpawn)
      dsk.addItemToSell(item);

    return new DigResult(DigResult.Type.SPECIAL);
  }

  private static ArrayList<Item> GenerateItems() {
    ArrayList<Item> itemsToSpawn = new ArrayList<>();

    // potion of healing and scroll of remove curse is preferred if identified
    {
      ScrollOfRemoveCurse s = new ScrollOfRemoveCurse();
      if (s.isKnown() && Random.Float() < .5f)
        itemsToSpawn.add(s);
      else
        itemsToSpawn.add(Generator.random(Generator.Category.SCROLL));

      PotionOfHealing p = new PotionOfHealing();
      if (p.isKnown() && Random.Float() < .5f)
        itemsToSpawn.add(p);
      else
        itemsToSpawn.add(Generator.random(Generator.Category.POTION));
    }

    // armors and weapons 
    switch (Dungeon.depth) {
      case 6:
        itemsToSpawn.add((Random.Int(2) == 0 ? new NewShortsword().identify()
                : new HandAxe()).identify());
        itemsToSpawn.add(Random.Int(2) == 0 ?
                new IncendiaryDart().quantity(Random.NormalIntRange(2, 4)) :
                new CurareDart().quantity(Random.NormalIntRange(1, 3)));
        itemsToSpawn.add(new LeatherArmor().identify());
        itemsToSpawn.add(new Torch());
        if (Random.Int(5) == 0)
          itemsToSpawn.add(new Torch());
        break;

      case 11:
        itemsToSpawn.add((Random.Int(2) == 0 ? new Sword().identify() : new
                Mace()).identify());
        itemsToSpawn.add(Random.Int(2) == 0 ?
                new CurareDart().quantity(Random.NormalIntRange(2, 5)) :
                new Shuriken().quantity(Random.NormalIntRange(3, 6)));
        itemsToSpawn.add(new MailArmor().identify());
        itemsToSpawn.add(new Torch());
        if (Random.Int(6) == 0)
          itemsToSpawn.add(new Torch());
        break;

      case 16:
        itemsToSpawn.add((Random.Int(2) == 0 ? new Longsword().identify() :
                new BattleAxe()).identify());
        itemsToSpawn.add(Random.Int(2) == 0 ?
                new Shuriken().quantity(Random.NormalIntRange(4, 7)) :
                new Javelin().quantity(Random.NormalIntRange(3, 6)));
        itemsToSpawn.add(new ScaleArmor().identify());
        itemsToSpawn.add(new Torch());
        if (Random.Int(10) == 0)
          itemsToSpawn.add(new Torch());
        break;
    }

    Item bag = ChooseBag(Dungeon.hero.belongings);
    if (bag != null)
      itemsToSpawn.add(bag);

    itemsToSpawn.add(new OverpricedRation());
    itemsToSpawn.add(new OverpricedRation());
    itemsToSpawn.add(new Wine());

    // no bombs anymore

    if (Dungeon.depth == 6) {
      itemsToSpawn.add(new Ankh());
      itemsToSpawn.add(new Weightstone());
    } else {
      itemsToSpawn.add(Random.Int(2) == 0 ? new Ankh() : new Weightstone());
    }

    // specials
    Item rare;
    switch (Random.Int(10)) {
      case 0:
        rare = Generator.random(Generator.Category.WAND);
        rare.level(0);
        break;
      case 1:
        rare = Generator.random(Generator.Category.RING);
        rare.level(1);
        break;
      default:
        rare = new Stylus();
    }
    rare.cursed = rare.cursedKnown = false;
    itemsToSpawn.add(rare);

    TimekeepersHourglass hourglass = Dungeon.hero.belongings.getItem
            (TimekeepersHourglass.class);
    if (hourglass != null) {
      int bags = 0;
      //creates the given float percent of the remaining bags to be dropped.
      //this way players who get the hourglass late can still max it, usually.
      switch (Dungeon.depth) {
        case 6:
          bags = (int) Math.ceil((5 - hourglass.sandBags) * 0.20f);
          break;
        case 11:
          bags = (int) Math.ceil((5 - hourglass.sandBags) * 0.25f);
          break;
        case 16:
          bags = (int) Math.ceil((5 - hourglass.sandBags) * 0.50f);
          break;
        case 21:
          bags = (int) Math.ceil((5 - hourglass.sandBags) * 0.80f);
          break;
      }

      for (int i = 1; i <= bags; i++) {
        itemsToSpawn.add(new TimekeepersHourglass.sandBag());
        hourglass.sandBags++;
      }
    }

    return itemsToSpawn;
  }

  private static Item ChooseBag(Belongings pack) {
    int seeds = 0, scrolls = 0, potions = 0, wands = 0;

    for (Item item : pack.backpack.items) {
      if (!Dungeon.limitedDrops.seedBag.dropped() && item instanceof Plant.Seed)
        ++seeds;
      else if (!Dungeon.limitedDrops.scrollBag.dropped() &&
              item instanceof Scroll)
        ++scrolls;
      else if (!Dungeon.limitedDrops.potionBag.dropped() &&
              item instanceof Potion)
        ++potions;
      else if (!Dungeon.limitedDrops.wandBag.dropped() && item instanceof Wand)
        ++wands;
    }

    //then pick whichever valid bag has the most items available to put into it.
    //note that the order here gives a perference if counts are otherwise equal
    if (seeds >= scrolls && seeds >= potions && seeds >= wands && !Dungeon
            .limitedDrops.seedBag.dropped()) {
      Dungeon.limitedDrops.seedBag.drop();
      return new SeedPouch();

    } else if (scrolls >= potions && scrolls >= wands && !Dungeon
            .limitedDrops.scrollBag.dropped()) {
      Dungeon.limitedDrops.scrollBag.drop();
      return new ScrollHolder();

    } else if (potions >= wands && !Dungeon.limitedDrops.potionBag.dropped()) {
      Dungeon.limitedDrops.potionBag.drop();
      return new PotionBandolier();

    } else if (!Dungeon.limitedDrops.wandBag.dropped()) {
      Dungeon.limitedDrops.wandBag.drop();
      return new WandHolster();
    }

    return null;
  }
}
