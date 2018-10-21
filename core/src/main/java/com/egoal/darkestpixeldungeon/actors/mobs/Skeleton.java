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
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.artifacts.HandOfTheElder;
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.SkeletonSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Skeleton extends Mob {

  {
    spriteClass = SkeletonSprite.class;

    HP = HT = 25;
    defenseSkill = 9;

    EXP = 5;
    maxLvl = 10;

    loot = Generator.Category.WEAPON;
    lootChance = 0.2f;

    properties.add(Property.UNDEAD);

    addResistances(Damage.Element.FIRE, .75f);
    addResistances(Damage.Element.HOLY, .667f);
  }

  @Override
  public Damage giveDamage(Char target) {
    return new Damage(Random.NormalIntRange(2, 10), this, target);
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    dmg.value -= Random.NormalIntRange(0, 5);
    return dmg;
  }

  @Override
  public void die(Object cause) {

    super.die(cause);

    boolean heroKilled = false;
    for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
      Char ch = findChar(pos + PathFinder.NEIGHBOURS8[i]);
      if (ch != null && ch.isAlive()) {
        Damage dmg = new Damage(Random.NormalIntRange(4, 12),
                this, ch).addElement(Damage.Element.SHADOW);
        ch.takeDamage(dmg);
        if (ch == Dungeon.hero && !ch.isAlive()) {
          heroKilled = true;
        }
      }
    }

    if (Dungeon.visible[pos]) {
      Sample.INSTANCE.play(Assets.SND_BONES);
    }

    if (heroKilled) {
      Dungeon.fail(getClass());
      GLog.n(Messages.get(this, "explo_kill"));
    }
  }

  @Override
  protected Item createLoot() {
    if(!Dungeon.limitedDrops.handOfElder.dropped() && Random.Float()<0.1f) {
      Dungeon.limitedDrops.handOfElder.drop();
      return new HandOfTheElder().random();
    }
    else{
      Item loot;
      do {
        loot = Generator.random(Generator.Category.WEAPON);
        //50% chance of re-rolling tier 4 or 5 items
      }
      while (loot instanceof MeleeWeapon && ((MeleeWeapon) loot).tier >= 4 &&
              Random.Int(2) == 0);
      loot.level(0);
      
      return loot;
    }
  }

  @Override
  public int attackSkill(Char target) {
    return 12;
  }

}
