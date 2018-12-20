package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/9.
 */

public class WeakFloorDigger extends RectDigger {
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(4, 8), Random.IntRange(4, 8));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    if (Random.Int(1) == 0)
      return digWellInCenter(level, wall, rect);

    return null;
  }

  public DigResult digWellInCenter(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.CHASM);
    Set(level, overlapedWall(wall, rect).random(), Terrain.DOOR);

    DigResult dr = new DigResult(DigResult.Type.WEAK_FLOOR);

    Point well = rect.cen();
    switch (wall.direction) {
      case LEFT:
        int cx = rect.x2 - rect.w() / 2 + 1;
        Fill(level, cx, rect.y1, rect.w() / 2, rect.h(), Terrain.EMPTY_SP);
        well.x = rect.x1;
        dr.walls.add(new XWall(cx, rect.x2, rect.y1 - 1, rect.y1 - 1, UP));
        dr.walls.add(new XWall(cx, rect.x2, rect.y2 + 1, rect.y2 + 1, DOWN));
        break;
      case RIGHT:
        Fill(level, rect.x1, rect.y1, rect.w() / 2, rect.h(), Terrain.EMPTY_SP);
        well.x = rect.x2;
        dr.walls.add(new XWall(rect.x1, rect.x1 + rect.w() / 2 - 1,
                rect.y1 - 1, rect.y1 - 1, UP));
        dr.walls.add(new XWall(rect.x1, rect.x1 + rect.w() / 2 - 1,
                rect.y2 + 1, rect.y2 + 1, DOWN));
        break;
      case UP:
        int cy = rect.y2 - rect.h() / 2 + 1;
        Fill(level, rect.x1, cy, rect.w(), rect.h() / 2, Terrain.EMPTY_SP);
        well.y = rect.y1;
        dr.walls.add(new XWall(rect.x1 - 1, rect.x1 - 1, cy, rect.y2, LEFT));
        dr.walls.add(new XWall(rect.x2 + 1, rect.x2 + 1, cy, rect.y2, RIGHT));
        break;
      case DOWN:
        Fill(level, rect.x1, rect.y1, rect.w(), rect.h() / 2, Terrain.EMPTY_SP);
        well.y = rect.y2;
        dr.walls.add(new XWall(rect.x1 - 1, rect.x1 - 1,
                rect.y1, rect.y1 + rect.h() / 2 - 1, Terrain.EMPTY_SP));
        break;
    }
    
    CustomTileVisual cts = new HiddenWell();
    cts.pos(well.x, well.y);
    level.customTiles.add(cts);

    return dr;
  }

  public static class HiddenWell extends CustomTileVisual {
    {
      name = Messages.get(this, "name");

      tx = Assets.WEAK_FLOOR;
      txX = Dungeon.depth / 5;
      txY = 0;
    }

    @Override
    public String desc() {
      return Messages.get(this, "desc");
    }
  }
}
