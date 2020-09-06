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


import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Item;

public class RingOfMight extends Ring {

  @Override
  public boolean doEquip(Hero hero) {
    if (super.doEquip(hero)) {
      hero.setHT(hero.getHT() + level() * 5);
      hero.setHP(Math.min(hero.getHP(), hero.getHT()));
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean doUnequip(Hero hero, boolean collect, boolean single) {

    if (super.doUnequip(hero, collect, single)) {
      hero.setHT(hero.getHT() - level() * 5);
      hero.setHP(Math.min(hero.getHP(), hero.getHT()));
      return true;
    } else {
      return false;
    }

  }

  @Override
  public Item upgrade() {
    if (getBuff() != null && getBuff().target != null) {
      getBuff().target.setHT(getBuff().target.getHT() + 5);
    }
    return super.upgrade();
  }

  @Override
  public void level(int value) {
    if (getBuff() != null && getBuff().target != null) {
      getBuff().target.setHT(getBuff().target.getHT() - level() * 5);
    }
    super.level(value);
    if (getBuff() != null && getBuff().target != null) {
      getBuff().target.setHT(getBuff().target.getHT() + level() * 5);
      getBuff().target.setHP(Math.min(getBuff().target.getHP(), getBuff().target.getHT()));
    }
  }

  @Override
  protected RingBuff buff() {
    return new Might();
  }

  public class Might extends RingBuff { }
}

