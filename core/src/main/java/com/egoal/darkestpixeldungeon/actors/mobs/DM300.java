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

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.items.artifacts.CapeOfThorns;
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.DM300Sprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon;
import com.egoal.darkestpixeldungeon.ui.BossHealthBar;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.sql.DatabaseMetaData;
import java.util.HashSet;

public class DM300 extends Mob {

  {
    spriteClass = DM300Sprite.class;

    HP = HT = 200;
    EXP = 30;
    defenseSkill = 18;

    loot = new CapeOfThorns().identify();
    lootChance = 0.333f;

    properties.add(Property.BOSS);

    addResistances(Damage.Element.ICE, .8f);
    addResistances(Damage.Element.POISON, 100.f, 1.5f);
    addResistances(Damage.Element.LIGHT, .667f);
  }

  @Override
  public Damage giveDamage(Char target) {
    return new Damage(Random.NormalIntRange(20, 25), this, target);
  }

  @Override
  public int attackSkill(Char target) {
    return 28;
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    dmg.value -= Random.NormalIntRange(0, 10);
    return dmg;
  }

  @Override
  public boolean act() {

    GameScene.add(Blob.seed(pos, 30, ToxicGas.class));

    return super.act();
  }

  @Override
  public void move(int step) {
    super.move(step);

    if (Dungeon.level.map[step] == Terrain.INACTIVE_TRAP && HP < HT) {

      HP += Random.Int(1, HT - HP);
      sprite.emitter().burst(ElmoParticle.FACTORY, 5);

      if (Dungeon.visible[step] && Dungeon.hero.isAlive()) {
        GLog.n(Messages.get(this, "repair"));
      }
    }

    int[] cells = {
            step - 1, step + 1, step - Dungeon.level.width(), step + Dungeon
            .level.width(),
            step - 1 - Dungeon.level.width(),
            step - 1 + Dungeon.level.width(),
            step + 1 - Dungeon.level.width(),
            step + 1 + Dungeon.level.width()
    };
    int cell = cells[Random.Int(cells.length)];

    if (Dungeon.visible[cell]) {
      CellEmitter.get(cell).start(Speck.factory(Speck.ROCK), 0.07f, 10);
      Camera.main.shake(3, 0.7f);
      Sample.INSTANCE.play(Assets.SND_ROCKS);

      if (Level.water[cell]) {
        GameScene.ripple(cell);
      } else if (Dungeon.level.map[cell] == Terrain.EMPTY) {
        Level.set(cell, Terrain.EMPTY_DECO);
        GameScene.updateMap(cell);
      }
    }

    Char ch = Actor.findChar(cell);
    if (ch != null && ch != this) {
      Buff.prolong(ch, Paralysis.class, 2);
    }
  }

  @Override
  public int takeDamage(Damage dmg) {
    int val = super.takeDamage(dmg);

    LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
    if (lock != null && !immunizedBuffs().contains(dmg.from.getClass()))
      lock.addTime(dmg.value * 1.5f);

    return val;
  }

  @Override
  public void die(Object cause) {

    super.die(cause);

    GameScene.bossSlain();
    Dungeon.level.drop(new SkeletonKey(Dungeon.depth), pos).sprite.drop();

    Badges.validateBossSlain();

    LloydsBeacon beacon = Dungeon.hero.belongings.getItem(LloydsBeacon.class);
    if (beacon != null) {
      beacon.upgrade();
    }

    yell(Messages.get(this, "defeated"));
  }

  @Override
  public void notice() {
    super.notice();
    BossHealthBar.assignBoss(this);
    yell(Messages.get(this, "notice"));
  }

  @Override
  public Damage resistDamage(Damage dmg) {
    if (dmg.isFeatured(Damage.Feature.DEATH))
      dmg.value *= 0.5;
    if (dmg.type == Damage.Type.NORMAL)
      dmg.value *= 0.8;
    return super.resistDamage(dmg);
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

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    BossHealthBar.assignBoss(this);
  }
}
