package com.egoal.darkestpixeldungeon.levels;

import android.util.Log;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption;
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
    digFirstRoom();

    int rooms = 20;
    while(rooms>0){
      if(Random.Float()<0.35){
        if(digARoom())
          --rooms;
        else
          break;
      }else if(!digACorridor())
        break;
    }
    Log.d("dpd", String.format("%d rooms digged.", 20-rooms));
    
    paintCorridor();

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
  // my digging algorithm
  // ArrayList<XRoom> rooms;
  ArrayList<XWall> digableWalls = new ArrayList<>();

  private void digFirstRoom() {
    int w = Random.IntRange(3, 6);
    int h = Random.IntRange(3, 6);
    int x = Random.IntRange(width / 4, width / 4 * 3 - w);
    int y = Random.IntRange(height / 4, height / 4 * 3 - h);

    Digger.fill(this, x, y, w, h, Terrain.EMPTY);
    XRect room = XRect.create(x, y, w, h);
    digableWalls.add(new XWall(room.x1 - 1, room.x1 - 1, room.y1, room.y2,
            Digger.LEFT));
    digableWalls.add(new XWall(room.x2 + 1, room.x2 + 1, room.y1, room.y2, 
            Digger.RIGHT));
    digableWalls.add(new XWall(room.x1, room.x2, room.y1 - 1, room.y1 - 1, 
            Digger.UP));
    digableWalls.add(new XWall(room.x1, room.x2, room.y2 + 1, room.y2 + 1, 
            Digger.DOWN));
  }

  private boolean digARoom() {
    if (digableWalls.isEmpty()) return false;

    // try 100 times to dig a room
    for (int i = 0; i < 100; ++i) {
      int index = Random.Int(digableWalls.size());
      XWall wall = digableWalls.get(index);
      Point door = wall.random(0);

      // get a rect to place room
      int w = Random.IntRange(3, 6);
      int h = Random.IntRange(3, 6);
      int x = -1;
      int y = -1;
      switch (wall.direction) {
        case Digger.LEFT:
          x = wall.x1 - w;
          y = Random.IntRange(door.y - h + 1, door.y);
          break;
        case Digger.RIGHT:
          x = wall.x2 + 1;
          y = Random.IntRange(door.y - h + 1, door.y);
          break;
        case Digger.UP:
          x = Random.IntRange(door.x - w + 1, door.x);
          y = wall.y1 - h;
          break;
        case Digger.DOWN:
          x = Random.IntRange(door.x - w + 1, door.x);
          y = wall.y2 + 1;
          break;
      }

      XRoom newroom = XRoom.create(x, y, w, h, wall, door);
      if (canDigAt(newroom)) {
        // dig!
        digableWalls.addAll(new NormalRectRoomDigger(newroom).dig(this));

        Digger.set(this, door, Terrain.DOOR);

        digableWalls.remove(index);
        return true;
      }
    }

    return false;
  }

  private boolean digACorridor() {
    if (digableWalls.isEmpty()) return false;

    for (int i = 0; i < 100; ++i) {
      int index = Random.Int(digableWalls.size());
      XWall wall = digableWalls.get(index);
      Point door = wall.random(0);

      // get a rect to place room
      int len = Random.IntRange(2, 5);
      int w = -1;
      int h = -1;
      int x = -1;
      int y = -1;
      if (Random.Int(2) == 0) {
        w = 1;
        h = len;
      } else {
        w = len;
        h = 1;
      }

      switch (wall.direction) {
        case Digger.LEFT:
          x = wall.x1 - w;
          y = Random.IntRange(door.y - h + 1, door.y);
          break;
        case Digger.RIGHT:
          x = wall.x2 + 1;
          y = Random.IntRange(door.y - h + 1, door.y);
          break;
        case Digger.UP:
          x = Random.IntRange(door.x - w + 1, door.x);
          y = wall.y1 - h;
          break;
        case Digger.DOWN:
          x = Random.IntRange(door.x - w + 1, door.x);
          y = wall.y2 + 1;
          break;
      }

      XRoom newroom = XRoom.create(x, y, w, h, wall, door);
      if (canDigCorridorAt(newroom)) {
        // dig!
        digableWalls.addAll(new CorridorDigger(newroom).dig(this));

        Digger.set(this, door, wall.isRoomWall ? Terrain.DOOR : Terrain.EMPTY);

        digableWalls.remove(index);
        return true;
      }
    }

    return false;
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

  private boolean canDigCorridorAt(XRect rect) {
    if (rect.x1 > 0 && rect.x2 < width - 1 && rect.y1 > 0 && rect.y2 < height
            - 1) {
      for (int x = rect.x1 - 1; x <= rect.x2 + 1; ++x)
        for (int y = rect.y1 - 1; y <= rect.y2 + 1; ++y) {
          int t = map[xy2cell(x, y)];
          if (t != Terrain.WALL && t != CorridorDigger.CORRIDOR)
            return false;
        }

      return true;
    }

    return false;
  }

  private void paintCorridor() {
    for (int i = 0; i < length; ++i)
      if (map[i] == CorridorDigger.CORRIDOR)
        map[i] = Terrain.EMPTY;
  }
}
