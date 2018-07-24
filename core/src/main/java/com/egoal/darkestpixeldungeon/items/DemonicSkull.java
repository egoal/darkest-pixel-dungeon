package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

/**
 * Created by 93942 on 6/18/2018.
 */

public class DemonicSkull extends Item{
	
	private static final String AC_SMEAR	=	"SMEAR";
	
	{
		image	=	ItemSpriteSheet.DPD_DEMONIC_SKULL;
		unique	=	true;
	}
	
	@Override
	public boolean isUpgradable(){ return false; }
	
	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions	=	super.actions(hero);
		UnholyBlood ub	=	hero.belongings.getItem(UnholyBlood.class);
		if(ub!=null){
			actions.add(AC_SMEAR);
		}
		
		return actions;
	}
	
	@Override
	public void execute(final Hero hero, String action){
		super.execute(hero, action);
		
		if(action==AC_SMEAR){
			MaskOfMadness mom	=	new MaskOfMadness();
			mom.identify();
			if(!mom.doPickUp(hero)){
				Dungeon.level.drop(mom, hero.pos);
			}
			
			detach(hero.belongings.backpack);
			hero.belongings.getItem(UnholyBlood.class).detach(hero.belongings.backpack);
		}
	}
}
