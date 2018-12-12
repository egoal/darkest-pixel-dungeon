package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Bomb;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.keys.IronKey;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/12.
 */

public class ArmoryDigger extends RectDigger {
  @Override
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(4, 8), Random.IntRange(4, 8));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY);
    Point door = overlapedWall(wall, rect).random();
    Set(level, door, Terrain.LOCKED_DOOR);

    Set(level, rect.random(1), Terrain.STATUE);

    int n = Random.IntRange(1, 2);
    for (int i = 0; i < n; ++i) {
      int pos;
      do {
        pos = level.pointToCell(rect.random());
      } while (level.map[pos] != Terrain.EMPTY || level.heaps.get(pos) != null);
      level.drop(Prize(level), pos);
    }

    level.addItemToSpawn(new IronKey(Dungeon.depth));

    return new DigResult(DigResult.Type.LOCKED);
  }

  private static Item Prize(Level level) {
    return Generator.random(Random.oneOf(Generator.Category.ARMOR,
            Generator.Category.WEAPON));
  }
}
