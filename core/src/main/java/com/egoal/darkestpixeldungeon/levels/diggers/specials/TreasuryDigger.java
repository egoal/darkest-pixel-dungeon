package com.egoal.darkestpixeldungeon.levels.diggers.specials;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Gold;
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

public class TreasuryDigger extends RectDigger {
  
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(4, 6), Random.IntRange(4, 6));
  }


  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY);
    Set(level, rect.cen(), Terrain.STATUE);

    Set(level, overlapedWall(wall, rect).random(), Terrain.LOCKED_DOOR);

    Heap.Type ht = Random.Int(2) == 0 ? Heap.Type.CHEST : Heap.Type.HEAP;
    int n = Random.IntRange(2, 3);
    for (int i = 0; i < n; ++i) {
      int pos;
      do {
        pos = level.pointToCell(rect.random());
      } while (level.map[pos] != Terrain.EMPTY || level.heaps.get(pos) != null);
      level.drop(Prize(level), pos).type = (ht == Heap.Type.CHEST &&
              Random.Int(10) == 0) ? Heap.Type.MIMIC : ht;
    }

    if (ht == Heap.Type.HEAP) {
      // some little gold
      for (int i = 0; i < 6; ++i) {
        int pos;
        do {
          pos = level.pointToCell(rect.random());
        } while (level.map[pos] != Terrain.EMPTY);
        level.drop(new Gold(Random.IntRange(5, 20)), pos);
      }
    }

    level.addItemToSpawn(new IronKey(Dungeon.depth));

    return new DigResult(DigResult.Type.LOCKED);
  }

  private static Item Prize(Level level) {
    return new Gold().random();
  }

}
