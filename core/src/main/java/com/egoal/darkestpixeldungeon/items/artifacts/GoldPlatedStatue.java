package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.quest.CorpseDust;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * Created by 93942 on 9/21/2018.
 */

// check Gold::doPickUp
public class GoldPlatedStatue extends Artifact {
  {
    image = ItemSpriteSheet.DPD_GOLD_PLATE_STATUE;

    levelCap = 10;
    defaultAction = AC_INVEST;
  }

  private static final String AC_INVEST = "INVEST";

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    if (level() < levelCap)
      actions.add(AC_INVEST);

    return actions;
  }

  @Override
  public void execute(final Hero hero, String action) {
    super.execute(hero, action);
    if (action.equals(AC_INVEST) && level()<levelCap) {
      int goldRequired = (level() + 1) * 100;
      if (Dungeon.gold < goldRequired)
        GLog.w(Messages.get(GoldPlatedStatue.class, "no_enough_gold"));
      else {
        Dungeon.gold -= goldRequired;

        upgrade();
        GLog.p(Messages.get(GoldPlatedStatue.class, "levelup", goldRequired));
      }
    }
  }

  @Override
  protected ArtifactBuff passiveBuff() {
    return new Greedy();
  }

  public class Greedy extends ArtifactBuff {
    public int extraCollect(int gold) {
      float ratio = cursed? -.3f: level() * .1f;
      
      return (int) (gold * ratio);
    }

  }
}
