package com.egoal.darkestpixeldungeon.actors.buffs;


/**
 * Created by 93942 on 5/9/2018.
 */

public class Dementage extends Corruption{
	
	public boolean act(){
		spend(TICK);
		return true;	
	}
	
}
