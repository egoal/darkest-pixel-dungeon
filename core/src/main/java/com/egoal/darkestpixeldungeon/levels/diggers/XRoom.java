package com.egoal.darkestpixeldungeon.levels.diggers;

import com.watabou.utils.Point;

/**
 * Created by 93942 on 11/27/2018.
 */

public class XRoom extends XRect {
  public Point door = new Point(); // door position
  public XWall wall;
  
  public XRoom(int _x1, int _x2, int _y1, int _y2, XWall wall, Point doorpos) {
    super(_x1, _x2, _y1, _y2);

    this.wall = wall;
    door = doorpos;
  }

  public static XRoom create(int x, int y, int w, int h, XWall wall, Point dp) {
    return new XRoom(x, x + w - 1, y, y + h - 1, wall, dp);
  }
}
