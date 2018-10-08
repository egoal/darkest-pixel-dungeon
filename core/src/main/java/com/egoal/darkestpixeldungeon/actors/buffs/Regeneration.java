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
package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.egoal.darkestpixeldungeon.Dungeon;

public class Regeneration extends Buff {

  private static final float REGENERATION_DELAY = 8f;

  @Override
  public boolean act() {
    if (target.isAlive()) {

      int dhp = 1;
      ChaliceOfBlood.chaliceRegen regenBuff = Dungeon.hero.buff
              (ChaliceOfBlood.chaliceRegen.class);

      if(regenBuff!=null && !regenBuff.isCursed())
        dhp += target.HT*0.01*regenBuff.itemLevel();

      if (target.HP < target.HT && !((Hero) target).isStarving()) {
        LockedFloor lock = target.buff(LockedFloor.class);
        if (target.HP > 0 && (lock == null || lock.regenOn())) {
          target.HP += dhp;
          if (target.HP >= target.HT) {
            target.HP = target.HT;
            ((Hero) target).resting = false;
          }
        }
      }

      spend(regenBuff != null && regenBuff.isCursed() ? REGENERATION_DELAY * 
              2 : REGENERATION_DELAY);

    } else {

      diactivate();

    }

    return true;
  }
}
