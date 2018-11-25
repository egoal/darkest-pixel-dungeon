package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;

/**
 * Created by 93942 on 10/20/2018.
 */

// see Char::checkHit
public class Shock extends FlavourBuff{

  {
    type = buffType.NEGATIVE;
  }
  
  @Override
  public int icon() {
    return BuffIndicator.SHOCK;
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
