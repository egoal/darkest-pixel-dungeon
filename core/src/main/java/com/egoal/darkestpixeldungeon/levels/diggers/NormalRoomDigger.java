package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 11/27/2018.
 */

public class NormalRoomDigger extends RectDigger {

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY);
    Point door = overlapedWall(wall, rect).random(0);
    Set(level, door, Terrain.DOOR);

    DigResult dr = new DigResult();

    dr.walls = wallsBut(rect, -wall.direction);
    
//    switch (wall.direction){
//      case LEFT:
//        dr.walls = walls(rect, LEFT, UP, DOWN);
//        break;
//      case RIGHT:
//        dr.walls = walls(rect, RIGHT, UP, DOWN);
//        break;
//      case UP:
//        dr.walls = walls(rect, UP, LEFT, RIGHT);
//        break;
//      case DOWN:
//        dr.walls = walls(rect, DOWN, LEFT, RIGHT);
//        break;
//    }

    return dr;
  }
  
}
