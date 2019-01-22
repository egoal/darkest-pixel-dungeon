package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHighlyToxicGas;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfParalyticGas;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfToxicGas;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;

import javax.microedition.khronos.opengles.GL;
import java.util.ArrayList;

public class PotionTestPaper extends Item {

  private static final float TIME_TO_TEST = 1;
  private static final String AC_TEST = "TEST";

  {
    image = ItemSpriteSheet.DPD_TEST_PAPER;
    unique = false;

    defaultAction = AC_TEST;
    stackable = true;
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    actions.add(AC_TEST);
    return actions;
  }

  @Override
  public void execute(Hero hero, String action) {
    super.execute(hero, action);

    if (action == AC_TEST) {
      curUser = hero;
      GameScene.selectItem(itemSelector, WndBag.Mode.POTION, Messages.get
              (this, "prompt"));
    }
  }

  @Override
  public boolean isUpgradable() {
    return false;
  }

  @Override
  public boolean isIdentified() {
    return true;
  }

  @Override
  public int price() {
    return 12 * quantity;
  }

  private void test(Item item) {
    if (item.isIdentified()) {
      GLog.i(Messages.get(this, "tip"));
    } else {
      // try test and identify
      detach(curUser.belongings.backpack);
      if (item instanceof PotionOfLiquidFlame || item instanceof 
              PotionOfToxicGas ||
              item instanceof PotionOfParalyticGas || item instanceof 
              PotionOfHighlyToxicGas) {
        // item.identify();
        GLog.w(Messages.get(this, "test_succeed", item.name()));
      } else {
        GLog.i(Messages.get(this, "test_failed"));
      }

      curUser.sprite.operate(curUser.pos);
      curUser.spend(TIME_TO_TEST);
      curUser.busy();
    }
  }

  private final WndBag.Listener itemSelector = new WndBag.Listener() {
    @Override
    public void onSelect(Item item) {
      if (item != null) {
        test(item);
      }
    }
  };
}
