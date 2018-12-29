package com.egoal.darkestpixeldungeon.levels.diggers.unordinary;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.items.quest.CeremonialCandle;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult;
import com.egoal.darkestpixeldungeon.levels.diggers.Digger;
import com.egoal.darkestpixeldungeon.levels.diggers.XRect;
import com.egoal.darkestpixeldungeon.levels.diggers.XWall;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/18.
 */

public class RitualSiteDigger extends Digger {
  @Override
  public XRect chooseDigArea(XWall wall) {
    return chooseCenteredBox(wall, Random.IntRange(3, 5));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Point cen = rect.cen();
    int hs = rect.w() / 2;
    int hs2 = hs * hs;
    for (Point p : rect.getAllPoints())
      if (Point.DistanceL22(cen, p) <= hs2)
        Set(level, p, Terrain.EMPTY);

    RitualMarker rm = new RitualMarker();
    rm.pos(cen.x - 1, cen.y - 1);
    level.customTiles.add(rm);

    int ccen = level.pointToCell(cen);
    for (int i : PathFinder.NEIGHBOURS9)
      Set(level, ccen + i, Terrain.EMPTY_DECO);

    for (int i = 0; i < 4; ++i)
      level.addItemToSpawn(new CeremonialCandle());

    CeremonialCandle.ritualPos = ccen;

    //
    Point door = rect.cen();
    if (wall.direction == LEFT || wall.direction == RIGHT)
      door.x = wall.x1;
    else
      door.y = wall.y1;
    Set(level, door, Terrain.DOOR);

    DigResult dr = new DigResult(DigResult.Type.SPECIAL);

    if (-wall.direction != LEFT)
      dr.walls.add(new XWall(rect.x1 - 1, cen.y, LEFT));
    if (-wall.direction != RIGHT)
      dr.walls.add(new XWall(rect.x2 + 1, cen.y, RIGHT));
    if (-wall.direction != UP)
      dr.walls.add(new XWall(cen.x, rect.y1 - 1, UP));
    if (-wall.direction != DOWN)
      dr.walls.add(new XWall(cen.x, rect.y2 + 1, DOWN));

    return dr;
  }

  public static class RitualMarker extends CustomTileVisual {

    {
      name = Messages.get(this, "name");

      tx = Assets.PRISON_QUEST;
      txX = txY = 0;
      tileW = tileH = 3;
    }

    @Override
    public String desc() {
      return Messages.get(this, "desc");
    }
  }
}