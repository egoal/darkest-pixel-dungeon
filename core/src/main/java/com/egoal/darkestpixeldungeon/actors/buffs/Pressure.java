package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by 93942 on 5/15/2018.
 */

public class Pressure extends Buff implements Hero.Doom{
	
	private static final float STEP	=	10f; // buff update tick
	
	private static final float LVL_CONFIDENT	=	20f;
	private static final float LVL_NORMAL	=	80f;
	private static final float LVL_NERVOUS	=	100f;
	private static final float LVL_COLLAPSE	=	110f;
	
	public enum Level{
		CONFIDENT("confident"), NORMAL("normal"), NERVOUS("nervous"), COLLAPSE("collapse"); 
		public String title;
		Level(String t){ title=t; }
	}
	
	public float pressure	=	0f;
	public static final float MAX_PRESSURE	=	100f;
	Level level	=	Level.CONFIDENT;
	
	private static final String PRESSURE	=	"pressure";
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(PRESSURE, pressure);
		
	}
	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		pressure	=	bundle.getFloat(PRESSURE);
		updateLevel();
	}
	
	public float upPressure(float p){
		float rp	=	(LVL_NERVOUS-pressure>p)? p: (LVL_NERVOUS-pressure);
		pressure	+=	rp;
		
		updateLevel();
		return rp;
	}
	public float downPressure(float p){
		float rp	=	pressure<p? pressure: p;
		pressure	-=	rp;
		
		updateLevel();
		return rp;
	}
	
	public void updateLevel(){
		if(pressure<=LVL_CONFIDENT) level	=	Level.CONFIDENT;
		else if(pressure<=LVL_NORMAL) level	=	Level.NORMAL;
		else if(pressure<LVL_NERVOUS) level	=	Level.NERVOUS;
		else level	=	Level.COLLAPSE;
		
		BuffIndicator.refreshHero();
	}
	public Level getLevel(){
		return level;
	}
	
	@Override
	public int icon(){
		switch(level){
			case CONFIDENT:
				return BuffIndicator.CONFIDENT;
			case NORMAL:
				return BuffIndicator.NONE;
			case NERVOUS:
				return BuffIndicator.NERVOUS;
			case COLLAPSE:
				return BuffIndicator.COLLAPSE;
		}
		// never come here
		return BuffIndicator.NONE;
	}
	
	@Override
	public String toString(){
		String result	=	Messages.get(this, level.title);
		return result;
	}
	
	@Override
	public String desc(){
		return Messages.get(this, "desc_intro_"+level.title)+Messages.get(this, "desc");
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
				upPressure((Random.Int(1, Dungeon.depth/10+1)));
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
