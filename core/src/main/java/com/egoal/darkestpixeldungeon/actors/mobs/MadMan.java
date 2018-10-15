package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.egoal.darkestpixeldungeon.items.food.Humanity;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.MobSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 8/18/2018.
 */

public class MadMan extends Mob implements Callback {
  {
    spriteClass = Sprite.class;

    HP = HT = 10 + 2 * Dungeon.depth;
    defenseSkill = 5 + Dungeon.depth;

    EXP = Math.min(3 + Dungeon.depth / 2, 12);
    maxLvl = Dungeon.depth + 3;

    loot = new Humanity();
    lootChance = 0.2f;

    addResistances(Damage.Element.SHADOW, 2f);
    addResistances(Damage.Element.HOLY, .5f);
  }

  private static final float TIME_TO_SHOUT = 1f;
  private static final int SHOUT_RANGE = 3;

  @Override
  public Damage giveDamage(Char target) {
    if (target instanceof Hero) {
      int dis = Dungeon.level.distance(pos, enemy.pos);
      return new Damage(Random.Int(4 - dis, 6 - dis), this, target).type
              (Damage.Type.MENTAL).addFeature(Damage.Feature.ACCURATE);
    } else
      return new Damage(1, this, target).addFeature(Damage.Feature.PURE)
              .addFeature(Damage
                      .Feature.ACCURATE);
  }

  @Override
  protected float attackDelay(){
    return 1.25f;
  }
  
  @Override
  public int attackSkill(Char target) {
    return 10 + Dungeon.depth;
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    // lower physical defense, 
    // magic resistance is really high
    if (dmg.type == Damage.Type.NORMAL)
      dmg.value -= Random.NormalIntRange(0, Dungeon.depth / 5 * 3);

    return dmg;
  }

  @Override
  protected boolean canAttack(Char enemy) {
    if (enemy instanceof Hero)
      return Dungeon.level.distance(pos, enemy.pos) <= SHOUT_RANGE &&
              new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT)
                      .collisionPos == enemy.pos;

    return super.canAttack(enemy);
  }

  @Override
  protected boolean doAttack(Char enemy) {
    boolean visible = Level.fieldOfView[pos] || Level.fieldOfView[enemy.pos];

    if (visible) {
      sprite.zap(enemy.pos);
    } else {
      shout();
    }

    return !visible;
  }

  private void shout() {
    // shout!
    spend(TIME_TO_SHOUT);

    Damage dmg = giveDamage(enemy);
    if (enemy.checkHit(dmg)) {
      enemy.takeDamage(dmg);
      if (!enemy.isAlive() && enemy == Dungeon.hero) {
        Dungeon.fail(getClass());
        GLog.n(Messages.get(this, "shout_kill"));
      }
    } else
      enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
  }

  private void onZapComplete() {
    // called when zap animation completed
    shout();
    next();
  }

  @Override
  public void call() {
    next();
  }

  // sprite
  public static class Sprite extends MobSprite {

    public Sprite() {
      super();

      texture(Assets.MADMAN);

      TextureFilm frames = new TextureFilm(texture, 12, 14);

      idle = new Animation(8, true);
      idle.frames(frames, 0, 0, 0, 0, 0, 0, 1, 1, 2);

      run = new Animation(8, true);
      run.frames(frames, 3, 4, 5, 6);

      attack = new Animation(8, false);
      attack.frames(frames, 10, 11, 12);

      zap = attack.clone();

      die = new Animation(8, false);
      die.frames(frames, 7, 8, 9);

      play(idle);
    }

    @Override
    public void zap(int cell) {
      turnTo(ch.pos, cell);
      play(zap);

      MagicMissile.shadow(parent, ch.pos, cell, new Callback() {
        @Override
        public void call() {
          ((MadMan) ch).onZapComplete();
        }
      });
      Sample.INSTANCE.play(Assets.SND_BADGE);
    }

    @Override
    public void onComplete(Animation anim) {
      if (anim == zap)
        idle();
      super.onComplete(anim);
    }
  }

}
