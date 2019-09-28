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

import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.PropertyConfiger;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Charm;
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption;
import com.egoal.darkestpixeldungeon.actors.buffs.LifeLink;
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.unclassified.ArmorKit;
import com.egoal.darkestpixeldungeon.items.helmets.CrownOfDwarf;
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Grim;
import com.egoal.darkestpixeldungeon.levels.CityBossLevel;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.sprites.UndeadSprite;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.effects.Flare;
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.KingSprite;
import com.egoal.darkestpixeldungeon.ui.BossHealthBar;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.HashSet;

public class King extends Mob {

  private static final int MAX_ARMY_SIZE = 5;

  {
    spriteClass = KingSprite.class;

    PropertyConfiger.INSTANCE.set(this, "King");

    Undead.count = 0;
  }

  private boolean nextPedestal = true;

  private static final String PEDESTAL = "pedestal";

  @Override
  public int viewDistance() {
    return 6;
  }

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(PEDESTAL, nextPedestal);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    nextPedestal = bundle.getBoolean(PEDESTAL);
    BossHealthBar.assignBoss(this);
  }

  @Override
  protected boolean getCloser(int target) {
    return canTryToSummon() ?
            super.getCloser(((CityBossLevel) Dungeon.level).pedestal
                    (nextPedestal)) :
            super.getCloser(target);
  }

  @Override
  protected boolean canAttack(Char enemy) {
    return canTryToSummon() ?
            pos == ((CityBossLevel) Dungeon.level).pedestal(nextPedestal) :
            Dungeon.level.adjacent(pos, enemy.pos);
  }

  private boolean canTryToSummon() {
    if (Undead.count < maxArmySize()) {
      Char ch = Actor.findChar(((CityBossLevel) Dungeon.level).pedestal
              (nextPedestal));
      return ch == this || ch == null;
    } else {
      return false;
    }
  }

  @Override
  public boolean attack(Char enemy) {
    if (canTryToSummon() && pos == ((CityBossLevel) Dungeon.level).pedestal
            (nextPedestal)) {
      summon();
      return true;
    } else {
      if (Actor.findChar(((CityBossLevel) Dungeon.level).pedestal
              (nextPedestal)) == enemy) {
        nextPedestal = !nextPedestal;
      }
      return super.attack(enemy);
    }
  }

  @Override
  public int takeDamage(Damage dmg) {
    int val = super.takeDamage(dmg);
    LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
    if (lock != null) lock.addTime(dmg.value);

    return val;
  }

  @Override
  public void die(Object cause) {

    GameScene.bossSlain();
    Dungeon.level.drop(new ArmorKit(), pos).sprite.drop();
    Dungeon.level.drop(new SkeletonKey(Dungeon.depth), pos).sprite.drop();
    Dungeon.level.drop(new CrownOfDwarf(), pos).sprite.drop();

    super.die(cause);

    Badges.validateBossSlain();

    LloydsBeacon beacon = Dungeon.hero.getBelongings().getItem(LloydsBeacon
            .class);
    if (beacon != null) {
      beacon.upgrade();
    }

    yell(Messages.get(this, "defeated", Dungeon.hero.givenName()));
  }

  @Override
  public void aggro(Char ch) {
    super.aggro(ch);
    for (Mob mob : Dungeon.level.getMobs()) {
      if (mob instanceof Undead) {
        mob.aggro(ch);
      }
    }
  }

  private int maxArmySize() {
    return 1 + MAX_ARMY_SIZE * (HT - HP) / HT;
  }

  private void summon() {

    nextPedestal = !nextPedestal;

    sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.4f, 2);
    Sample.INSTANCE.play(Assets.SND_CHALLENGE);

    boolean[] passable = Level.Companion.getPassable().clone();
    for (Char c : Actor.chars()) {
      passable[c.pos] = false;
    }

    int undeadsToSummon = maxArmySize() - Undead.count;

    PathFinder.buildDistanceMap(pos, passable, undeadsToSummon);
    PathFinder.distance[pos] = Integer.MAX_VALUE;
    int dist = 1;

    undeadLabel:
    for (int i = 0; i < undeadsToSummon; i++) {
      do {
        for (int j = 0; j < Dungeon.level.length(); j++) {
          if (PathFinder.distance[j] == dist) {

            Undead undead = new Undead();
            undead.pos = j;
            GameScene.add(undead);
            if (buff(LifeLink.class) == null) {
              Buff.prolong(this, LifeLink.class, 10f).linker = undead.id();
            }

            ScrollOfTeleportation.Companion.appear(undead, j);
            new Flare(3, 32).color(0x000000, false).show(undead.sprite, 2f);

            PathFinder.distance[j] = Integer.MAX_VALUE;

            continue undeadLabel;
          }
        }
        dist++;
      } while (dist < undeadsToSummon);
    }

    yell(Messages.get(this, "arise"));
  }

  @Override
  public void notice() {
    super.notice();
    BossHealthBar.assignBoss(this);
    yell(Messages.get(this, "notice"));
  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(Paralysis.class);
    IMMUNITIES.add(Vertigo.class);
    IMMUNITIES.add(Corruption.class);
    IMMUNITIES.add(Terror.class);
    IMMUNITIES.add(Charm.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }

  public static class Undead extends Mob {

    public static int count = 0;

    {
      PropertyConfiger.INSTANCE.set(this, "King.Undead");

      spriteClass = UndeadSprite.class;
      state = WANDERING;
    }

    @Override
    protected void onAdd() {
      count++;
      super.onAdd();
    }

    @Override
    protected void onRemove() {
      count--;
      super.onRemove();
    }

    @Override
    public Damage attackProc(Damage damage) {
      if (Random.Float() < 0.15f) {
        Buff.prolong((Char) damage.to, Paralysis.class, 1);
      }

      return damage;
    }

    @Override
    public int takeDamage(Damage dmg) {
      int val = super.takeDamage(dmg);
      if (dmg.from instanceof ToxicGas) {
        ((ToxicGas) dmg.from).clear(pos);
      }

      return val;
    }

    @Override
    public void die(Object cause) {
      super.die(cause);

      if (Dungeon.visible[pos]) {
        Sample.INSTANCE.play(Assets.SND_BONES);
      }
    }

    private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

    static {
      IMMUNITIES.add(Grim.class);
      IMMUNITIES.add(Paralysis.class);
    }

    @Override
    public HashSet<Class<?>> immunizedBuffs() {
      return IMMUNITIES;
    }
  }
}
