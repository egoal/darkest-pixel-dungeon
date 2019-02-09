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

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class Ooze extends Buff {

  {
    type = buffType.NEGATIVE;
  }

  @Override
  public int icon() {
    return BuffIndicator.OOZE;
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  @Override
  public String heroMessage() {
    return Messages.get(this, "heromsg");
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc");
  }

  @Override
  public boolean act() {
    if (target.isAlive()) {
      if (Dungeon.depth > 4)
        // target.damage( Dungeon.depth/5, this );
        target.takeDamage(new Damage(Dungeon.depth / 5, this, target)
                .type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE));
      else if (Random.Int(2) == 0)
        target.takeDamage(new Damage(1, this, target)
                .type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE));
      if (!target.isAlive() && target == Dungeon.hero) {
        Dungeon.fail(getClass());
        GLog.n(Messages.get(this, "ondeath"));
      }
      
      spend(TICK);
    }
    if (Level.water[target.pos]) {
      detach();
    }
    return true;
  }

}
