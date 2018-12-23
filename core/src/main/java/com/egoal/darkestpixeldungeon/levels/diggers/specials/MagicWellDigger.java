package com.egoal.darkestpixeldungeon.levels.diggers.specials;

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfAwareness;
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfHealth;
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfTransmutation;
import com.egoal.darkestpixeldungeon.actors.blobs.WellWater;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult;
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RoundDigger;
import com.egoal.darkestpixeldungeon.levels.diggers.XRect;
import com.egoal.darkestpixeldungeon.levels.diggers.XWall;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/12.
 */

public class MagicWellDigger extends RoundDigger {
  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    DigResult dr = super.dig(level, wall, rect);

    int ccen = level.pointToCell(rect.cen());
    for (int i : PathFinder.NEIGHBOURS9)
      if (Random.Int(2) == 0)
        Set(level, ccen + i, Terrain.GRASS);

    Set(level, ccen, Terrain.WELL);

    // well
    Class<? extends WellWater> cls = (Class<? extends WellWater>) Random
            .element(WATERS);

    WellWater ww = (WellWater) level.blobs.get(cls);
    if (ww == null) {
      try {
        ww = cls.newInstance();
      } catch (Exception e) {
        DarkestPixelDungeon.reportException(e);
        return null;
      }
    }
    ww.seed(level, ccen, 1);
    level.blobs.put(cls, ww);
    
    return dr;
  }

  private static final Class<?>[] WATERS = {WaterOfAwareness.class,
          WaterOfHealth.class, WaterOfTransmutation.class};

}
