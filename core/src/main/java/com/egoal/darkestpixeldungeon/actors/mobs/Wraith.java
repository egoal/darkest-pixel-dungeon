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
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Grim;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.WraithSprite;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Wraith extends Mob {

  private static final float SPAWN_DELAY = 2f;

  protected int level;

  {
    spriteClass = WraithSprite.class;

    HP = HT = 1;
    EXP = 0;

    flying = true;

    properties.add(Property.UNDEAD);
    addResistances(Damage.Element.SHADOW, 1.25f);
    addResistances(Damage.Element.HOLY, .667f);
  }

  private static final String LEVEL = "level";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(LEVEL, level);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    level = bundle.getInt(LEVEL);
    adjustStats(level);
  }

  @Override
  public Damage giveDamage(Char target) {
    return new Damage(Random.NormalIntRange(1 + level / 2, 2 + level), this, 
            target);
  }

  @Override
  public int attackSkill(Char target) {
    return 10 + level;
  }

  public void adjustStats(int level) {
    this.level = level;
    defenseSkill = attackSkill(null) * 5;
    enemySeen = true;
  }

  @Override
  public boolean reset() {
    state = WANDERING;
    return true;
  }

  public static void spawnAround(int pos) {
    for (int n : PathFinder.NEIGHBOURS4) {
      int cell = pos + n;
      if (Level.passable[cell] && Actor.findChar(cell) == null) {
        spawnAt(cell);
      }
    }
  }

  public static Wraith spawnAt(int pos) {
    if (Level.passable[pos] && Actor.findChar(pos) == null) {

      Wraith w = new Wraith();
      w.adjustStats(Dungeon.depth);
      w.pos = pos;
      w.state = w.HUNTING;
      GameScene.add(w, SPAWN_DELAY);

      w.sprite.alpha(0);
      w.sprite.parent.add(new AlphaTweener(w.sprite, 1, 0.5f));

      w.sprite.emitter().burst(ShadowParticle.CURSE, 5);

      return w;
    } else {
      return null;
    }
  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(Grim.class);
    IMMUNITIES.add(Terror.class);
    IMMUNITIES.add(Corruption.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }
}
