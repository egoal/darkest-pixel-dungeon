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
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat;
import com.egoal.darkestpixeldungeon.sprites.CrabSprite;
import com.watabou.utils.Random;

public class Crab extends Mob {

  {
    spriteClass = CrabSprite.class;

    HP = HT = 15;
    defenseSkill = 5;
    baseSpeed = 2f;

    EXP = 4;
    maxLvl = 9;

    loot = new MysteryMeat();
    lootChance = 0.167f;
    
    addResistances(Damage.Element.LIGHT, .8f);
    addResistances(Damage.Element.ICE, 1.1f);
  }

  @Override
  public Damage giveDamage(Char target) {
    return new Damage(Random.NormalIntRange(1, 8), this, target);
  }

  @Override
  public int attackSkill(Char target) {
    return 12;
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    dmg.value -= Random.NormalIntRange(0, 4);
    return dmg;
  }
}
