package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 11/27/2018.
 */

public class NormalRoomDigger extends Digger {

  @Override
  public ArrayList<XWall> dig(Level level, XRoom room) {
    Fill(level, room, Terrain.EMPTY);
    Set(level, room.door, Terrain.DOOR);

    ArrayList<XWall> walls = new ArrayList<>();
    if (-room.wall.direction != Digger.LEFT)
      walls.add(new XWall(room.x1 - 1, room.x1 - 1, room.y1, room.y2, Digger
              .LEFT));
    if (-room.wall.direction != Digger.RIGHT)
      walls.add(new XWall(room.x2 + 1, room.x2 + 1, room.y1, room.y2, Digger
              .RIGHT));
    if (-room.wall.direction != Digger.UP)
      walls.add(new XWall(room.x1, room.x2, room.y1 - 1, room.y1 - 1, Digger
              .UP));
    if (-room.wall.direction != Digger.DOWN)
      walls.add(new XWall(room.x1, room.x2, room.y2 + 1, room.y2 + 1, Digger
              .DOWN));

    return walls;
  }

  @Override
  protected Point desireDigSize() {
    return new Point(Random.IntRange(3, 6), Random.IntRange(3, 6));
  }
}
