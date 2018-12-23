package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 11/27/2018.
 */

public class NormalRectDigger extends RectDigger {

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY);

    if (Random.Int(8) == 0) {
      // some times no door, dig the whole wall 
      Fill(level, overlapedWall(wall, rect), Terrain.EMPTY);
    } else {
      Point door = overlapedWall(wall, rect).random();
      Set(level, door, Terrain.DOOR);
    }
    
    DigResult dr = new DigResult();

    dr.walls = wallsBut(rect, -wall.direction);

    return dr;
  }
}
