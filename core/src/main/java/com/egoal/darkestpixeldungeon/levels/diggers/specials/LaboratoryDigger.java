package com.egoal.darkestpixeldungeon.levels.diggers.specials;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.Alchemy;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.keys.IronKey;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult;
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger;
import com.egoal.darkestpixeldungeon.levels.diggers.XRect;
import com.egoal.darkestpixeldungeon.levels.diggers.XWall;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 12/4/2018.
 */

public class LaboratoryDigger extends RectDigger {
  @Override
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(4, 6), Random.IntRange(4, 6));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY_SP);

    // lock the door
    Point door = overlapedWall(wall, rect).random(0);
    Set(level, door, Terrain.LOCKED_DOOR);
    level.addItemToSpawn(new IronKey(Dungeon.depth));
    
    Point pA = rect.cen();
    switch (wall.direction){
      case LEFT:
        pA.x = rect.x1;
        break;
      case RIGHT:
        pA.x = rect.x2;
        break;
      case UP:
        pA.y = rect.y1;
        break;
      case DOWN:
        pA.y = rect.y2;
        break;
    }
    int cA = level.pointToCell(pA);
    int cE;
    do{
      cE = PathFinder.NEIGHBOURS4[Random.Int(4)] + cA;
    }while(level.map[cE]==Terrain.WALL);

    // alchemy
    Set(level, cA, Terrain.ALCHEMY);
    Alchemy a = new Alchemy();
    a.seed(level, cA, 1);
    level.blobs.put(Alchemy.class, a);

    // enchanting station
    Set(level, cE, Terrain.ENCHANTING_STATION);

    // two potions, perhaps some day, i should add some enchanting stone
    int n = Random.IntRange(2, 3);
    for (int i = 0; i < n; ++i) {
      int pos;
      do {
        pos = level.pointToCell(rect.random());
      }
      while (level.map[pos] != Terrain.EMPTY_SP || level.heaps.get(pos) != 
              null);

      level.drop(prize(level), pos);
    }

    return new DigResult(DigResult.Type.LOCKED);
  }

  private static Item prize(Level level) {
    Item prize = level.findPrizeItem(Potion.class);
    if (prize == null)
      prize = Generator.random(Generator.Category.POTION);
    
    return prize;
  }
}
