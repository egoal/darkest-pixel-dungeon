package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;

import java.util.ArrayList;

/**
 * Created by 93942 on 11/27/2018.
 */

public class NormalRectRoomDigger extends Digger {
  public NormalRectRoomDigger(XRoom room) {
    super(room);
  }

  @Override
  public ArrayList<XWall> dig(Level level) {
    fill(level, room, Terrain.EMPTY);
    set(level, room.door, Terrain.DOOR);
    
    ArrayList<XWall> walls = new ArrayList<>();
    if (-room.wall.direction != Digger.LEFT)
      walls.add(new XWall(room.x1-1, room.x1-1, room.y1, room.y2, Digger.LEFT));
    if(-room.wall.direction!=Digger.RIGHT)
      walls.add(new XWall(room.x2+1, room.x2+1, room.y1, room.y2, Digger.RIGHT));
    if(-room.wall.direction!=Digger.UP)
      walls.add(new XWall(room.x1, room.x2, room.y1-1, room.y1-1, Digger.UP));
    if(-room.wall.direction!=Digger.DOWN)
      walls.add(new XWall(room.x1, room.x2, room.y2+1, room.y2+1, Digger.DOWN));

    return walls;
  }
}
