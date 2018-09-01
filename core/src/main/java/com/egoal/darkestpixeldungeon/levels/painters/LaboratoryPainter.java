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

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.Alchemy;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.keys.IronKey;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.levels.Room;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class LaboratoryPainter extends Painter {

  public static void paint(Level level, Room room) {

    fill(level, room, Terrain.WALL);
    fill(level, room, 1, Terrain.EMPTY_SP);

    Room.Door entrance = room.entrance();

    // set position
    Point pA = null;
    Point pE = null;
    if (entrance.x == room.left) {
      pA = new Point(room.right - 1, room.top + room.height() / 2);
      pE = new Point(pA.x, Random.Int(2) == 0 ? pA.y - 1 : pA.y + 1);
    } else if (entrance.x == room.right) {
      pA = new Point(room.left + 1, room.top + room.height() / 2);
      pE = new Point(pA.x, Random.Int(2) == 0 ? pA.y - 1 : pA.y + 1);
    } else if (entrance.y == room.top) {
      pA = new Point(room.left + room.width() / 2, room.bottom - 1);
      pE = new Point(Random.Int(2) == 0 ? pA.x - 1 : pA.x + 1, pA.y);
    } else if (entrance.y == room.bottom) {
      pA = new Point(room.left + room.width() / 2, room.top + 1);
      pE = new Point(Random.Int(2) == 0 ? pA.x - 1 : pA.x + 1, pA.y);
    }

    // alchemy
    set(level, pA, Terrain.ALCHEMY);
    Alchemy a = new Alchemy();
    a.seed(level, pA.x + level.width() * pA.y, 1);
    level.blobs.put(Alchemy.class, a);

    // enchanting station
    set(level, pE, Terrain.ENCHANTING_STATION);

    int n = Random.IntRange(2, 3);
    for (int i = 0; i < n; i++) {
      int pos;
      do {
        pos = level.pointToCell(room.random());
      } while (
              level.map[pos] != Terrain.EMPTY_SP ||
                      level.heaps.get(pos) != null);
      level.drop(prize(level), pos);
    }

    // lock the room
    entrance.set(Room.Door.Type.LOCKED);
    level.addItemToSpawn(new IronKey(Dungeon.depth));
  }

  private static Item prize(Level level) {

    Item prize = level.findPrizeItem(Potion.class);
    if (prize == null)
      prize = Generator.random(Generator.Category.POTION);

    return prize;
  }
}
