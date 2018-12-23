package com.egoal.darkestpixeldungeon.levels.diggers.specials;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.keys.GoldenKey;
import com.egoal.darkestpixeldungeon.items.keys.IronKey;
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
 * Created by 93942 on 2018/12/8.
 */

public class VaultDigger extends RectDigger {
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(5, 8), Random.IntRange(5, 8));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY_SP);
    Fill(level, rect.inner(1), Terrain.EMPTY);

    int dp = level.pointToCell(overlapedWall(wall, rect).random());
    Set(level, dp, Terrain.LOCKED_DOOR);
    
    int c = level.pointToCell(rect.cen());
    switch (Random.Int(3)) {
      case 0:
        level.drop(Prize(level), c).type = Heap.Type.LOCKED_CHEST;
        level.addItemToSpawn(new GoldenKey(Dungeon.depth));
        break;
      case 1:
        // ensure two different categories
        Generator.Category c1 = Random.oneOf(Generator.Category.WAND,
                Generator.Category.RING, Generator.Category.ARTIFACT);
        Generator.Category c2;
        do {
          c2 = Random.oneOf(Generator.Category.WAND,
                  Generator.Category.RING, Generator.Category.ARTIFACT);
        } while (c1 == c2);

        Item i1 = Generator.random(c1);
        Item i2 = Generator.random(c2);

        level.drop(i1, c).type = Heap.Type.CRYSTAL_CHEST;
        level.drop(i2, c + PathFinder.NEIGHBOURS8[Random.Int(8)]).type =
                Heap.Type.CRYSTAL_CHEST;
        level.addItemToSpawn(new GoldenKey(Dungeon.depth));
        break;

      case 2:
        level.drop(Prize(level), c);
        Set(level, c, Terrain.PEDESTAL);
        break;
    }
    
    level.addItemToSpawn(new IronKey(Dungeon.depth));

    return new DigResult(DigResult.Type.LOCKED);
  }

  private static Item Prize(Level level) {
    return Generator.random(Random.oneOf(
            Generator.Category.WAND,
            Generator.Category.RING,
            Generator.Category.ARTIFACT
    ));
  }
}
