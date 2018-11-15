package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;

/**
 * Created by 93942 on 9/24/2018.
 */

public class CrownOfDwarf extends Item {
  {
    image = ItemSpriteSheet.DPD_DWARF_CROWN;
  }

  public CrownOfDwarf() {
    super();
    identify();
  }

  @Override
  public boolean isUpgradable() {
    return false;
  }
  
  @Override
  public int price(){ return 750*quantity(); }

}
