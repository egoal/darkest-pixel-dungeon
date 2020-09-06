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
import com.egoal.darkestpixeldungeon.effects.Chains;
import com.egoal.darkestpixeldungeon.effects.Pushing;
;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.armor.Armor;
import com.egoal.darkestpixeldungeon.items.helmets.GuardHelmet;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.sprites.GuardSprite;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Guard extends Mob {

  //they can only use their chains once
  private boolean chainsUsed = false;

  {
    PropertyConfiger.INSTANCE.set(this, "Guard");

    spriteClass = GuardSprite.class;
    loot = null;    //see createloot.
  }

  @Override
  protected boolean act() {
    Dungeon.level.updateFieldOfView(this, Level.Companion.getFieldOfView());

    if (state == HUNTING &&
            getParalysed() <= 0 &&
            enemy != null &&
            enemy.getInvisible() == 0 &&
            Level.Companion.getFieldOfView()[enemy.getPos()] &&
            Dungeon.level.distance(getPos(), enemy.getPos()) < 5 &&
            !Dungeon.level.adjacent(getPos(), enemy.getPos()) &&
            Random.Int(3) == 0 &&

            chain(enemy.getPos())) {

      return false;

    } else {
      return super.act();
    }
  }

  private boolean chain(int target) {
    if (chainsUsed || enemy.properties().contains(Property.IMMOVABLE))
      return false;

    Ballistica chain = new Ballistica(getPos(), target, Ballistica.PROJECTILE);

    if (chain.collisionPos != enemy.getPos() || chain.path.size() < 2 || Level.Companion.getPit()[chain.path.get(1)])
      return false;
    else {
      int newPos = -1;
      for (int i : chain.subPath(1, chain.dist)) {
        if (!Level.Companion.getSolid()[i] && Actor.Companion.findChar(i) == null) {
          newPos = i;
          break;
        }
      }

      if (newPos == -1) {
        return false;
      } else {
        final int newPosFinal = newPos;
        yell(Messages.get(this, "scorpion"));
        getSprite().parent.add(new Chains(getPos(), enemy.getPos(), new Callback() {
          public void call() {
            Actor.Companion.addDelayed(new Pushing(enemy, enemy.getPos(), newPosFinal, new
                    Callback() {
                      public void call() {
                        enemy.setPos(newPosFinal);
                        Dungeon.level.press(newPosFinal, enemy);
                        Cripple.prolong(enemy, Cripple.class, 4f);
                        if (enemy == Dungeon.hero) {
                          Dungeon.hero.interrupt();
                          Dungeon.observe();
                          GameScene.updateFog();
                        }
                      }
                    }), -1);
            next();
          }
        }));
      }
    }
    chainsUsed = true;
    return true;
  }

  @Override
  protected Item createLoot() {
    //first see if we drop armor, overall chance is 1/8
    float p = Random.Float();
    if (p<0.5f) {
      Armor loot;
      do {
        loot = (Armor) Generator.ARMOR.INSTANCE.generate();
        //50% chance of re-rolling tier 4 or 5 items
      } while (loot.tier >= 4 && Random.Int(2) == 0);
      loot.level(0);
      return loot;
      //otherwise, we may drop a health potion. overall chance is 7/(8 * (7 +
      // potions dropped))
      //with 0 potions dropped that simplifies to 1/8
    } else if(p<0.85f) {
      if (Random.Int(7 + Dungeon.limitedDrops.guardHP.count) < 6) {
        Dungeon.limitedDrops.guardHP.drop();
        return new PotionOfHealing();
      }
    }else{
      return new GuardHelmet().random();
    }

    return null;
  }

  private final String CHAINSUSED = "chainsused";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(CHAINSUSED, chainsUsed);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    chainsUsed = bundle.getBoolean(CHAINSUSED);
  }
}
