package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.ResistAny;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 9/4/2018.
 */

//* buff used in Hero::resistDamage
public class RiemannianManifoldShield extends Artifact {
  {
    image = ItemSpriteSheet.RIEMANNIAN_SHIELD;

    levelCap = 10;
    exp = 0;
    cooldown = 1;

    defaultAction = "NONE"; // to put into quick slot
  }

  // called when ResistAny detached
  public void recharge() {
    // curUser is assigned in the execute method!!!
    if (isEquipped(Dungeon.hero)) {
      if (level() <= 5)
        cooldown = 45 - level() * 3;
      else
        cooldown = 30 - (level() - 5) * 3;
    }
  }

  // more likely to be cursed.
  @Override
  public Item random() {
    cursed = Random.Float() < .7f;
    return this;
  }

  @Override
  public boolean doUnequip(Hero hero, boolean collect, boolean single) {
    GLog.w(Messages.get(this, "unequipped"));
    recharge();

    return super.doUnequip(hero, collect, single);
  }

  // recharge buff
  @Override
  protected ArtifactBuff passiveBuff() {
    return new Recharge();
  }

  public class Recharge extends ArtifactBuff {
    public boolean act() {
      if (cursed) {
        spend(TICK);
        return true;
      }

      if (--cooldown == 0 && Dungeon.hero.buff(ResistAny.class) == null) {
        Buff.affect(Dungeon.hero, ResistAny.class).set(1);

        exp += 1;
        // check upgrade
        int requireExp = level() * level() + 1;
        if (exp > requireExp && level() < levelCap) {
          exp -= requireExp;
          upgrade();
          GLog.p(Messages.get(RiemannianManifoldShield.class, "levelup"));
        }
      }
      updateQuickslot();

      spend(TICK);
      return true;
    }
  }

  @Override
  public String status() {
    //display the current cooldown
    return cooldown > 0 ? Messages.format("%d", cooldown) : null;
  }

  @Override
  public String desc() {
    String desc = super.desc();

    if (isIdentified() && cursed)
      desc += "\n\n" + Messages.get(this, "desc_cursed");

    return desc;
  }

  private static final String COOLDOWN = "cooldown";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(COOLDOWN, cooldown);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    cooldown = bundle.getInt(COOLDOWN);
  }

}
