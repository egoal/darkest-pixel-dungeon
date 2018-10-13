package com.egoal.darkestpixeldungeon.items.weapon.enchantments;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;

/**
 * Created by 93942 on 10/13/2018.
 */

public class Suppress extends Weapon.Enchantment {
  private static ItemSprite.Glowing GREY = new ItemSprite.Glowing(0x444444);

  @Override
  public Damage proc(Weapon weapon, Damage damage) {
    if (damage.to instanceof Char) {
      float pm = 1f - ((Char) damage.to).HP / (float) (((Char) damage.to).HT);
      int level = Math.max(0, weapon.level());
      if(level>0)
        damage.value  *= (1f+Math.pow(pm, level>3? 2: 3));
    }

    return damage;
  }

  @Override
  public ItemSprite.Glowing glowing() {
    return GREY;
  }

}
