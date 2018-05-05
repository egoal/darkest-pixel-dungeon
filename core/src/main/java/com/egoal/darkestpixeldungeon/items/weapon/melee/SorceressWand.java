package com.egoal.darkestpixeldungeon.items.weapon.melee;

import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Unstable;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Image;

/**
 * Created by 93942 on 4/23/2018.
 */

public class SorceressWand extends MeleeWeapon{
	
	{
		image	=	ItemSpriteSheet.DPD_SORCERESS_WAND;
		tier	=	1;
		DLY	=	1.f;
		unique	=	true;
		
	}
	
	public SorceressWand(){
		// give enchantment
		enchant(new Unstable());
	}
}
