package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.watabou.utils.Rect;

/**
 * Created by 93942 on 11/11/2018.
 */

public class Digger {
  public static void set(Level level, int cell, int tile){
    level.map[cell] = tile;
  }
  
  public static void set(Level level, int x, int y, int tile){
    set(level, level.xy2cell(x, y), tile);
  }
  
  public static void fill(Level level, int x, int y, int w, int h, int tile){
    for(int r=0; r<h; ++r)
      for(int c=0; c<w; ++c)
        set(level, x+c, y+r, tile);
  }
  
  public static void fill(Level level, Rect rect, int tile){
    fill(level, rect.left, rect.top, rect.width(), rect.height(), tile);
  }
}
