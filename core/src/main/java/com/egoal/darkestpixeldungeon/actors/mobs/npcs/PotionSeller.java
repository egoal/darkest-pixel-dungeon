package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.PotionTestPaper;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 8/26/2018.
 */

public class PotionSeller extends DPDShopKeeper{
	
	{
		//todo: sprite class
	}
	
	@Override
	public DPDShopKeeper initSellItems(){
		// potions
		addItemToSell(new PotionOfHealing());
		int cntItems	=	Random.Int(4, 8);
		for(int i=0; i<cntItems; ++i){
			Generator.Category c	=	Random.Float()<.7f? Generator.Category.POTION:
				Generator.Category.SEED;
			Item item	=	Generator.random(c);
			// may be reinforced
			if(item instanceof Potion){
				Potion p	=	(Potion)item;
				if(p.canBeReinforced()&&Random.Float()<.3f){
					p.reinforce();
				}
			}
			addItemToSell(item);
		}
		
		addItemToSell(new PotionTestPaper().quantity(Random.Int(1, 3)));
		
		return this;
	}
	
}
