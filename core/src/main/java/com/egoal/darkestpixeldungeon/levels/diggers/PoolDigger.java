package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.mobs.Piranha;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfInvisibility;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/8.
 */

public class PoolDigger extends RectDigger {
  private static final int NUM_PIRANHAS = 3;

  protected Point chooseRoomSize(XWall wall) {
    if(Random.Int(3)==0)
      return new Point(Random.IntRange(7, 9), Random.IntRange(7, 9));
    return super.chooseRoomSize(wall);
  }
  
  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    if (rect.w()>=7 && rect.h() >= 7)
      return digBig(level, wall, rect);

    Fill(level, rect, Terrain.WATER);

    Point door = overlapedWall(wall, rect).random();
    Set(level, door, Terrain.DOOR);

    Point plat = rect.cen();
    switch (wall.direction) {
      case LEFT:
        plat.x = rect.x1;
        break;
      case RIGHT:
        plat.x = rect.x2;
        break;
      case UP:
        plat.y = rect.y1;
        break;
      case DOWN:
        plat.y = rect.y2;
        break;
    }
    Set(level, plat, Terrain.PEDESTAL);

    // items
    level.drop(prize(level), level.pointToCell(plat)).type =
            Random.Int(3) == 0 ? Heap.Type.CHEST : Heap.Type.HEAP;

    level.addItemToSpawn(new PotionOfInvisibility());
    
    // piranhas
    for (int i = 0; i < NUM_PIRANHAS; ++i) {
      Piranha p = new Piranha();
      do {
        p.pos = level.pointToCell(rect.random());
      } while (level.map[p.pos] != Terrain.WATER ||
              level.findMob(p.pos) != null);
      level.mobs.add(p);
    }

    return new DigResult(DigResult.Type.SPECIAL);
  }

  private DigResult digBig(Level level, XWall wall, XRect rect) {
    // big room, can be expanded
    Fill(level, rect, Terrain.EMPTY);
    Fill(level, rect.inner(1), Terrain.EMPTY_SP);
    Fill(level, rect.inner(2), Terrain.WATER);

    int ccen = level.pointToCell(rect.cen());
    Set(level, ccen, Terrain.PEDESTAL);
    level.drop(prize(level), ccen).type =
            Random.Int(3) == 0 ? Heap.Type.CHEST : Heap.Type.HEAP;

    level.addItemToSpawn(new PotionOfInvisibility());
    
    // piranhas
    for (int i = 0; i < NUM_PIRANHAS; ++i) {
      Piranha p = new Piranha();
      do {
        p.pos = level.pointToCell(rect.random(2));
      } while (level.map[p.pos] != Terrain.WATER ||
              level.findMob(p.pos) != null);
      level.mobs.add(p);
    }

    DigResult dr =
            new DigResult(DigResult.Type.SPECIAL);

    dr.walls = wallsBut(rect, -wall.direction);
    return dr;
  }

  private static Item prize(Level level) {
    Item prize;

    if (Random.Int(3) == 0) {
      prize = level.findPrizeItem();
      if (prize != null)
        return prize;
    }

    //1 floor set higher in probability, never cursed
    do {
      if (Random.Int(2) == 0) {
        prize = Generator.randomWeapon((Dungeon.depth / 5) + 1);
      } else {
        prize = Generator.randomArmor((Dungeon.depth / 5) + 1);
      }
    } while (prize.cursed);

    //33% chance for an extra update.
    if (!(prize instanceof MissileWeapon) && Random.Int(3) == 0) {
      prize.upgrade();
    }

    return prize;
  }
}
