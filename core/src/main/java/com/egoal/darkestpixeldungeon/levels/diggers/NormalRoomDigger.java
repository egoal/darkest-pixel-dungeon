package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 11/27/2018.
 */

public class NormalRoomDigger extends RectRoomDigger {
  @Override
  protected void createPattern() {
     set(wall.random(0), new Point(Random.IntRange(3, 8), Random.IntRange(3, 8)));
     
     super.createPattern();
     
     pattern.fill(Terrain.EMPTY);
  }
  
}
