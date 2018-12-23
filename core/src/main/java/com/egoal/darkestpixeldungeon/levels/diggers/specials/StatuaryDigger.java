package com.egoal.darkestpixeldungeon.levels.diggers.specials;

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Statuary;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult;
import com.egoal.darkestpixeldungeon.levels.diggers.XRect;
import com.egoal.darkestpixeldungeon.levels.diggers.XWall;
import com.egoal.darkestpixeldungeon.levels.diggers.normal.DiamondDigger;
import com.watabou.utils.PathFinder;

/**
 * Created by 93942 on 2018/12/5.
 */

public class StatuaryDigger extends DiamondDigger {
  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    DigResult dr = super.dig(level, wall, rect);

    int ccen = level.pointToCell(rect.cen());
    for (int i : PathFinder.NEIGHBOURS9)
      Set(level, ccen + i, Terrain.EMPTY_SP);

    Statuary s = new Statuary().random();
    s.pos = ccen;
    level.mobs.add(s);

    return dr;
  }
}
