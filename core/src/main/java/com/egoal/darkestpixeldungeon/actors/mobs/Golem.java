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

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Amok;
import com.egoal.darkestpixeldungeon.actors.buffs.Sleep;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Imp;
import com.egoal.darkestpixeldungeon.sprites.GolemSprite;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Golem extends Mob {

  {
    spriteClass = GolemSprite.class;

    HP = HT = 85;
    defenseSkill = 18;

    EXP = 12;
    maxLvl = 22;
    
    properties.add(Property.MACHINE);
    
    addResistances(Damage.Element.LIGHT, .75f);
  }

  @Override
  public Damage giveDamage(Char target) {
    return new Damage(Random.NormalIntRange(25, 40), this, target);
  }

  @Override
  public int attackSkill(Char target) {
    return 28;
  }

  @Override
  protected float attackDelay() {
    return 1.5f;
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    dmg.value -= Random.NormalIntRange(0, 12);
    return dmg;
  }

  @Override
  public void die(Object cause) {
    Imp.Quest.process(this);

    super.die(cause);
  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(Amok.class);
    IMMUNITIES.add(Terror.class);
    IMMUNITIES.add(Sleep.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }
}
