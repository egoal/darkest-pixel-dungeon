package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 11/29/2018.
 */

public class TunnelDigger extends Digger {

  @Override
  public ArrayList<XWall> dig(Level level, XRoom room) {
    ArrayList<XWall> walls = new ArrayList<>();

    if (-room.wall.direction != Digger.LEFT) {
      Point newdoor = new Point(room.x1, Random.IntRange(room.y1, room.y2));
      RandomLink(level, room.door, newdoor, Terrain.EMPTY);

      XWall wall = new XWall(newdoor.x-1, newdoor.y, Digger.LEFT);
      walls.add(wall);
    }

    if (-room.wall.direction != Digger.RIGHT) {
      Point newdoor = new Point(room.x2, Random.IntRange(room.y1, room.y2));
      RandomLink(level, room.door, newdoor, Terrain.EMPTY);
      
      walls.add(new XWall(newdoor.x+1, newdoor.y, Digger.RIGHT));
    }
    
    if(-room.wall.direction!=Digger.UP){
      Point newdoor = new Point(Random.IntRange(room.x1, room.x2), room.y1);
      RandomLink(level, room.door, newdoor, Terrain.EMPTY);
      
      walls.add(new XWall(newdoor.x, newdoor.y-1, Digger.UP));
    }

    if(-room.wall.direction!=Digger.DOWN){
      Point newdoor = new Point(Random.IntRange(room.x1, room.x2), room.y2);
      RandomLink(level, room.door, newdoor, Terrain.EMPTY);

      walls.add(new XWall(newdoor.x, newdoor.y+1, Digger.DOWN));
    }
    
    Set(level, room.door, room.wall.isRoomWall ? Terrain.DOOR : Terrain.EMPTY);

    for (XWall w : walls)
      w.isRoomWall = false;
    
    return walls;
  }

//  @Override
//  protected Point desireDigSize() {
//    int len = Random.IntRange(2, 5);
//    return Random.Int(2)==0? new Point(len, 1): new Point(1, len);
//  }
}
