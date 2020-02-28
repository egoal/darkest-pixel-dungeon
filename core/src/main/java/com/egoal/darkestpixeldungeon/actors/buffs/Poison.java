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

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.effects.particles.PoisonParticle;
import com.egoal.darkestpixeldungeon.items.rings.RingOfResistance.Resistance;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class Poison extends Buff implements Hero.Doom {

  protected float left;
  protected float extraDamage = 0;

  private static final String LEFT = "left";
  private static final String EXTRA_DAMAGE = "extra-damage";

  {
    type = buffType.NEGATIVE;
  }

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(LEFT, left);
    bundle.put(EXTRA_DAMAGE, extraDamage);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    left = bundle.getFloat(LEFT);
    extraDamage = bundle.getFloat(EXTRA_DAMAGE);
  }

  public void set(float duration) {
    this.left = Math.max(duration, left);
  }

  public void addDamage(int damage) {
    this.extraDamage = damage;
  }

  public void extend(float duration) {
    this.left += duration;
  }

  @Override
  public int icon() {
    return BuffIndicator.POISON;
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
    return Messages.get(this, "desc", dispTurns(left));
  }

  @Override
  public boolean attachTo(Char target) {
    if (super.attachTo(target) && target.sprite != null) {
      CellEmitter.center(target.pos).burst(PoisonParticle.SPLASH, 5);
      return true;
    } else
      return false;
  }

  @Override
  public boolean act() {
    if (target.isAlive()) {
      target.takeDamage(new Damage((int) (left / 3 + extraDamage) + 1, this, 
              target).type(Damage.Type.MAGICAL).addElement(Damage.Element.POISON));
      spend(TICK);

      if ((left -= TICK) <= 0) {
        detach();
      }

    } else {

      detach();

    }

    return true;
  }

  public static float durationFactor(Char ch) {
    Resistance r = ch.buff(Resistance.class);
    return r != null ? r.durationFactor() : 1;
  }

  @Override
  public void onDeath() {
    Badges.INSTANCE.validateDeathFromPoison();

    Dungeon.fail(getClass());
    GLog.n(Messages.get(this, "ondeath"));
  }
}
