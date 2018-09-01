package com.egoal.darkestpixeldungeon.levels.painters;

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Statuary;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Room;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;

/**
 * Created by 93942 on 6/2/2018.
 */

public class StatuaryPainter extends Painter {

  public static void paint(Level level, Room room) {
    fill(level, room, Terrain.WALL);
    fill(level, room, 1, Terrain.EMPTY);

    Point c = room.center();

    // no need to lock
    room.entrance().set(Room.Door.Type.REGULAR);

    Statuary s = new Statuary().random();
    s.pos = c.x + c.y * level.width();
    level.mobs.add(s);
  }
}
