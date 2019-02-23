package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * Created by 93942 on 8/3/2018.
 */

//* check in Char::takeDamage
public class LifeLink extends FlavourBuff {
  {
    type = buffType.POSITIVE;
  }
  
  public int linker = 0;

  private static final String LINKER = "linker";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(LINKER, linker);

  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    linker = bundle.getInt(LINKER);
  }

  @Override
  public int icon() {
    return BuffIndicator.LIFE_LINK;
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc", dispTurns());
  }
}
