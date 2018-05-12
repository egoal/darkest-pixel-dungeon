package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * Created by 93942 on 5/12/2018.
 */

public class ViewMark extends FlavourBuff{
	
	public int observer	=	0;

	private static final String OBSERVER    = "observer";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( OBSERVER, observer );

	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		observer	=	bundle.getInt( OBSERVER );
	}
	
	@Override
	public int icon(){ return BuffIndicator.STARE; }

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}
	
}
