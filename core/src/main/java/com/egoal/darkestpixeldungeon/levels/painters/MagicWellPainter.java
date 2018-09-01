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
package com.egoal.darkestpixeldungeon.levels.painters;

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfHealth;
import com.egoal.darkestpixeldungeon.actors.blobs.WellWater;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfAwareness;
import com.egoal.darkestpixeldungeon.actors.blobs.WaterOfTransmutation;
import com.egoal.darkestpixeldungeon.levels.Room;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class MagicWellPainter extends Painter {

  private static final Class<?>[] WATERS =
          {WaterOfAwareness.class, WaterOfHealth.class, WaterOfTransmutation
                  .class};

  public static void paint(Level level, Room room) {

    fill(level, room, Terrain.WALL);
    fill(level, room, 1, Terrain.EMPTY);

    Point c = room.center();
    set(level, c.x, c.y, Terrain.WELL);

    @SuppressWarnings("unchecked")
    Class<? extends WellWater> waterClass =
            Dungeon.depth >= Dungeon.transmutation ?
                    WaterOfTransmutation.class :
                    (Class<? extends WellWater>) Random.element(WATERS);

    if (waterClass == WaterOfTransmutation.class) {
      Dungeon.transmutation = Integer.MAX_VALUE;
    }

    WellWater water = (WellWater) level.blobs.get(waterClass);
    if (water == null) {
      try {
        water = waterClass.newInstance();
      } catch (Exception e) {
        DarkestPixelDungeon.reportException(e);
        return;
      }
    }
    water.seed(level, c.x + level.width() * c.y, 1);
    level.blobs.put(waterClass, water);

    room.entrance().set(Room.Door.Type.REGULAR);
  }
}
