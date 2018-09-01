package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;

/**
 * Created by 93942 on 7/24/2018.
 */

public class UnholyBlood extends Item {
  {
    image = ItemSpriteSheet.DPD_UNHOLY_BLOOD;
    unique = true;
  }

  @Override
  public boolean isUpgradable() {
    return false;
  }

}
