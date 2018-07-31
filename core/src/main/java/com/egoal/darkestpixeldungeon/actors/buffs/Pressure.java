package com.egoal.darkestpixeldungeon.actors.buffs;

import android.util.DebugUtils;
import android.util.Log;

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
	
	private static final float LVL_CONFIDENT	=	30f;
	private static final float LVL_NORMAL	=	70f;
	private static final float LVL_NERVOUS	=	100f;
	private static final float LVL_COLLAPSE	=	110f;
	
	public enum Level{
		CONFIDENT("confident"), NORMAL("normal"), NERVOUS("nervous"), COLLAPSE("collapse"); 
		public String title;
		Level(String t){ title=t; }
	}
	
	// private int stepsToCollapse	=	5;
	private int collapseDuration	=	0;
	
	public float pressure	=	0f;
	public static final float MAX_PRESSURE	=	100f;
	Level level	=	Level.CONFIDENT;
	
	private static final String PRESSURE	=	"pressure";
	private static final String COLLASPE_DURATION	=	"collapse_duration";
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(PRESSURE, pressure);
		bundle.put(COLLASPE_DURATION, collapseDuration);
		
	}
	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		pressure	=	bundle.getFloat(PRESSURE);
		collapseDuration	=	bundle.getInt(COLLASPE_DURATION);
		updateLevel();
	}
	
	public static float heroPressure(){
		if(Dungeon.hero!=null && Dungeon.hero.isAlive()){
			return Dungeon.hero.buff(Pressure.class).pressure;
		}
		return 0.f;
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
		Level newLevel	=	null;
		if(pressure<=LVL_CONFIDENT) newLevel	=	Level.CONFIDENT;
		else if(pressure<=LVL_NORMAL) newLevel	=	Level.NORMAL;
		else if(pressure<LVL_NERVOUS) newLevel	=	Level.NERVOUS;
		else newLevel	=	Level.COLLAPSE;
		
		if(newLevel.title!=level.title){
			// level changed
			level	=	newLevel;
			BuffIndicator.refreshHero();
			
			// reset collapse
			if(level!=Level.COLLAPSE)
				collapseDuration	=	0;
			
			switch(level){
				case CONFIDENT:
					GLog.p(Messages.get(this, "reach_"+level.title));
					break;
				case NORMAL:
					break;
				case NERVOUS:
					GLog.w(Messages.get(this, "reach_"+level.title));
					break;
				case COLLAPSE:
					GLog.h(Messages.get(this, "reach_"+level.title));
					break;
			}
		}
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
			// normal increase pressure in the dungeon
			if(Dungeon.depth>0){
				// chance to increase, not in the village
				if(Random.Int(10)==0){
					upPressure(Random.Int(1, (Dungeon.depth/4+1)));
				}
			}

			// nearly death
			if(level==Level.COLLAPSE){
				// take damage
				double ed	=	Math.exp(collapseDuration++-4.);
				target.takeDamage(new Damage((int)(target.HT*(ed/(ed+1.))), 
					this, target).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE));
				
				if(target==Dungeon.hero){
					Dungeon.hero.interrupt();
					GLog.n(Messages.get(this, "onhurt"));
				}
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
