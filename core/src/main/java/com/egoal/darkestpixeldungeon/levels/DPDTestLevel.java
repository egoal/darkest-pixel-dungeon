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
    digFirstRoom();

    int rooms = 20;
    TunnelDigger tunnelDigger = new TunnelDigger();
    NormalRoomDigger normalRoomDigger = new NormalRoomDigger();
    boolean digok = true;
    while (rooms > 0 && digok) {
      if (Random.Float() < 0.4) {
        digok = dig(normalRoomDigger);
        --rooms;
      } else {
        digok = dig(tunnelDigger);
      }
    }
    Log.d("dpd", String.format("%d rooms digged.", 20 - rooms));
    if (rooms < 12) return false;

    int lc = makeLoopClosure(6);
    Log.d("dpd", String.format("%d loop closures linked.", lc));
    if (lc < 3) return false;
    
    // place entrance and exit
    ArrayList<XRoom> normalRooms = new ArrayList<>();
    for (XRoom room : diggedRooms) {
      if (room.type == XRoom.Type.NORMAL)
        normalRooms.add(room);
    }
    XRoom roomEnter = Random.element(normalRooms);
    XRoom roomExit;
    int trials = 0;
    do {
      if (++trials == 100)
        return false;

      roomExit = Random.element(normalRooms);
    } while (roomExit == roomEnter || distance(pointToCell(roomExit.cen()),
            pointToCell(roomEnter.cen())) < 12);

    entrance = pointToCell(roomEnter.random(1));
    map[entrance] = Terrain.ENTRANCE;
    exit = pointToCell(roomExit.random(1));
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
  // ArrayList<XRoom> rooms;
  ArrayList<XWall> digableWalls = new ArrayList<>();
  ArrayList<XRoom> diggedRooms = new ArrayList<>(); // only the rect info counts

  private void digFirstRoom() {
    int w = Random.IntRange(3, 6);
    int h = Random.IntRange(3, 6);
    int x = Random.IntRange(width / 4, width / 4 * 3 - w);
    int y = Random.IntRange(height / 4, height / 4 * 3 - h);

    Digger.Fill(this, x, y, w, h, Terrain.EMPTY);
    XRect room = XRect.create(x, y, w, h);
    digableWalls.add(new XWall(room.x1 - 1, room.x1 - 1, room.y1, room.y2,
            Digger.LEFT));
    digableWalls.add(new XWall(room.x2 + 1, room.x2 + 1, room.y1, room.y2,
            Digger.RIGHT));
    digableWalls.add(new XWall(room.x1, room.x2, room.y1 - 1, room.y1 - 1,
            Digger.UP));
    digableWalls.add(new XWall(room.x1, room.x2, room.y2 + 1, room.y2 + 1,
            Digger.DOWN));

    diggedRooms.add(new XRoom(room.x1, room.x2, room.y1, room.y2,
            new XWall(room.cen().x, room.cen().y, Digger.NONE), new Point()));
  }

  private boolean dig(Digger digger) {
    if (digableWalls.isEmpty()) return false;

    // try 100 times to dig a room
    for (int i = 0; i < 100; ++i) {
      // select a wall, choose a init dig position
      int index = Random.Int(digableWalls.size());
      XWall wall = digableWalls.get(index);

      // determine place to dig
      XRoom newRoom = digger.desireDigRoom(wall);

      if (canDigAt(newRoom)) {
        // dig! door type is set in the digger
        digableWalls.addAll(digger.dig(this, newRoom));
        digableWalls.remove(index);

        //! set room type!!!
        Digger.AssignRoomType(newRoom, digger);
        diggedRooms.add(newRoom);

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
