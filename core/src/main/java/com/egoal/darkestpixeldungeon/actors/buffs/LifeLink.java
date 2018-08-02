package com.egoal.darkestpixeldungeon.actors.buffs;

import com.watabou.utils.Bundle;

/**
 * Created by 93942 on 8/3/2018.
 */

//* check in Char::takeDamage
public class LifeLink extends FlavourBuff{
	public int linker	=	0;
	
	private static final String LINKER	=	"linker";
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LINKER, linker );

	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		linker	=	bundle.getInt( LINKER );
	}
}
