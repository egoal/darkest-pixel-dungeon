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
package com.egoal.darkestpixeldungeon.items.rings;

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Poison;
import com.egoal.darkestpixeldungeon.actors.mobs.Eye;
import com.egoal.darkestpixeldungeon.actors.mobs.Warlock;
import com.egoal.darkestpixeldungeon.actors.mobs.Yog;
import com.egoal.darkestpixeldungeon.levels.traps.LightningTrap;
import com.egoal.darkestpixeldungeon.actors.buffs.Venom;
import com.watabou.utils.Random;

import java.util.HashSet;

public class RingOfElements extends Ring {

  @Override
  protected RingBuff buff() {
    return new Resistance();
  }

  public class Resistance extends RingBuff {

    public Damage resist(Damage dmg){
      if(dmg.element!=Damage.Element.NONE){
        // resist any damage with element
        dmg.value *=  Math.pow(.8, level()/3.);
      }
      return dmg;
    }

    // decrease debuff duration
    public float durationFactor() {
      return level() < 0 ? 1 : (1 + 0.5f * level()) / (1 + level());
    }
  }
}
