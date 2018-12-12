package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.mobs.Statue;
import com.egoal.darkestpixeldungeon.items.keys.IronKey;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/12.
 */

public class StatueDigger extends RectDigger {

  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(4, 8), Random.IntRange(4, 8));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {

    Fill(level, rect, Terrain.EMPTY);

    int lastRowTile = Terrain.STATUE;
    Point plat = rect.cen();
    switch (wall.direction) {
      case LEFT:
        LinkV(level, rect.x1, rect.y1, rect.y2, lastRowTile);
        plat.x = rect.x1 + 1;
        break;
      case RIGHT:
        LinkV(level, rect.x2, rect.y1, rect.y2, lastRowTile);
        plat.x = rect.x2 - 1;
        break;
      case UP:
        LinkH(level, rect.y1, rect.x1, rect.x2, lastRowTile);
        plat.y = rect.y1 + 1;
        break;
      case DOWN:
        LinkH(level, rect.y2, rect.x1, rect.x2, lastRowTile);
        plat.y = rect.y2 - 1;
        break;
    }

    Point door = overlapedWall(wall, rect).random();
    Set(level, door, Terrain.LOCKED_DOOR);

    Statue s = new Statue();
    s.pos = level.pointToCell(plat);
    level.mobs.add(s);

    level.addItemToSpawn(new IronKey(Dungeon.depth));

    return new DigResult(DigResult.Type.LOCKED);
  }
}
