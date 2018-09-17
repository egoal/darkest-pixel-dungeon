package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass;
import com.egoal.darkestpixeldungeon.items.TomeOfMastery;
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon;
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey;
import com.egoal.darkestpixeldungeon.levels.PrisonBossLevel;
import com.egoal.darkestpixeldungeon.levels.traps.DPDPrisonBossLevel;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.TenguSprite;
import com.egoal.darkestpixeldungeon.ui.BossHealthBar;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 9/16/2018.
 */

public class DPDTengu extends Mob {
  {
    spriteClass = TenguSprite.class;

    HP = HT = 120;
    EXP = 20;
    defenseSkill = 20;

    HUNTING = new Hunting();

    properties.add(Property.BOSS);

    addResistances(Damage.Element.POISON, 1.5f, 1.25f);
  }

  @Override
  protected boolean canAttack(Char enemy) {
    return new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos
            == enemy.pos;
  }

  @Override
  protected boolean doAttack(Char enemy) {
    if (enemy == Dungeon.hero)
      Dungeon.hero.resting = false;

    sprite.attack(enemy.pos);
    spend(attackDelay());

    return true;
  }

  @Override
  public Damage giveDamage(Char target) {
    return new Damage(Random.NormalIntRange(0, 2), this, target).addFeature
            (Damage.Feature.RANGED);
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    dmg.value -= Random.NormalIntRange(0, 5);

    if (dmg.isFeatured(Damage.Feature.RANGED))
      dmg.value -= 3;

    return dmg;
  }

  @Override
  public int attackSkill(Char target) {
    return 20;
  }

  @Override
  public int takeDamage(Damage dmg) {
    int val = super.takeDamage(dmg);

    if (((DPDPrisonBossLevel) Dungeon.level).isLighted && HP < HT / 2) {
      // turn off the lights and give blind
      ((DPDPrisonBossLevel) Dungeon.level).turnLights(false);
      Buff.prolong(Dungeon.hero, Blindness.class, 4);
      
      Dungeon.observe();
      GameScene.flash(0x444444);
      Sample.INSTANCE.play(Assets.SND_BLAST);      
      
      yell(Messages.get(this, "interesting"));
    }

    return val;
  }

  @Override
  public Damage resistDamage(Damage dmg) {
    if (dmg.isFeatured(Damage.Feature.DEATH))
      dmg.value *= .1;
    else if (dmg.type == Damage.Type.MAGICAL)
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

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    BossHealthBar.assignBoss(this);
    if (HP <= HT / 2) BossHealthBar.bleed(true);
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
}
