package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.items.unclassified.DemonicSkull;
import com.egoal.darkestpixeldungeon.items.unclassified.Gold;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 6/18/2018.
 */

public class DevilGhost extends Wraith {
  {
    spriteClass = DevilGhostSprite.class;

    HP = HT = 4;
    EXP = 1;

    loot = new DemonicSkull();
    lootChance = 1.f;
  }

  @Override
  public Damage giveDamage(Char target) {
    if (target instanceof Hero) {
      return new Damage(Math.min(Random.Int(level / 2) + 2, 10),
              this, target).type(Damage.Type.MENTAL);
    }

    return new Damage(Random.Int(0, 6) + level / 2, this, target).type(Damage
            .Type.MAGICAL).addFeature(Damage.Feature.ACCURATE);
  }

  @Override
  public int takeDamage(Damage dmg) {
    if (dmg.value > 0) {
      dmg.value = 1;
    }

    return super.takeDamage(dmg);
  }

  @Override
  public void adjustStats(int level) {
    this.level = level;
    defenseSkill = 10 + level / 2 * 3;
    enemySeen = true;
  }

  @Override
  protected Item createLoot() {
    if (!Dungeon.limitedDrops.demonicSkull.dropped()) {
      Dungeon.limitedDrops.demonicSkull.drop();
      return super.createLoot();
    } else
      return new Gold(Random.NormalIntRange(80, 150));
  }

  public static DevilGhost spawnAt(int pos) {
    if (Level.passable[pos] && Actor.findChar(pos) == null) {
      DevilGhost dg = new DevilGhost();
      dg.adjustStats(Dungeon.depth);
      dg.pos = pos;
      dg.state = dg.HUNTING;

      GameScene.add(dg, 1f);

      dg.sprite.alpha(0);
      dg.sprite.parent.add(new AlphaTweener(dg.sprite, 1, .5f));
      dg.sprite.emitter().burst(ShadowParticle.CURSE, 8);

      return dg;
    } else {
      return null;
    }
  }

  @Override
  public void add(Buff buff) {
    //in other words, can't be directly affected by buffs/debuffs.
  }
  
  // sprite
  public static class DevilGhostSprite extends MobSprite {

    public DevilGhostSprite() {
      super();

      texture(Assets.DEVIL_GHOST);

      TextureFilm frames = new TextureFilm(texture, 14, 24);

      idle = new Animation(10, true);
      idle.frames(frames, 0);

      run = new Animation(10, true);
      run.frames(frames, 0);

      attack = new Animation(15, false);
      attack.frames(frames, 0, 0, 0);

      die = new Animation(10, false);
      die.frames(frames, 0);

      play(idle);
    }

    @Override
    public void die() {
      super.die();

      emitter().start(ElmoParticle.FACTORY, 0.03f, 60);
    }

    @Override
    public int blood() {
      return 0xFFcccccc;
    }
  }
}
