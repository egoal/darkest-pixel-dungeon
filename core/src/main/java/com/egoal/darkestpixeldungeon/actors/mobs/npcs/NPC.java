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
package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.windows.WndQuest;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public abstract class NPC extends Mob {

  {
    HP = HT = 1;
    EXP = 0;

    hostile = false;
    state = PASSIVE;
  }

  // never overlap with an item
  protected void throwItem() {
    Heap heap = Dungeon.level.getHeaps().get(pos);
    if (heap != null) {
      int n;
      do {
        n = pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
      } while (!Level.Companion.getPassable()[n] && !Level.Companion.getAvoid()[n]);
      Dungeon.level.drop(heap.pickUp(), n).sprite.drop(pos);
    }
  }

  @Override
  public void beckon(int cell) {
  }

  abstract public boolean interact();

  protected void tell(String text) {
    GameScene.show(new WndQuest(this, text));
  }
}