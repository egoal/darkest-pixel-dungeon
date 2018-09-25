package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;

/**
 * Created by 93942 on 9/25/2018.
 */

// check in Char::takeDamage
public class Ignorant extends FlavourBuff{
  {
    type  = buffType.NEGATIVE;
  }
  
  @Override
  public int icon(){
    return BuffIndicator.IGNORANT;
  }
  
  @Override
  public String toString(){ return Messages.get(this, "name"); }
  
  @Override
  public String desc(){ return Messages.get(this, "desc", dispTurns()); }
}
