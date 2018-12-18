package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.actors.mobs.RotHeart;
import com.egoal.darkestpixeldungeon.actors.mobs.RotLasher;
import com.egoal.darkestpixeldungeon.items.keys.IronKey;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/18.
 */

public class RotGardenDigger extends RectDigger {
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(5, 9), Random.IntRange(5, 9));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {

    Fill(level, rect, Terrain.GRASS);

    Point door = overlapedWall(wall, rect).random();
    Set(level, door, Terrain.LOCKED_DOOR);
    level.addItemToSpawn(new IronKey(Dungeon.depth));

    Point hp = rect.random();
    switch (wall.direction) {
      case LEFT:
        hp.x = rect.x1;
        break;
      case RIGHT:
        hp.x = rect.x2;
        break;
      case UP:
        hp.y = rect.y1;
        break;
      case DOWN:
        hp.y = rect.y2;
        break;
    }

    int hpc = level.pointToCell(hp);

    PlacePlant(level, hpc, new RotHeart());

    int lashers = rect.w() * rect.h() / 8;
    for (int i = 0; i < lashers; ++i) {
      int pos;
      do {
        pos = level.pointToCell(rect.random());
      } while (!ValidPlantPos(level, pos));
      PlacePlant(level, pos, new RotLasher());
    }

    return new DigResult(DigResult.Type.LOCKED);
  }

  private static boolean ValidPlantPos(Level level, int pos) {
    if (level.map[pos] != Terrain.GRASS) return false;

    for (int i : PathFinder.NEIGHBOURS9)
      if (level.findMob(pos + i) != null)
        return false;

    return true;
  }

  private static void PlacePlant(Level level, int pos, Mob plant) {
    plant.pos = pos;
    level.mobs.add(plant);

    for (int i : PathFinder.NEIGHBOURS8)
      if (level.map[pos + i] == Terrain.GRASS)
        Set(level, pos + i, Terrain.HIGH_GRASS);
  }
}
