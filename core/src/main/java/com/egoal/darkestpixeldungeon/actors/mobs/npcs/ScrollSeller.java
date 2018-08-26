package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import android.provider.MediaStore;

import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Stylus;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 8/26/2018.
 */

public class ScrollSeller extends DPDShopKeeper{
	{
		
	}
	
	@Override
	public DPDShopKeeper initSellItems(){
		addItemToSell( new ScrollOfIdentify());
		addItemToSell( new ScrollOfRemoveCurse() );
		int cntItems	=	Random.Int(2, 6);
		for(int i=0; i<cntItems; ++i){
			addItemToSell(Generator.random(Generator.Category.SCROLL));
		}
		
		addItemToSell(Generator.random(Generator.Category.WAND));
		if(Random.Float()<.4f)
			addItemToSell(Generator.random(Generator.Category.WAND));
		
		addItemToSell(new Stylus().identify());
		
		return this;
	}
	
}
