package com.egoal.darkestpixeldungeon.items.weapon.melee;

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;

/**
 * Created by 93942 on 8/16/2018.
 */

public class BattleGloves extends MeleeWeapon {

  {
    image = ItemSpriteSheet.DPD_BATTLE_GLOVES;

    tier = 1;
    DLY = .75f;  // faster speed
    // ACC		=	1.2f;	// 20% boost to accuracy
  }

  // 3+2*x
  @Override
  public int max(int lvl) {
    // very low damage
    return 3 * (tier) + lvl * (tier + 1);
  }

  @Override
  public int STRReq(int lvl) {
    lvl = Math.max(0, lvl);
    return (6 + tier * 2) - (int) (Math.sqrt(8 * lvl + 1) - 1) / 2;
  }
}
