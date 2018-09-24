package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Light;
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor;
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.TomeOfMastery;
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon;
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.traps.DPDPrisonBossLevel;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.TenguSprite;
import com.egoal.darkestpixeldungeon.ui.BossHealthBar;
import com.egoal.darkestpixeldungeon.ui.HealthIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by 93942 on 9/16/2018.
 */

public class DPDTengu extends Mob {
  {
    spriteClass = TenguSprite.class;

    HP = HT = 120;
    EXP = 20;
    defenseSkill = 15;

    HUNTING = new Hunting();

    properties.add(Property.BOSS);
  }

  // 0: simple jump& shot
  // 1: phantom strike, jump& shot
  //todo: use ai state instead
  private int attackStage = 0;
  private HashSet<Phantom> phantoms = new HashSet<>();

  @Override
  protected boolean canAttack(Char enemy) {
    return new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos
            == enemy.pos;
  }

  @Override
  protected boolean doAttack(Char enemy) {
    if (!phantoms.isEmpty()) {
      // when hero is not adjacent to any of tengu or his phantoms, 
      // if attack directly, the phantoms perform bad
      boolean shouldFollow = !Dungeon.level.adjacent(enemy.pos, pos);
      if (!shouldFollow)
        for (Phantom p : phantoms) {
          if (!Dungeon.level.adjacent(p.pos, enemy.pos)) {
            shouldFollow = true;
            break;
          }
        }

      if (shouldFollow) {
        clearPhantoms();
        jumpPhantomAttack(enemy.pos);
        
        return true;
      }
    }

    if (enemy == Dungeon.hero)
      Dungeon.hero.resting = false;

    sprite.attack(enemy.pos);
    spend(attackDelay());

    return true;
  }

  @Override
  public Damage giveDamage(Char target) {
    return new Damage(Random.NormalIntRange(6, 20), this, target).addFeature
            (Damage.Feature.RANGED);
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    dmg.value -= Random.NormalIntRange(0, 5);

    return dmg;
  }

  @Override
  public int attackSkill(Char target) {
    return 20;
  }

  @Override
  public boolean checkHit(Damage dmg) {
    // double check for ranged damage
    boolean hit = super.checkHit(dmg);
    return (hit && dmg.isFeatured(Damage.Feature.RANGED)) ?
            super.checkHit(dmg) : hit;
  }

  @Override
  public int takeDamage(Damage dmg) {
    int val = super.takeDamage(dmg);

    // already dead, nothing to do...
    if (!isAlive()) return val;

    LockedFloor lf = Dungeon.hero.buff(LockedFloor.class);
    if (lf != null)
      lf.addTime(val * 2);  // no need to give extra duration, since the map 
    // size is limited

    int hpBracket = attackStage == 0 ? 20 : 30;
    // keep the tengu stand on the water longer
    boolean bracketExceed = HP < (HT - 40) &&
            (HP + val) / hpBracket != HP / hpBracket;

    //todo: code cleanse
    if (attackStage == 0) {
      boolean switchStage = HP < HT / 2;

      if (switchStage) {
        HP = HT / 2 - 1; // avoid directly death from healthy

        // turn off the lights and give blind
        if (((DPDPrisonBossLevel) Dungeon.level).isLighted)
          ((DPDPrisonBossLevel) Dungeon.level).turnLights(false);
        Buff.prolong(Dungeon.hero, Blindness.class, 2);

        // turn off your lights
        Light l = Dungeon.hero.buff(Light.class);
        if (l != null)
          l.detach();

        jumpAway(pos);

        Dungeon.observe();
        GameScene.flash(0x444444);
        Sample.INSTANCE.play(Assets.SND_BLAST);

        yell(Messages.get(this, "interesting"));

        attackStage = 1;

        // GLog.i("switch to phase 1.");
      } else {
        // hard attack from the face, jump away
        if (bracketExceed || (dmg.from instanceof Char &&
                Dungeon.level.adjacent(((Char) dmg.from).pos, pos) && val >
                10)) {
          jumpAway(pos);
        }
      }
    } else if (attackStage == 1) {
      if (!phantoms.isEmpty()) {
        // destroy all phantoms, jump away 
        clearPhantoms();
        
        jumpAway(pos);

        // GLog.i("destroy phantoms and jump away.");
      } else {
        // attack from face, or (attack from ranged, and bracket exceed), 
        // jump & create phantoms
        if (dmg.from instanceof Char) {
          Char c = (Char) dmg.from;

          if (Dungeon.level.adjacent(c.pos, pos) && Random.Int(4) == 0)
            jumpAway(pos);
          else {
            // ranged attack or adjacent attack but rolled
            jumpPhantomAttack(c.pos);

            // GLog.i("phantoms strike!");
          }
        } else if (bracketExceed) {
          jumpAway(pos);
        }
      }
    }

    return val;
  }

  @Override
  public Damage resistDamage(Damage dmg) {
    if (dmg.isFeatured(Damage.Feature.DEATH))
      dmg.value *= .2;
    if (dmg.type == Damage.Type.MAGICAL)
      dmg.value *= .75;

    return super.resistDamage(dmg);
  }

  @Override
  public void die(Object cause) {
    if (Dungeon.hero.subClass == HeroSubClass.NONE) {
      Dungeon.level.drop(new TomeOfMastery(), pos).sprite.drop();
    }
    Dungeon.level.drop(new SkeletonKey(Dungeon.depth), pos).sprite.drop();

    GameScene.bossSlain();
    super.die(cause);

    Badges.validateBossSlain();

    // upgrade to the beacon
    LloydsBeacon beacon = Dungeon.hero.belongings.getItem(LloydsBeacon.class);
    if (beacon != null)
      beacon.upgrade();

    yell(Messages.get(this, "defeated"));
  }

  @Override
  public void notice() {
    super.notice();

    BossHealthBar.assignBoss(this);

    yell(Messages.get(this, "notice_mine", Dungeon.hero.givenName()));
  }

  private void jumpAway(int thepos) {
    // choose a position away from pos
    //todo: maybe more likely to jump into the water
    int newpos;
    do {
      newpos = Dungeon.level.pointToCell(((DPDPrisonBossLevel) Dungeon.level)
              .rmHall.random(1));
    }
    while (Level.solid[newpos] || Dungeon.level.map[newpos] == Terrain.TRAP ||
            Dungeon.level.distance(newpos, thepos) < 6 ||
            Actor.findChar(newpos) != null);

    if (Dungeon.visible[pos])
      CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6);

    sprite.move(pos, newpos);
    move(newpos);

    if (Dungeon.visible[newpos])
      CellEmitter.get(newpos).burst(Speck.factory(Speck.WOOL), 6);

    Sample.INSTANCE.play(Assets.SND_PUFF);
    spend(1 / speed());
  }

  private void jumpPhantomAttack(int enemypos) {
    ArrayList<Integer> availables = new ArrayList<>();
    for (int i : PathFinder.NEIGHBOURS8) {
      int pos = enemypos + i;
      if (Level.passable[pos] && Dungeon.level.findMob(pos) == null)
        availables.add(pos);
    }

    // hide the bar, avoid distinguishing from which
    HealthIndicator.instance.target(null);

    if (availables.size() < 4) {
      // no space to spawn phantoms
      jumpAway(enemypos);
    } else {
      // spawn phantoms surround target
      Integer[] arr = new Integer[availables.size()];
      availables.toArray(arr);
      Random.shuffle(arr);

      // for the performance of phantom strike,
      // use random jump time cost
      // float jumptime = 1 / speed();

      // 2, 3 phantoms
      int cntphantoms = Random.Int(3)==0? 3: 2;
      for (int i = 0; i < cntphantoms; ++i) {
        Phantom p = new Phantom().imitate(this);

        p.pos = arr[i];
        p.state = p.HUNTING;

        float jumptime = Random.Float(.01f, .03f);
        GameScene.add(p, jumptime);

        phantoms.add(p);
      }

      // self
      sprite.move(pos, arr[cntphantoms]);
      sprite.turnTo(pos, enemy.pos);
      move(arr[cntphantoms]);

      float jumptime = Random.Float(.01f, .03f);
      spend(jumptime);
    }
  }

  private void clearPhantoms(){
    for(Phantom p: phantoms){
      if(p!=null && p.isAlive())
        p.die(null);
    }
    
    phantoms.clear();
  }
  
  private static final String ATTACK_STAGE = "attack_stage";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(ATTACK_STAGE, attackStage);

  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);

    attackStage = bundle.getInt(ATTACK_STAGE);

    BossHealthBar.assignBoss(this);
    if (HP <= HT / 2) BossHealthBar.bleed(true);

    for (Mob m : Dungeon.level.mobs)
      if (m instanceof Phantom)
        phantoms.add((Phantom) m);
  }

  // tengu is always hunting..
  private class Hunting extends Mob.Hunting {
    @Override
    public boolean act(boolean enemyInFov, boolean justAlerted) {
      enemySeen = enemyInFov;
      if (enemyInFov && !isCharmedBy(enemy) && canAttack(enemy)) {
        return doAttack(enemy);
      } else {
        if (enemyInFov)
          target = enemy.pos;
        else {
          chooseEnemy();
          target = enemy.pos;
        }

        spend(TICK);
        return true;
      }
    }
  }

  // tengu's skill
  static class Phantom extends Mob {
    {
      // spriteClass = TenguSprite.class;
      spriteClass = TenguSprite.Phantom.class;

      HP = HT = 1;
      EXP = 0;

      defenseSkill = 0;
      // HUNTING = new Phantom.Hunting();

      name = Messages.get(DPDTengu.class, "name");
    }

    // avoid Distinguishing by infoer.
    @Override
    public String description() {
      return Messages.get(DPDTengu.class, "desc");
    }

    public Phantom imitate(DPDTengu tengu) {
      HP = tengu.HP;
      HT = tengu.HT;

      return this;
    }

    @Override
    public int attackSkill(Char target) {
      return 10;
    }

    @Override
    public int takeDamage(Damage dmg) {
      HP = 0;
      die(dmg.from);

      //todo: add sfx
      return 0;
    }

//    private class Hunting extends Mob.Hunting {
//      @Override
//      public boolean act(boolean enemyInFov, boolean justAlerted) {
//        // do nothing...
//        if (enemy == null)
//          chooseEnemy();
//
//        sprite.turnTo(pos, enemy.pos);
//        spend(TICK);
//
//        return true;
//      }
//    }
  }
}
