package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;

import java.util.ArrayList;

/**
 * Created by 93942 on 11/27/2018.
 */

public class CorridorDigger extends Digger {
  public static final int CORRIDOR = 10000;

  public CorridorDigger(XRoom room) {
    super(room);
  }

  @Override
  public ArrayList<XWall> dig(Level level) {
    fill(level, room, CORRIDOR);
    set(level, room.door, room.wall.isRoomWall? Terrain.DOOR: Terrain.EMPTY);
    
    ArrayList<XWall> walls = new ArrayList<>();
    if (-room.wall.direction != Digger.LEFT) {
      XWall newWall = new XWall(room.x1 - 1, room.x1 - 1, room.y1, room.y2,
              Digger.LEFT, false);
      if (isAll(level, newWall, Terrain.WALL))
        walls.add(newWall);
    }
    if (-room.wall.direction != Digger.RIGHT) {
      XWall newWall = new XWall(room.x2 + 1, room.x2 + 1, room.y1, room.y2,
              Digger.RIGHT, false);
      if (isAll(level, newWall, Terrain.WALL))
        walls.add(newWall);
    }

    if (-room.wall.direction != Digger.UP) {
      XWall newWall = new XWall(room.x1, room.x2, room.y1 - 1, room.y1 - 1, 
              Digger.UP, false);
      if (isAll(level, newWall, Terrain.WALL))
        walls.add(newWall);
    }
    if (-room.wall.direction != Digger.DOWN) {
      XWall newWall = new XWall(room.x1, room.x2, room.y2 + 1, room.y2 + 1, 
              Digger.DOWN, false);
      if (isAll(level, newWall, Terrain.WALL))
        walls.add(newWall);
    }

    return walls;
  }
}
