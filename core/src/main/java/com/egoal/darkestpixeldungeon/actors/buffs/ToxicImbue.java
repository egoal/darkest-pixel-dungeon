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

import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class ToxicImbue extends Buff {

  public static final float DURATION = 30f;

  protected float left;

  private static final String LEFT = "left";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(LEFT, left);

  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    left = bundle.getFloat(LEFT);
  }

  public void set(float duration) {
    this.left = duration;
  }

  ;


  @Override
  public boolean act() {
    GameScene.add(Blob.seed(target.getPos(), 50, ToxicGas.class));

    spend(Actor.TICK);
    left -= Actor.TICK;
    if (left <= 0)
      detach();

    return true;
  }

  @Override
  public int icon() {
    return BuffIndicator.IMMUNITY;
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc", dispTurns(left));
  }

  {
    immunities.add(ToxicGas.class);
    immunities.add(Poison.class);
  }
}
