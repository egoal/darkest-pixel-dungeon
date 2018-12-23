package com.egoal.darkestpixeldungeon.levels.diggers.specials;

import com.egoal.darkestpixeldungeon.Challenges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.Foliage;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult;
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger;
import com.egoal.darkestpixeldungeon.levels.diggers.XRect;
import com.egoal.darkestpixeldungeon.levels.diggers.XWall;
import com.egoal.darkestpixeldungeon.plants.BlandfruitBush;
import com.egoal.darkestpixeldungeon.plants.Sungrass;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 12/4/2018.
 */

public class GardenDigger extends RectDigger {
  @Override
  protected Point chooseRoomSize(XWall wall) {
    int size = Random.IntRange(3, 6);
    return new Point(size, size);
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    int circles = (rect.w() - 1) / 2;
    for (int i = 0; i <= circles; ++i)
      Fill(level, rect.inner(i), i % 2 == 0 ? Terrain.HIGH_GRASS : Terrain
              .GRASS);

    Point door = overlapedWall(wall, rect).random(0);
    Set(level, door, Terrain.DOOR);

    if (Dungeon.isChallenged(Challenges.NO_FOOD)) {
      if (Random.Int(2) == 0)
        level.plant(new Sungrass.Seed(), level.pointToCell(rect.random(0)));
    } else {
      int bushes = Random.Int(3);
      if (bushes == 0)
        level.plant(new Sungrass.Seed(), level.pointToCell(rect.random(0)));
      else if (bushes == 1)
        level.plant(new BlandfruitBush.Seed(), level.pointToCell(rect.random
                (0)));
      else if (Random.Int(5) == 0) {
        // both
        int p1, p2;
        p1 = level.pointToCell(rect.random(0));
        do {
          p2 = level.pointToCell(rect.random(0));
        } while (p1 == p2);

        level.plant(new Sungrass.Seed(), p1);
        level.plant(new BlandfruitBush.Seed(), p2);
      }
    }

    Foliage light = (Foliage) level.blobs.get(Foliage.class);
    if (light == null)
      light = new Foliage();

    for (Point p : rect.getAllPoints())
      light.seed(level, level.pointToCell(p), 1);
    level.blobs.put(Foliage.class, light);

    // now, the garden is open!
    DigResult dr = new DigResult();
    dr.type = DigResult.Type.SPECIAL;
    dr.walls = wallsBut(rect, -wall.direction);
    
    return dr;
  }
}
