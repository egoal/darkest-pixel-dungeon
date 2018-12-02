package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 12/2/2018.
 */

public class RectRoomDigger extends Digger {
  protected Point door = new Point();
  protected Point size = new Point();
  
  void set(Point door, Point size){
    this.door = door;
    this.size = size;
  }
  
  @Override
  protected void createPattern() {
    int x = -1;
    int y = -1;
    int w = size.x;
    int h = size.y;
    switch (wall.direction) {
      case LEFT:
        x = wall.x1 - w;
        y = Random.IntRange(door.y - h + 1, door.y);
        break;
      case RIGHT:
        x = wall.x2 + 1;
        y = Random.IntRange(door.y - h + 1, door.y);
        break;
      case UP:
        x = Random.IntRange(door.x - w + 1, door.x);
        y = wall.y1 - h;
        break;
      case DOWN:
        x = Random.IntRange(door.x - w + 1, door.x);
        y = wall.y2 + 1;
        break;
    }

    pattern = new DigPattern(x, x + w - 1, y, y + h - 1);
  }

  @Override
  protected void digWall(Level level) {
    Set(level, door, Terrain.DOOR);
  }

  @Override
  protected ArrayList<XWall> newDigableWalls() {
    // 4 walls
    ArrayList<XWall> walls = new ArrayList<>();
    if (-wall.direction != LEFT)
      walls.add(new XWall(pattern.x1 - 1, pattern.x1 - 1,
              pattern.y1, pattern.y2, LEFT));
    if (-wall.direction != RIGHT)
      walls.add(new XWall(pattern.x2 + 1, pattern.x2 + 1,
              pattern.y1, pattern.y2, RIGHT));
    if (-wall.direction != UP)
      walls.add(new XWall(pattern.x1, pattern.x2,
              pattern.y1 - 1, pattern.y1 - 1, UP));
    if (-wall.direction != DOWN)
      walls.add(new XWall(pattern.x1, pattern.x2,
              pattern.y2 + 1, pattern.y2 + 1, DOWN));

    return walls;
  }
}
