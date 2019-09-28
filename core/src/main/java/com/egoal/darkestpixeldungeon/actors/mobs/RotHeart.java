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
package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.PropertyConfiger;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.plants.Rotberry;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.RotHeartSprite;
import com.watabou.utils.Random;

import java.util.HashSet;

public class RotHeart extends Mob {

  {
    PropertyConfiger.INSTANCE.set(this, "RotHeart");

    spriteClass = RotHeartSprite.class;

    state = PASSIVE;
  }

  @Override
  public int takeDamage(Damage dmg) {

    if (dmg.hasElement(Damage.Element.FIRE)) {
      destroy();
      sprite.die();

      return HP;
    } else {
      return super.takeDamage(dmg);
    }

  }

  @Override
  public Damage defenseProc(Damage damage) {
    GameScene.add(Blob.seed(pos, 20, ToxicGas.class));

    return super.defenseProc(damage);
  }

  @Override
  public void beckon(int cell) {
    //do nothing
  }

  @Override
  protected boolean getCloser(int target) {
    return false;
  }

  @Override
  public void destroy() {
    super.destroy();
    for (Mob mob : Dungeon.level.getMobs().toArray(new Mob[Dungeon.level.getMobs().size
            ()])) {
      if (mob instanceof RotLasher) {
        mob.die(null);
      }
    }
  }

  @Override
  public void die(Object cause) {
    super.die(cause);
    Dungeon.level.drop(new Rotberry.Seed(), pos).sprite.drop();
  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(ToxicGas.class);
    IMMUNITIES.add(Terror.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }

}
