package com.egoal.darkestpixeldungeon.levels.diggers;

/**
 * Created by 93942 on 11/27/2018.
 */

public class XWall extends XRect{
  public int direction;
  public boolean isRoomWall; // the wall is a room's wall
  
  public XWall(int x1, int x2, int y1, int y2, int direction, boolean roomWall){
    super(x1, x2, y1, y2);
    this.direction = direction;
    isRoomWall = roomWall;
  }

  public XWall(int x1, int x2, int y1, int y2, int direction){
    this(x1, x2, y1, y2, direction, true);
  }
  
}
