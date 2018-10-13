package com.egoal.darkestpixeldungeon.items.weapon.curses;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 10/13/2018.
 */

public class Arrogant extends Weapon.Enchantment{

  private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing(0x000000);

  @Override
  public Damage proc(Weapon weapon, Damage damage) {
    if(Random.Int(10)==0){
      Pressure p = ((Char)damage.from).buff(Pressure.class);
      if(p!=null)
        p.upPressure(Random.Int(1, 3));
    }
    
    return damage;
  }
  
  @Override
  public boolean curse() {
    return true;
  }

  @Override
  public ItemSprite.Glowing glowing() {
    return BLACK;
  }
}
