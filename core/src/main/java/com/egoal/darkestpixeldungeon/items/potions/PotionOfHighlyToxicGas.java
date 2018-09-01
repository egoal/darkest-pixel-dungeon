package com.egoal.darkestpixeldungeon.items.potions;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.HighlyToxicGas;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Poison;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 4/25/2018.
 */

public class PotionOfHighlyToxicGas extends Potion {
  {
    initials = 12;
  }

  @Override
  public void shatter(int cell) {
    if (Dungeon.visible[cell]) {
      setKnown();

      splash(cell);
      Sample.INSTANCE.play(Assets.SND_SHATTER);
    }

    for (int offset : PathFinder.NEIGHBOURS9) {
      Mob mob = Dungeon.level.findMob(cell + offset);
      if (mob != null) {
        affectChar(mob);
      }
    }
    if (Dungeon.level.distance(curUser.pos, cell) <= 1) {
      affectChar(curUser);
    }
    // longer, sharper
    // GameScene.add( Blob.seed( cell, 1200, HighlyToxicGas.class));
  }

  private void affectChar(Char c) {
    Poison p = Buff.affect(c, Poison.class);
    p.set(Random.Int(6, 10));
    p.addDamage(Dungeon.depth / 2 + Random.Int(1, 4));
  }

  @Override
  public void reset() {
    image = ItemSpriteSheet.DPD_HIGHLY_TOXIC_POTION;
    color = "drakgreen";
  }

  @Override
  protected void drink(Hero hero) {
    super.drink(hero);
    affectChar(hero);
  }

  public boolean isKnown() {
    return true;
  }

  public void setKnown() {
  }
}
