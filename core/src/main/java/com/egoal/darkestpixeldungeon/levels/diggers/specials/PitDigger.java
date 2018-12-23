package com.egoal.darkestpixeldungeon.levels.diggers.specials;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.keys.IronKey;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult;
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger;
import com.egoal.darkestpixeldungeon.levels.diggers.XRect;
import com.egoal.darkestpixeldungeon.levels.diggers.XWall;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/8.
 */

public class PitDigger extends RectDigger {
  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY);

    Point door = overlapedWall(wall, rect).random();
    Set(level, door, Terrain.LOCKED_DOOR);

    Point well = rect.random();
    switch (wall.direction) {
      case LEFT:
        well.x = rect.x1;
        break;
      case RIGHT:
        well.x = rect.x2;
        break;
      case UP:
        well.y = rect.y1;
        break;
      case DOWN:
        well.y = rect.y2;
        break;
    }
    Set(level, well, Terrain.EMPTY_WELL);

    // items
    int remains = level.pointToCell(rect.random());
    while (level.map[remains] == Terrain.EMPTY_WELL)
      remains = level.pointToCell(rect.random());

    level.drop(new IronKey(Dungeon.depth), remains).type = Heap.Type.SKELETON;
    int loot = Random.Int(3);
    if (loot == 0)
      level.drop(Generator.random(Generator.Category.RING), remains);
    else if (loot == 1)
      level.drop(Generator.random(Generator.Category.ARTIFACT), remains);
    else
      level.drop(Generator.random(Random.oneOf(Generator.Category.WEAPON,
              Generator.Category.ARMOR)), remains);

    // extra drop
    int n = Random.IntRange(1, 2);
    for(int i=0; i<n; ++i)
      level.drop(prize(level), remains);

    return new DigResult(DigResult.Type.PIT);
  }

  private static Item prize(Level level) {
    if (Random.Int(2) == 0) {
      Item prize = level.findPrizeItem();
      if (prize != null) return prize;
    }
    
    return Generator.random(Random.oneOf(Generator.Category.POTION, 
            Generator.Category.SCROLL, Generator.Category.FOOD, 
            Generator.Category.GOLD));
  }
}
