package com.egoal.darkestpixeldungeon.items.food;

import com.egoal.darkestpixeldungeon.actors.buffs.Pressure;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * Created by 93942 on 5/13/2018.
 */

public class Humanity extends Item{
	private static final String AC_CONSUME	=	"CONSUME";
	
	private static final float TIME_TO_CONSUME	=	1f;
	
	{
		image	=	ItemSpriteSheet.DPD_HUMANITY;
		defaultAction	=	AC_CONSUME;
		
		stackable	=	true;
	}
	
	public Humanity(){
		super();
		identify();
	}
	
	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions	=	super.actions(hero);
		actions.add(AC_CONSUME);
		
		return actions;
	}
	
	@Override
	public void execute(final Hero hero, String action){
		super.execute(hero, action);
		
		if(action==AC_CONSUME){
			//todo: add effects
			
			//0. detach
			detach(hero.belongings.backpack);
			hero.spend(TIME_TO_CONSUME);
			hero.busy();
			
			//0. recover sanity
			hero.recoverSanity((int)(Pressure.heroPressure()*0.5f));
			//todo: show effects
			
			//1. recover hp
			hero.HP	+=	hero.HT*0.25;
			if(hero.HP>hero.HT)
				hero.HP	=	hero.HT;
			
			hero.sprite.operate(hero.pos);
			
			GLog.i(Messages.get(this, "used"));
		}
	}
	
}
