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
import com.egoal.darkestpixeldungeon.sprites.ScorpioSprite;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

public class Scorpio extends Mob {

  {
    PropertyConfiger.INSTANCE.set(this, "Scorpio");

    spriteClass = ScorpioSprite.class;

    loot = new PotionOfHealing();
  }

  @Override
  public int viewDistance() {
    return 6;
  }

  @Override
  public Damage giveDamage(Char target) {
    return super.giveDamage(target).addElement(Damage.Element.POISON).addFeature(Damage.Feature.RANGED);
  }

  @Override
  protected boolean canAttack(Char enemy) {
    Ballistica attack = new Ballistica(getPos(), enemy.getPos(), Ballistica.PROJECTILE);
    return !Dungeon.level.adjacent(getPos(), enemy.getPos()) && attack.collisionPos ==
            enemy.getPos();
  }

  @Override
  public Damage attackProc(Damage dmg) {
    if (Random.Int(3) == 0)
      Buff.prolong((Char) dmg.to, Cripple.class, Cripple.DURATION);

    return dmg;
  }

  @Override
  protected boolean getCloser(int target) {
    if (state == HUNTING) {
      return enemySeen && getFurther(target);
    } else {
      return super.getCloser(target);
    }
  }

  @Override
  protected Item createLoot() {
    //5/count+5 total chance of getting healing, failing the 2nd roll drops 
    // mystery meat instead.
    if (Random.Int(5 + Dungeon.limitedDrops.scorpioHP.count) <= 4) {
      Dungeon.limitedDrops.scorpioHP.count++;
      return (Item) loot;
    } else {
      return new MysteryMeat();
    }
  }

}
