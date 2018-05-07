package com.egoal.darkestpixeldungeon.levels;

/**
 * Created by 93942 on 5/7/2018.
 */

public class DPDCavesLevel extends RegularLevel{
	
	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;

		viewDistance = 3;
	}
	
	@Override
	protected boolean[] water(){
		return new boolean[0];
	}

	@Override
	protected boolean[] grass(){
		return new boolean[0];
	}

	@Override
	protected void decorate(){

	}
}
