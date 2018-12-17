package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/17.
 */

public class LatticeDigger extends RectDigger {
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(1, 4) * 2 + 1,
            Random.IntRange(1, 4) * 2 + 1);
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY);

    int tile = Random.Int(4) == 0 ? Terrain.CHASM : Terrain.WALL;

    for (int r = 1; r < rect.h(); r += 2)
      for (int c = 1; c < rect.w(); c += 2)
        Set(level, rect.x1 + c, rect.y1 + r, tile);

    if (Random.Int(8) == 0) {
      // some times no door, dig the whole wall 
      Fill(level, overlapedWall(wall, rect), Terrain.EMPTY);
    } else {
      Point door = overlapedWall(wall, rect).random(0);
      Set(level, door, Terrain.DOOR);
    }

    DigResult dr = new DigResult();

    dr.walls = wallsBut(rect, -wall.direction);

    return dr;
  }
}
