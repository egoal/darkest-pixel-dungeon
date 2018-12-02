package com.egoal.darkestpixeldungeon.levels;

import android.util.Log;
import android.util.Pair;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.levels.diggers.*;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

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
    // dig rooms
    {
      digFirstRoom();
      
      int roomsToDig = 20;
      for(int i=0; i<1000; ++i) {
        if(digableWalls.isEmpty())
          break;
        XWall wall = Random.element(digableWalls);
        if(digAt(new NormalRoomDigger().wall(wall))){
          digableWalls.remove(wall);
          
          if(--roomsToDig==0)
            break;
        }
      }
      Log.d("dpd", String.format("%d rooms digged.", 20-roomsToDig));
      
      int lc = makeLoopClosure(6);
      Log.d("dpd", String.format("%d loop closures made.", lc));
    }
    
    // place entrance and exit
    ArrayList<DigPattern> normalPatterns = new ArrayList<>();
    for (DigPattern pattern : diggedPatterns) {
      if (pattern.type == DigPattern.Type.NORMAL)
        normalPatterns.add(pattern);
    }
    Log.d("dpd", String.format("%d/%d normal patterns.", 
            normalPatterns.size(), diggedPatterns.size()));
    
    DigPattern patternEnter = Random.element(normalPatterns);
    DigPattern patternExit = null;
    for(int i=0; i<100; ++i){
      patternExit = Random.element(normalPatterns);
      if(patternEnter!=patternExit)
        break;
    }
    
    entrance = pointToCell(patternEnter.random(1));
    map[entrance] = Terrain.ENTRANCE;
    exit = pointToCell(patternExit.random(1));
    map[exit] = Terrain.EXIT;

    // do some painting

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
  // my digging algorithm
  ArrayList<XWall> digableWalls = new ArrayList<>();
  ArrayList<DigPattern> diggedPatterns = new ArrayList<>();

  private void digFirstRoom() {
    int w = Random.IntRange(3, 6);
    int h = Random.IntRange(3, 6);
    int x = Random.IntRange(width / 4, width / 4 * 3 - w);
    int y = Random.IntRange(height / 4, height / 4 * 3 - h);

    digAt(new InitRoomDigger().wall(
            new XWall(x, x + w - 1, y, y + h - 1, Digger.NONE)));
  }

  private boolean digAt(Digger digger) {
    if (!canDigAt(digger.desireDigSpace()))
      return false;

    Digger.DigResult dr = digger.dig(this);

    digableWalls.addAll(dr.walls);
    diggedPatterns.add(dr.pattern);

    return true;
  }

  private boolean canDigAt(XRect rect) {
    if (rect.x1 > 0 && rect.x2 < width - 1 && rect.y1 > 0 && rect.y2 < height
            - 1) {
      for (int x = rect.x1 - 1; x <= rect.x2 + 1; ++x)
        for (int y = rect.y1 - 1; y <= rect.y2 + 1; ++y)
          if (map[xy2cell(x, y)] != Terrain.WALL)
            return false;

      return true;
    }

    return false;
  }

  private int makeLoopClosure(int maxLoops) {
    int loops = 0;

    // simply dig a door when there's overlapped wall
    ArrayList<Pair<XWall, XWall>> overlappedWalls = new ArrayList<>();

    int cntWalls = digableWalls.size();
    for (int i = 0; i < cntWalls; ++i) {
      XWall wi = digableWalls.get(i);
      for (int j = i + 1; j < cntWalls; ++j) {
        XWall wj = digableWalls.get(j);

        if (wi.overlap(wj).isValid())
          overlappedWalls.add(new Pair<XWall, XWall>(wi, wj));
      }
    }

    while (!overlappedWalls.isEmpty()) {
      int i = Random.Int(overlappedWalls.size());
      Pair<XWall, XWall> pr = overlappedWalls.get(i);
      Point dp = pr.first.overlap(pr.second).random(0);

      overlappedWalls.remove(i);
      if (map[xy2cell(dp.x, dp.y)] == Terrain.WALL) {
        // dig!
        Digger.Set(this, dp, pr.first.isRoomWall || pr.second.isRoomWall ?
                Terrain.DOOR : Terrain.EMPTY);
        digableWalls.remove(pr.first);
        digableWalls.remove(pr.second);

        if (++loops >= maxLoops)
          break;
      }
    }

    // not enough, random select two wall and dig between them
    if (loops < maxLoops && digableWalls.size() >= 2) {
    }

    return loops;
  }
}
