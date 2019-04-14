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

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.egoal.darkestpixeldungeon.utils.GLog;

public class MagicalSleep extends Buff {

  private static final float STEP = 1f;
  private static final float MAX_SLEEP_TIME = 30f;

  private float sleeped_ = 0f;

  @Override
  public boolean attachTo(Char target) {
    if (super.attachTo(target) && !target.immunizedBuffs().contains(Sleep
            .class)) {

      if (target instanceof Hero)
        GLog.i(Messages.get(this, "fallasleep"));
      else if (target instanceof Mob)
        ((Mob) target).state = ((Mob) target).SLEEPING;

      target.paralysed++;

      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean act() {
    if (target instanceof Hero) {
      target.HP = Math.min(target.HP + 1, target.HT);
      ((Hero) target).setResting(true);
      target.buff(Pressure.class).downPressure(.5f);
      sleeped_ += STEP;
      if (sleeped_ > MAX_SLEEP_TIME) {
        GLog.p(Messages.get(this, "wakeup"));
        detach();
      }
    }
    spend(STEP);
    return true;
  }

  @Override
  public void detach() {
    if (target.paralysed > 0)
      target.paralysed--;
    if (target instanceof Hero)
      ((Hero) target).setResting(false);
    super.detach();
  }

  @Override
  public int icon() {
    return BuffIndicator.MAGIC_SLEEP;
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc");
  }
}