package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.artifacts.RiemannianManifoldShield;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * Created by 93942 on 9/4/2018.
 */

//* check in Char::resistDamage
public class ResistAny extends Buff {
  {
    type = buffType.POSITIVE;
  }

  public int resistCount = 1;
  
  public ResistAny set(int resist) {
    resistCount = resist;
    return this;
  }

  public void resist() {
    if (--resistCount <= 0) {
      // would detach, check rms
      if(target instanceof Hero){
        for(Item item: ((Hero) target).belongings.equippedItems())
          if(item instanceof RiemannianManifoldShield)
            ((RiemannianManifoldShield) item).recharge();
      }
      detach();
    }
  }

  @Override
  public int icon() {
    return BuffIndicator.RESIST_ANY;
  }
  
  @Override
  public String toString(){
    return Messages.get(this, "name");
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc", resistCount);
  }

  private static final String RESIST_COUNT  = "resist_count";
  @Override
  public void storeInBundle(Bundle bundle){
    super.storeInBundle(bundle);
    bundle.put(RESIST_COUNT, resistCount);
  }
  
  @Override
  public void restoreFromBundle(Bundle bundle){
    super.restoreFromBundle(bundle);
    resistCount = bundle.getInt(RESIST_COUNT);
  }
}
