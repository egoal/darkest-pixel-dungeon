package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by 93942 on 5/15/2018.
 */

public class Pressure extends Buff implements Hero.Doom{
	
	private static final float STEP	=	10f; // buff update tick
	
	@Override
	public int icon(){
		return BuffIndicator.NONE;
	}
	
	@Override
	public String toString(){
		String result	=	Messages.get(this, "pressure");
		return result;
	}
	
	@Override
	public boolean act(){
		//todo: recheck & adjust this process
		if(Dungeon.level.locked){
			spend(STEP);
			return true;
		}
		
		if(target.isAlive()){
			// chance to increase
			float pIncrease	=	(Dungeon.depth/10+1)*0.1f;
			
			if(Random.Float()<pIncrease){
				Hero hero	=	(Hero)target;
				hero.damageMentally(Random.Int(1, Dungeon.depth/10+1), this);
			}
			
		}else{
			diactivate();
		}
		spend(STEP);
		
		return true;
	}
	
	@Override
	public void onDeath(){
		Dungeon.fail(getClass());
		GLog.n(Messages.get(this, "ondeath"));
	}
}
