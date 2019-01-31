package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.buffs.Bless;
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Charm;
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption;
import com.egoal.darkestpixeldungeon.actors.buffs.Ignorant;
import com.egoal.darkestpixeldungeon.actors.buffs.Light;
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor;
import com.egoal.darkestpixeldungeon.actors.buffs.MustDodge;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.unclassified.TomeOfMastery;
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon;
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.PrisonBossLevel;
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

public class Tengu extends Mob {
  {
    spriteClass = TenguSprite.class;

    HP = HT = 120;
    EXP = 20;
    defenseSkill = 15;

    HUNTING = new Hunting();

    properties.add(Property.BOSS);
    addResistances(Damage.Element.POISON, 1.25f, 1f);
  }

  // 0: simple jump& shot
  // 1: phantom strike, jump& shot
  //todo: use ai state instead
  private int attackStage = 0;
  private HashSet<Phantom> phantoms = new HashSet<>();

  @Override
  public void onAdd(){
    for(Mob m: Dungeon.level.mobs)
      if(m instanceof Phantom)
        phantoms.add((Phantom)m);
  }
  
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
    return new Damage(Random.NormalIntRange(6, 18), this, target).addFeature
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
  
  // copy from Char, so i should rework this function...
  @Override
  public boolean checkHit(Damage dmg) {
    // when from nowhere, be accurate
    if (dmg.from instanceof Mob && !Dungeon.visible[((Char) dmg.from).pos])
      dmg.addFeature(Damage.Feature.ACCURATE);

    if (dmg.isFeatured(Damage.Feature.ACCURATE))
      return true;

    MustDodge md = buff(MustDodge.class);
    if (md != null && md.canDodge(dmg))
      return false;

    Char attacker = (Char) dmg.from;
    Char defender = (Char) dmg.to;
    float acuRoll = Random.Float(attacker.attackSkill(defender));
    float defRoll = Random.Float(defender.defenseSkill(attacker));
    if(dmg.isFeatured(Damage.Feature.RANGED))
      defRoll *=  1.2;
    
    if (attacker.buffs(Bless.class) != null) acuRoll *= 1.2f;
    if (defender.buffs(Bless.class) != null) defRoll *= 1.2f;

    float bonus = 1.f;
    if (dmg.type == Damage.Type.MAGICAL || dmg.type == Damage.Type.MENTAL)
      bonus = 2f;

    return bonus * acuRoll >= defRoll;
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

    int hpBracket = attackStage == 0 ? 15 : 20;
    // keep the tengu in the water longer
    boolean bracketExceed = HP < (HT - 30) &&
            (HP + val) / hpBracket != HP / hpBracket;

    //todo: code cleanse
    if (attackStage == 0) {
      boolean switchStage = HP < HT / 2;

      if (switchStage) {
        HP = HT / 2; // avoid directly death from healthy

        // turn off the lights and give blind
        if (((PrisonBossLevel) Dungeon.level).isLighted)
          ((PrisonBossLevel) Dungeon.level).turnLights(false);
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

          if (Dungeon.level.adjacent(c.pos, pos) && Random.Int(5) == 0)
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
    clearPhantoms();
    Buff.detach(Dungeon.hero, Ignorant.class);

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
    final int JUMP_MIN_DISTANCE = 5;
    // choose a position away from pos
    int newpos = -1;

    // more likely to jump into water
    int waterpos = ((PrisonBossLevel) Dungeon.level).hallCenter();
    boolean waterokay = Dungeon.level.distance(waterpos, thepos) >= 
            JUMP_MIN_DISTANCE && Actor.findChar(waterpos) == null;
    if (waterokay) {
      // wont jump into the gas!
      for (Blob b : Dungeon.level.blobs.values()) {
        if (b.cur[waterpos] > 0) {
          waterokay = false;
          break;
        }
      }
    }

    if (waterokay && (buff(Burning.class) != null || Random.Int(6) == 0)) {
      // try jump into water  
      newpos = waterpos;
    }

    if (newpos < 0) {
      do {
        newpos = Dungeon.level.pointToCell(((PrisonBossLevel) Dungeon.level)
                .rmHall.random(1));
      }
      while (Level.solid[newpos] || Dungeon.level.map[newpos] == Terrain.TRAP ||
              Dungeon.level.distance(newpos, thepos) < JUMP_MIN_DISTANCE ||
              Actor.findChar(newpos) != null);
    }

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
      // remove all negative buffs
      for (Buff b : buffs())
        if (b != null && b.type == Buff.buffType.NEGATIVE)
          b.detach();

      // spawn phantoms surround target
      Integer[] arr = new Integer[availables.size()];
      availables.toArray(arr);
      Random.shuffle(arr);

      // for the performance of phantom strike,
      // use random jump time cost
      // float jumptime = 1 / speed();

      // 2, 3 phantoms
      int cntphantoms = Random.Int(3) == 0 ? 3 : 2;
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
      sprite.turnTo(pos, enemypos);
      move(arr[cntphantoms]);

      float jumptime = Random.Float(.01f, .03f);
      spend(jumptime);
    }
  }

  private void clearPhantoms() {
    for (Phantom p : phantoms) {
      if (p != null && p.isAlive())
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

//    for (Mob m : Dungeon.level.mobs)
//      if (m instanceof Phantom)
//        phantoms.add((Phantom) m);
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

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(Terror.class);
    IMMUNITIES.add(Corruption.class);
    IMMUNITIES.add(Charm.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }
  
  // tengu's skill
  static class Phantom extends Mob {
    {
      // spriteClass = TenguSprite.class;
      spriteClass = TenguSprite.Phantom.class;

      HP = HT = 1;
      EXP = 0;
      maxLvl = 1;

      defenseSkill = 0;
      // HUNTING = new Phantom.Hunting();

      name = Messages.get(Tengu.class, "name");
    }

    // avoid Distinguishing by infoer.
    @Override
    public String description() {
      return Messages.get(Tengu.class, "desc");
    }

    public Phantom imitate(Tengu tengu) {
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
  }
}
