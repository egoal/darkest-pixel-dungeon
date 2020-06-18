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
import com.egoal.darkestpixeldungeon.PropertyConfiger;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.Fire;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption;
import com.egoal.darkestpixeldungeon.actors.buffs.MagicalSleep;
import com.egoal.darkestpixeldungeon.actors.buffs.ViewMark;
import com.egoal.darkestpixeldungeon.effects.Pushing;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Grim;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.actors.buffs.Amok;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Charm;
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor;
import com.egoal.darkestpixeldungeon.actors.buffs.Ooze;
import com.egoal.darkestpixeldungeon.actors.buffs.Poison;
import com.egoal.darkestpixeldungeon.actors.buffs.Sleep;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo;
import com.egoal.darkestpixeldungeon.sprites.BurningFistSprite;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.LarvaSprite;
import com.egoal.darkestpixeldungeon.sprites.RottingFistSprite;
import com.egoal.darkestpixeldungeon.sprites.YogSprite;
import com.egoal.darkestpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class Yog extends Mob {

  {
    PropertyConfiger.INSTANCE.set(this, "Yog");

    spriteClass = YogSprite.class;

    state = PASSIVE;
  }

  public Yog() {
    super();
  }

  public void spawnFists() {
    RottingFist fist1 = new RottingFist();
    BurningFist fist2 = new BurningFist();

    do {
      fist1.pos = pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
      fist2.pos = pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
    }
    while (!Level.Companion.getPassable()[fist1.pos] || !Level.Companion.getPassable()[fist2.pos] ||
            fist1.pos == fist2.pos);

    GameScene.add(fist1);
    GameScene.add(fist2);

    notice();
  }

  @Override
  protected boolean act() {
    //heals 1 health per turn
    HP = Math.min(HT, HP + 1);

    return super.act();
  }

  @Override
  public int takeDamage(Damage dmg) {
    HashSet<Mob> fists = new HashSet<>();

    for (Mob mob : Dungeon.level.getMobs())
      if (mob instanceof RottingFist || mob instanceof BurningFist)
        fists.add(mob);

    for (Mob fist : fists)
      fist.beckon(pos);

    dmg.value >>= fists.size();

    int val = super.takeDamage(dmg);


    LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
    if (lock != null) lock.addTime(dmg.value * 0.5f);

    return val;
  }

  @Override
  public Damage defenseProc(Damage damage) {
    Char enemy = (Char) damage.from;

    ArrayList<Integer> spawnPoints = new ArrayList<>();

    for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
      int p = pos + PathFinder.NEIGHBOURS8[i];
      if (Actor.findChar(p) == null && (Level.Companion.getPassable()[p] || Level.Companion.getAvoid()[p])) {
        spawnPoints.add(p);
      }
    }

    if (spawnPoints.size() > 0) {
      Larva larva = new Larva();
      larva.pos = Random.element(spawnPoints);

      GameScene.add(larva);
      Actor.addDelayed(new Pushing(larva, pos, larva.pos), -1);
    }

    for (Mob mob : Dungeon.level.getMobs()) {
      if (mob instanceof BurningFist || mob instanceof RottingFist || mob
              instanceof Larva) {
        mob.aggro(enemy);
      }
    }

    return super.defenseProc(damage);
  }

  @Override
  public void beckon(int cell) {
  }

  @SuppressWarnings("unchecked")
  @Override
  public void die(Object cause) {

    // remove view mark
    Buff.detach(Dungeon.hero, ViewMark.class);

    for (Mob mob : (Iterable<Mob>) Dungeon.level.getMobs().clone()) {
      if (mob instanceof BurningFist || mob instanceof RottingFist) {
        mob.die(cause);
      }
    }

    GameScene.bossSlain();
    Dungeon.level.drop(new SkeletonKey(Dungeon.depth), pos).getSprite().drop();
    super.die(cause);

    yell(Messages.get(this, "defeated"));
  }

  @Override
  public void notice() {
    super.notice();
    BossHealthBar.assignBoss(this);
    yell(Messages.get(this, "notice"));
  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {

    IMMUNITIES.add(Grim.class);
    IMMUNITIES.add(Terror.class);
    IMMUNITIES.add(Amok.class);
    IMMUNITIES.add(Charm.class);
    IMMUNITIES.add(Sleep.class);
    IMMUNITIES.add(Burning.class);
    IMMUNITIES.add(ToxicGas.class);
    IMMUNITIES.add(Vertigo.class);
    IMMUNITIES.add(Corruption.class);
    IMMUNITIES.add(MagicalSleep.class);
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

  public static class RottingFist extends Mob {

    private static final int REGENERATION = 4;

    {
      PropertyConfiger.INSTANCE.set(this, "Yog.RottingFist");

      spriteClass = RottingFistSprite.class;

      state = WANDERING;

      properties.add(Property.BOSS);
      properties.add(Property.DEMONIC);

      addResistances(Damage.Element.POISON, 0.2f);
      addResistances(Damage.Element.HOLY, -0.25f);
    }

    @Override
    public Damage attackProc(Damage damage) {
      Char enemy = (Char) damage.to;
      if (Random.Int(3) == 0) {
        Buff.affect(enemy, Ooze.class);
        enemy.sprite.burst(0xFF000000, 5);
      }

      return damage;
    }

    @Override
    public boolean act() {

      if (Level.Companion.getWater()[pos] && HP < HT) {
        sprite.emitter().burst(ShadowParticle.UP, 2);
        HP += REGENERATION;
      }

      // eyed, share vision with yog
      // code related to Mob::act, but no need to update field of view, 
      boolean justAlerted = alerted;
      alerted = false;

      sprite.hideAlert();

      if (paralysed > 0) {
        enemySeen = false;
        spend(TICK);
        return true;
      }

      enemy = chooseEnemy();
      boolean enemyInFOV = enemy != null && enemy.isAlive() &&
              enemy.invisible <= 0;

      return state.act(enemyInFOV, justAlerted);
    }

    @Override
    public int takeDamage(Damage dmg) {
      int val = super.takeDamage(dmg);
      LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
      if (lock != null) lock.addTime(dmg.value * 0.5f);

      return val;
    }

    private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

    static {
      IMMUNITIES.add(Amok.class);
      IMMUNITIES.add(Sleep.class);
      IMMUNITIES.add(Terror.class);
      IMMUNITIES.add(Poison.class);
      IMMUNITIES.add(Vertigo.class);
      IMMUNITIES.add(Corruption.class);
      IMMUNITIES.add(MagicalSleep.class);
    }

    @Override
    public HashSet<Class<?>> immunizedBuffs() {
      return IMMUNITIES;
    }

  }

  public static class BurningFist extends Mob {

    {
      PropertyConfiger.INSTANCE.set(this, "Yog.BurningFist");

      spriteClass = BurningFistSprite.class;
      state = WANDERING;
    }

    @Override
    protected boolean canAttack(Char enemy) {
      return new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT)
              .collisionPos == enemy.pos;
    }

    @Override
    public boolean attack(Char enemy) {

      if (!Dungeon.level.adjacent(pos, enemy.pos)) {
        spend(attackDelay());

        Damage dmg = giveDamage(enemy).type(Damage.Type.MAGICAL).addElement
                (Damage.Element.FIRE);
        if (enemy.checkHit(dmg)) {

          enemy.takeDamage(dmg);

          enemy.sprite.bloodBurstA(sprite.center(), dmg.value);
          enemy.sprite.flash();

          if (!enemy.isAlive() && enemy == Dungeon.hero) {
            Dungeon.fail(getClass());
            GLog.n(Messages.get(Char.class, "kill", name));
          }
          return true;

        } else {

          enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
          return false;
        }
      } else {
        return super.attack(enemy);
      }
    }

    @Override
    public boolean act() {

      for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
        GameScene.add(Blob.seed(pos + PathFinder.NEIGHBOURS9[i], 2, Fire
                .class));
      }

      // code related to RottingFist::act
      boolean justAlerted = alerted;
      alerted = false;

      sprite.hideAlert();

      if (paralysed > 0) {
        enemySeen = false;
        spend(TICK);
        return true;
      }

      enemy = chooseEnemy();
      boolean enemyInFOV = enemy != null && enemy.isAlive() &&
              enemy.invisible <= 0;

      return state.act(enemyInFOV, justAlerted);
    }

    @Override
    public int takeDamage(Damage dmg) {
      int val = super.takeDamage(dmg);
      LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
      if (lock != null) lock.addTime(dmg.value * 0.5f);

      return val;
    }


    private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

    static {
      IMMUNITIES.add(Amok.class);
      IMMUNITIES.add(Sleep.class);
      IMMUNITIES.add(Terror.class);
      IMMUNITIES.add(Burning.class);
      IMMUNITIES.add(Vertigo.class);
      IMMUNITIES.add(Corruption.class);
      IMMUNITIES.add(MagicalSleep.class);
    }

    @Override
    public HashSet<Class<?>> immunizedBuffs() {
      return IMMUNITIES;
    }
  }

  public static class Larva extends Mob {
    {
      PropertyConfiger.INSTANCE.set(this, "Yog.Larva");

      spriteClass = LarvaSprite.class;
      state = HUNTING;
    }
  }
}
