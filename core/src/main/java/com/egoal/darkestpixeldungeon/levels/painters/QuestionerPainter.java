package com.egoal.darkestpixeldungeon.levels.painters;

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Questioner;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Room;
import com.egoal.darkestpixeldungeon.levels.Terrain;

/**
 * Created by 93942 on 10/14/2018.
 */

public class QuestionerPainter extends Painter {
  
  public static void paint(Level level, Room room){
    fill(level, room, Terrain.WALL);
    fill(level, room, 1, Terrain.EMPTY);
    
    set(level, room.entrance(), Terrain.WALL);
    room.entrance().set(Room.Door.Type.USER_DEFINED);

    Questioner q = new Questioner().random().hold(room);
    q.pos = level.pointToCell(room.entrance());
    level.mobs.add(q);
  }
  
}
