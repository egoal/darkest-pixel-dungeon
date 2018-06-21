package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;

/**
 * Created by 93942 on 6/18/2018.
 */

public class DemonicSkull extends Item{
	{
		image	=	ItemSpriteSheet.DPD_URN_OF_SHADOW;
		unique	=	true;
	}
	
	@Override
	public boolean isUpgradable(){ return false; }
	
	
}
