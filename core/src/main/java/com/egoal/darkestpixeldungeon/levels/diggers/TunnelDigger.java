package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 11/29/2018.
 */

public class TunnelDigger extends RectDigger {

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Point idp = overlapedWall(wall, rect).random(0);
    Point cdp = rect.cen();
    Point odp = new Point();
    
    switch (wall.direction) {
      case LEFT:
        odp.x = rect.x1 - 1;
        odp.y = Random.IntRange(rect.y1, rect.y2);
        break;
      case RIGHT:
        odp.x = rect.x2 + 1;
        odp.y = Random.IntRange(rect.y1, rect.y2);
        break;
      case UP:
        odp.x = Random.IntRange(rect.x1, rect.x2);
        odp.y = rect.y1 - 1;
        break;
      case DOWN:
        odp.x = Random.IntRange(rect.x1, rect.x2);
        odp.y = rect.y2 + 1;
        break;
    }
    
    int tunnelTile = Terrain.EMPTY_SP;
    if(wall.direction==LEFT || wall.direction==RIGHT) {
      LinkH(level, idp.y, idp.x, cdp.x, tunnelTile);
      LinkV(level, cdp.x, idp.y, odp.y, tunnelTile);
      LinkH(level, odp.y, cdp.x, odp.x, tunnelTile);
    }else{
      LinkV(level, idp.x, idp.y, cdp.y, tunnelTile);
      LinkH(level, cdp.y, idp.x, odp.x, tunnelTile);
      LinkV(level, odp.x, cdp.y, odp.y, tunnelTile);
    }
    Set(level, idp, Terrain.DOOR);
    Set(level, odp, Terrain.WALL);
    
    DigResult dr = new DigResult();
    dr.type = DigResult.Type.TUNNEL;
    dr.walls.add(new XWall(odp.x, odp.y, wall.direction));

    return dr;
  }

  protected Point chooseRoomSize(XWall wall) {
    int w = Random.HighIntRange(3, 8);
    int h = Random.LowIntRange(3, 8);

    return Random.Int(2) == 0 ? new Point(w, h) : new Point(h, w);
  }
}
