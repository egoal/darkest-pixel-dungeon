package com.egoal.darkestpixeldungeon.levels.diggers.specials;

import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Honeypot;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLiquidFlame;
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

public class StorageDigger extends RectDigger {

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY_SP);

    Point door = overlapedWall(wall, rect).random();
    Set(level, door, Terrain.BARRICADE);

    boolean hp = Random.Int(2) == 0;
    if (hp) {
      int pos;
      do {
        pos = level.pointToCell(rect.random());
      } while (level.map[pos] != Terrain.EMPTY_SP);
      level.drop(new Honeypot(), pos);
    }

    int n = Random.IntRange(3, 4);
    if (hp) --n;
    for (int i = 0; i < n; ++i) {
      int pos;
      do {
        pos = level.pointToCell(rect.random());
      } while (level.map[pos] != Terrain.EMPTY_SP);
      level.drop(Prize(level), pos);
    }

    level.addItemToSpawn(new PotionOfLiquidFlame());

    return new DigResult(DigResult.Type.LOCKED);
  }

  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(4, 6), Random.IntRange(4, 6));
  }

  private static Item Prize(Level level) {

    if (Random.Int(2) != 0) {
      Item prize = level.findPrizeItem();
      if (prize != null)
        return prize;
    }

    return Generator.random(Random.oneOf(
            Generator.Category.POTION,
            Generator.Category.SCROLL,
            Generator.Category.FOOD,
            Generator.Category.GOLD
    ));
  }
}
