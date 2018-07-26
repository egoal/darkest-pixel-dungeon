package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;

/**
 * Created by 93942 on 7/24/2018.
 */

public class MaskOfMadness extends Artifact{
	{
		image	=	ItemSpriteSheet.DPD_MASK_OF_MADNESS;
		unique	=	true;
	}

	@Override
	public boolean isUpgradable(){ return false; }
	
	@Override
	protected ArtifactBuff passiveBuff(){ return new Madness(); }
	
	public class Madness extends ArtifactBuff{
		
	}
	
}
