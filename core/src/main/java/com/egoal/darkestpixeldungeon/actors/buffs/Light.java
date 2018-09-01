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
import com.egoal.darkestpixeldungeon.actors.hero.HeroPerk;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class Light extends FlavourBuff {

  public static final float DURATION = 200f;
  public static final int DISTANCE = 6;

  @Override
  public boolean attachTo(Char target) {
    if (super.attachTo(target)) {
      if (Dungeon.level != null) {
        target.viewDistance = Math.max(Dungeon.level.viewDistance, DISTANCE);
        Dungeon.observe();
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void detach() {
    target.viewDistance = Dungeon.level.viewDistance;
    if (target instanceof Hero) {
      if (((Hero) target).heroPerk.contain(HeroPerk.Perk.NIGHT_VISION))
        target.viewDistance += 1;
    }

    Dungeon.observe(DISTANCE + 1);
    super.detach();
  }

  @Override
  public int icon() {
    return BuffIndicator.LIGHT;
  }

  @Override
  public void fx(boolean on) {
    if (on) target.sprite.add(CharSprite.State.ILLUMINATED);
    else target.sprite.remove(CharSprite.State.ILLUMINATED);
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc", dispTurns());
  }

}
