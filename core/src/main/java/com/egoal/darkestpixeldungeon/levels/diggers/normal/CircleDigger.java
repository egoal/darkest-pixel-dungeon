package com.egoal.darkestpixeldungeon.levels.diggers.normal;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult;
import com.egoal.darkestpixeldungeon.levels.diggers.XRect;
import com.egoal.darkestpixeldungeon.levels.diggers.XWall;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/17.
 */

public class CircleDigger extends RectDigger {
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(5, 9), Random.IntRange(5, 9));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY);

    int maxInner = (Math.min(rect.w(), rect.h()) - 1) / 2;
    int in = Random.NormalIntRange(1, maxInner);
    Fill(level, rect.inner(in),
            Random.Int(4) == 0 ? Terrain.CHASM : Terrain.WALL);

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
