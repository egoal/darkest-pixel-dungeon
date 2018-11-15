package com.egoal.darkestpixeldungeon.levels;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.levels.diggers.Digger;
import com.egoal.darkestpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

import java.lang.annotation.Inherited;
import java.util.Arrays;

/**
 * Created by 93942 on 11/11/2018.
 */

public class DPDTestLevel extends Level {
  {
    color1 = 0x48763c;
    color2 = 0x59994a;
    viewDistance = 8;
    seeDistance = 8;
  }

  @Override
  public String tilesTex() {
    return Assets.TILES_SEWERS;
  }

  @Override
  public String waterTex() {
    return Assets.WATER_SEWERS;
  }

  @Override
  protected void setupSize() {
    if (width == 0 && height == 0)
      width = height = 36;

    length = width * height;
  }

  @Override
  protected boolean build() {
    digInitRoom();

    entrance = xy2cell(width / 2, height / 2);
    map[entrance] = Terrain.ENTRANCE;
    exit = xy2cell(width / 2, height / 2 + 4);
    map[exit] = Terrain.EXIT;

    return true;
  }

  @Override
  protected void decorate() {
  }


  @Override
  protected void createMobs() {
  }

  @Override
  protected void createItems() {
  }

  //////////////////////////////////////////////////////////////////////////////
  private void digInitRoom() {
    int w = Random.Int(4, 8);
    int h = Random.Int(4, 8);
    int x = Random.Int(10, width - w - 10);
    int y = Random.Int(10, height - h - 10);

    Rect r = new Rect(x, y, x+w, y+h);
    
    Digger.fill(this, r, Terrain.EMPTY);
    // entrance = 
  }
}
