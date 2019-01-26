package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;

/**
 * Created by 93942 on 7/24/2018.
 */

public class MaskOfMadness extends Artifact {
  {
    image = ItemSpriteSheet.MASK_OF_MADNESS;
    unique = true;

    levelCap = 10;
  }

  @Override
  public boolean isUpgradable() {
    return false;
  }

  @Override
  public boolean doUnequip(Hero hero, boolean collect, boolean single) {
    // cannot unequip
    GLog.n(Messages.get(this, "cannot_unequip"));
    return false;
  }

  @Override
  public int price() {
    return 0;
  }

  @Override
  protected ArtifactBuff passiveBuff() {
    return new Madness();
  }

  public class Madness extends ArtifactBuff {

    public Damage procIncomingDamage(Damage dmg) {
      float ratio = dmg.type == Damage.Type.MENTAL ? 1.75f :
              (1.8f - 1.5f / ((float) Math.exp(level() / 3f) + 1f) + 0.05f *
                      level());

      dmg.value *= ratio;

      return dmg;
    }

    public Damage procOutcomingDamage(Damage dmg) {
      // float ratio = 1.8f - 1.5f / ((float) Math.exp(level() / 3f) + 1f);
      float ratio = 2f - 1.5f / ((float) Math.exp(level() / 3f) + .5f);
      dmg.value *= ratio;

      return dmg;
    }

    public void onEmenySlayed(Char e) {
      exp += e.properties().contains(Char.Property.BOSS) ? 3 : 1;
      if (exp >= level() * 2 && level() < levelCap) {
        exp -= level() * 2;

        upgrade();
        GLog.p(Messages.get(MaskOfMadness.class, "levelup"));
      }
    }
  }

}
