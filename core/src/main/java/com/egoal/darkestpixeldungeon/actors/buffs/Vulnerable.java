package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;

/**
 * Created by 93942 on 8/3/2018.
 */

// check in Char::takeDamage
public class Vulnerable extends FlavourBuff{
	{
		type	=	buffType.NEGATIVE;
	}
	
	public static final float DURATION	=	10f;
	
	public float ratio	=	1.f;
	
	@Override
	public int icon(){ return BuffIndicator.VULERABLE; }

	@Override
	public String toString(){ return Messages.get(this, "name"); }

	@Override
	public String desc(){ return Messages.get(this, ratio<1.f? "desc_1": "desc_0", ratio, dispTurns()); }
	
}
