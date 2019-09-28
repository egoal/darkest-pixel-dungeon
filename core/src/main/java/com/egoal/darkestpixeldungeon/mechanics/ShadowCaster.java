/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.mechanics;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.utils.BArray;
import com.watabou.utils.PathFinder;

public final class ShadowCaster {

  private static final int MAX_DISTANCE = 10;

  private static boolean[] falseArray;

  private static int[][] rounding;

  static {
    rounding = new int[MAX_DISTANCE + 1][];
    for (int i = 1; i <= MAX_DISTANCE; i++) {
      rounding[i] = new int[i + 1];
      for (int j = 1; j <= i; j++) {
        rounding[i][j] = (int) Math.min(j, Math.round(i * Math.cos(Math.asin
                (j / (i + 0.5)))));
      }
    }
  }

  public static void castShadow(int x, int y, boolean[] fieldOfView,
                                int viewDistance, int seeDistance) {

    BArray.setFalse(fieldOfView);

    fieldOfView[y * Dungeon.level.width() + x] = true;

    boolean[] losBlocking = Level.Companion.getLosBlocking();
    Obstacles obs = new Obstacles();

    scanSector(viewDistance, seeDistance, fieldOfView, losBlocking, obs, x,
            y, +1, +1, 0, 0);
    scanSector(viewDistance, seeDistance, fieldOfView, losBlocking, obs, x,
            y, -1, +1, 0, 0);
    scanSector(viewDistance, seeDistance, fieldOfView, losBlocking, obs, x,
            y, +1, -1, 0, 0);
    scanSector(viewDistance, seeDistance, fieldOfView, losBlocking, obs, x,
            y, -1, -1, 0, 0);
    scanSector(viewDistance, seeDistance, fieldOfView, losBlocking, obs, x,
            y, 0, 0, +1, +1);
    scanSector(viewDistance, seeDistance, fieldOfView, losBlocking, obs, x,
            y, 0, 0, -1, +1);
    scanSector(viewDistance, seeDistance, fieldOfView, losBlocking, obs, x,
            y, 0, 0, +1, -1);
    scanSector(viewDistance, seeDistance, fieldOfView, losBlocking, obs, x,
            y, 0, 0, -1, -1);

  }

  //FIXME This is is the primary performance bottleneck for game logic, need 
  // to optimize or rewrite
  private static void scanSector(int viewDistance, int seeDistance, boolean[]
          fieldOfView, boolean[] losBlocking, Obstacles obs, int cx, int cy,
                                 int m1, int m2, int m3, int m4) {

    obs.reset();

    for (int p = 1; p <= seeDistance; p++) {

      float dq2 = 0.5f / p;

      int pp = rounding[seeDistance][p];
      for (int q = 0; q <= pp; q++) {

        int x = cx + q * m1 + p * m3;
        int y = cy + p * m2 + q * m4;

        if (y >= 0 && y < Dungeon.level.height() && x >= 0 && x < Dungeon
                .level.width()) {

          float a0 = (float) q / p;
          float a1 = a0 - dq2;
          float a2 = a0 + dq2;

          int pos = y * Dungeon.level.width() + x;

          if (obs.isBlocked(a0) && obs.isBlocked(a1) && obs.isBlocked(a2)) {

            // Do nothing
          } else {
            if (p <= viewDistance || Level.Companion.getLighted()[pos]) {
              // in view or lighted
              fieldOfView[pos] = true;
            }
          }

          if (losBlocking[pos]) {
            obs.add(a1, a2);
          }

        }
      }

      obs.nextRow();
    }
  }

  private static final class Obstacles {

    private static int SIZE = (MAX_DISTANCE + 1) * (MAX_DISTANCE + 1) / 2;
    private float[] a1 = new float[SIZE];
    private float[] a2 = new float[SIZE];

    private int length;
    private int limit;

    public void reset() {
      length = 0;
      limit = 0;
    }

    public void add(float o1, float o2) {

      if (length > limit && o1 <= a2[length - 1]) {

        // Merging several blocking cells
        a2[length - 1] = o2;

      } else {

        a1[length] = o1;
        a2[length++] = o2;

      }

    }

    public boolean isBlocked(float a) {
      for (int i = 0; i < limit; i++) {
        if (a >= a1[i] && a <= a2[i]) {
          return true;
        }
      }
      return false;
    }

    public void nextRow() {
      limit = length;
    }
  }

  public static void castShadowRecursively(int x, int y, boolean[] fieldOfView,
                                           int viewDistance, int seeDistance) {

    BArray.setFalse(fieldOfView);

    //set source cell to true
    fieldOfView[y * Dungeon.level.width() + x] = true;

    boolean[] losBlocking = Level.Companion.getLosBlocking();

    //scans octants, clockwise
    scanOctant(viewDistance, seeDistance, fieldOfView, losBlocking, 1, x, y, 
            0.0, 1.0, +1, -1, false);
    scanOctant(viewDistance, seeDistance, fieldOfView, losBlocking, 1, x, y, 
            0.0, 1.0, -1, +1, true);
    scanOctant(viewDistance, seeDistance, fieldOfView, losBlocking, 1, x, y, 
            0.0, 1.0, +1, +1, true);
    scanOctant(viewDistance, seeDistance, fieldOfView, losBlocking, 1, x, y, 
            0.0, 1.0, +1, +1, false);
    scanOctant(viewDistance, seeDistance, fieldOfView, losBlocking, 1, x, y, 
            0.0, 1.0, -1, +1, false);
    scanOctant(viewDistance, seeDistance, fieldOfView, losBlocking, 1, x, y, 
            0.0, 1.0, +1, -1, true);
    scanOctant(viewDistance, seeDistance, fieldOfView, losBlocking, 1, x, y, 
            0.0, 1.0, -1, -1, true);
    scanOctant(viewDistance, seeDistance, fieldOfView, losBlocking, 1, x, y, 
            0.0, 1.0, -1, -1, false);

  }

  //scans a single 45 degree octant of the FOV.
  //This can add up to a whole FOV by mirroring in X(mX), Y(mY), and X=Y(mXY)
  private static void scanOctant(int viewDistance, int seeDistance, boolean[]
          fov,
                                 boolean[] blocking, int row, int x, int y,
                                 double lSlope, double rSlope,
                                 int mX, int mY, boolean mXY) {

    boolean inBlocking = false;
    int start, end;
    int col;

    //calculations are offset by 0.5 because FOV is coming from the center of
    // the source cell

    //for each row, starting with the current one
    for (; row <= seeDistance; row++) {

      //we offset by slightly less than 0.5 to account for slopes just 
      // touching a cell
      if (lSlope == 0) start = 0;
      else start = (int) Math.floor((row - 0.5) * lSlope + 0.499);

      if (rSlope == 1) end = rounding[seeDistance][row];
      else end = Math.min(rounding[seeDistance][row],
              (int) Math.ceil((row + 0.5) * rSlope - 0.499));

      //coordinates of source
      int cell = x + y * Dungeon.level.width();

      //plus coordinates of current cell (including mirroring in x, y, and x=y)
      if (mXY) cell += mX * start * Dungeon.level.width() + mY * row;
      else cell += mX * start + mY * row * Dungeon.level.width();

      if (cell < 0 || cell >= Dungeon.level.length()) return;

      //for each column in this row, which
      for (col = start; col <= end; col++) {
        if (cell < 0 || cell >= Dungeon.level.length()) continue;
        
        if (row <= viewDistance || Level.Companion.getLighted()[cell])
          fov[cell] = true;

        if (blocking[cell]) {
          if (!inBlocking) {
            inBlocking = true;

            //start a new scan, 1 row deeper, ending at the left side of 
            // current cell
            if (col != start) {
              scanOctant(viewDistance, seeDistance, fov, blocking, row + 1, 
                      x, y, lSlope,
                      //change in x over change in y
                      (col - 0.5) / (row + 0.5),
                      mX, mY, mXY);
            }
          }

        } else {
          if (inBlocking) {
            inBlocking = false;

            //restrict current scan to the left side of current cell for 
            // future rows

            //change in x over change in y
            lSlope = (col - 0.5) / (row - 0.5);
          }
        }

        if (!mXY) cell += mX;
        else cell += mX * Dungeon.level.width();
      }

      //if the row ends in a blocking cell, this scan is finished.
      if (inBlocking) return;
    }
  }

}
