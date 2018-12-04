package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 12/2/2018.
 */

public abstract class RectDigger extends Digger {
  @Override
  public XRect chooseDigArea(XWall wall) {
    Point size = chooseRoomSize(wall);
    int x = -1;
    int y = -1;
    switch (wall.direction) {
      case LEFT:
        x = wall.x1 - size.x;
        y = Random.IntRange(wall.y1 - size.y + 1, wall.y1);
        break;
      case RIGHT:
        x = wall.x2 + 1;
        y = Random.IntRange(wall.y1 - size.y + 1, wall.y1);
        break;
      case UP:
        x = Random.IntRange(wall.x1 - size.x + 1, wall.x1);
        y = wall.y1 - size.y;
        break;
      case DOWN:
        x = Random.IntRange(wall.x1 - size.x + 1, wall.x1);
        y = wall.y2 + 1;
        break;
    }

    return XRect.create(x, y, size.x, size.y);
  }

  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(3, 6), Random.IntRange(3, 6));
  }

  protected ArrayList<XWall> walls(XRect rect, int... directions) {
    ArrayList<XWall> walls = new ArrayList<>();
    for (int dir : directions)
      switch (dir) {
        case LEFT:
          walls.add(new XWall(rect.x1 - 1, rect.x1 - 1, rect.y1, rect.y2, 
                  LEFT));
        case RIGHT:
          walls.add(new XWall(rect.x2 + 1, rect.x2 + 1, rect.y1, rect.y2, 
                  RIGHT));
        case UP:
          walls.add(new XWall(rect.x1, rect.x2, rect.y1 - 1, rect.y1 - 1, UP));
        case DOWN:
          walls.add(new XWall(rect.x1, rect.x2, rect.y2 + 1, rect.y2 + 1, 
                  DOWN));
      }

    return walls;
  }

  protected ArrayList<XWall> wallsBut(XRect rect, int direction) {
    //!!! not a good api, can be out of range, but, just in handy
    int[] directions = new int[3];
    int i = 0;
    for (int d : Directions)
      if (d != direction)
        directions[i++] = d;
    
    return walls(rect, directions);
  }

  // assits
  protected XRect overlapedWall(XWall wall, XRect rect) {
    switch (wall.direction) {
      case LEFT:
      case RIGHT:
        return new XRect(wall.x1, wall.x2, Math.max(wall.y1, rect.y1),
                Math.min(wall.y2, rect.y2));
      case UP:
      case DOWN:
        return new XRect(Math.max(wall.x1, rect.x1), Math.min(wall.x2, rect.x2),
                wall.y1, wall.y2);
      default:
        // can never be here!!!
        return null;
    }
  }

}
