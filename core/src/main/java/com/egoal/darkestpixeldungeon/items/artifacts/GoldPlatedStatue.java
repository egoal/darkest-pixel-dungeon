package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * Created by 93942 on 9/21/2018.
 */

// check Gold::doPickUp
public class GoldPlatedStatue extends Artifact {
  {
    image = ItemSpriteSheet.GOLD_PLATE_STATUE;

    levelCap = 10;
  }

  private static final String AC_INVEST = "INVEST";

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    if (level() < levelCap && !cursed)
      actions.add(AC_INVEST);

    return actions;
  }

  @Override
  public void execute(final Hero hero, String action) {
    super.execute(hero, action);
    if (action.equals(AC_INVEST) && level() < levelCap) {
      if (!isEquipped(hero))
        GLog.i(Messages.get(Artifact.class, "need_to_equip"));
      else {
        int goldRequired = (int) (100 * Math.pow(1.27, level()));
        if (Dungeon.gold < goldRequired)
          GLog.w(Messages.get(GoldPlatedStatue.class, "no_enough_gold",
                  goldRequired));
        else {
          Dungeon.gold -= goldRequired;

          upgrade();
          GLog.p(Messages.get(GoldPlatedStatue.class, "levelup", goldRequired));
        }
      }
    }
  }

  @Override
  public String desc() {
    String desc = super.desc();

    if (isEquipped(Dungeon.hero)) {
      if (!cursed) {
        if (level() < levelCap)
          desc += "\n\n" + Messages.get(this, "desc_hint");
      } else {
        desc += "\n\n" + Messages.get(this, "desc_cursed");
      }
    }

    return desc;
  }

  @Override
  protected ArtifactBuff passiveBuff() {
    return new Greedy();
  }

  public class Greedy extends ArtifactBuff {
    public int extraCollect(int gold) {
      float ratio = cursed ? -.3f : level() * .1f;

      return (int) (gold * ratio);
    }

  }
}
