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

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.Fire;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Poison;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Ghost;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.CurareDart;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.GnollTricksterSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class GnollTrickster extends Gnoll {

  {
    spriteClass = GnollTricksterSprite.class;

    HP = HT = 20;
    defenseSkill = 5;

    EXP = 5;

    state = WANDERING;

    loot = new CurareDart();
    lootChance = 1f;

    properties.add(Property.MINIBOSS);
  }

  private int combo = 0;

  @Override
  public int attackSkill(Char target) {
    return 16;
  }

  @Override
  protected boolean canAttack(Char enemy) {
    Ballistica attack = new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE);
    return !Dungeon.level.adjacent(pos, enemy.pos) && attack.collisionPos ==
            enemy.pos;
  }

  @Override
  public Damage attackProc(Damage damage) {
    Char enemy = (Char) damage.to;
    //The gnoll's attacks get more severe the more the player lets it hit them
    combo++;
    int effect = Random.Int(4) + combo;

    if (effect > 2) {

      if (effect >= 6 && enemy.buff(Burning.class) == null) {

        if (Level.flamable[enemy.pos])
          GameScene.add(Blob.seed(enemy.pos, 4, Fire.class));
        Buff.affect(enemy, Burning.class).reignite(enemy);

      } else
        Buff.affect(enemy, Poison.class).set((effect - 2) * Poison
                .durationFactor(enemy));

    }
    return damage;
  }

  @Override
  protected boolean getCloser(int target) {
    combo = 0; //if he's moving, he isn't attacking, reset combo.
    if (state == HUNTING) {
      return enemySeen && getFurther(target);
    } else {
      return super.getCloser(target);
    }
  }

  @Override
  public void die(Object cause) {
    super.die(cause);

    Ghost.Quest.process();
  }

  private static final String COMBO = "combo";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(COMBO, combo);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    combo = bundle.getInt(COMBO);
  }

}
