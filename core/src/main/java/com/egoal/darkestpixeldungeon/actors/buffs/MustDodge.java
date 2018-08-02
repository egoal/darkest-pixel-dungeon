package com.egoal.darkestpixeldungeon.actors.buffs;

import android.util.Log;

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.watabou.utils.Bundle;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by 93942 on 8/2/2018.
 */

//* buff checked in Char::checkHit
public class MustDodge extends FlavourBuff{
	private int dodgeType	=	0;
	
	private static final String DODGE_TYPE	=	"dodge_type";
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(DODGE_TYPE, dodgeType);
	}
	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		dodgeType	=	bundle.getInt(DODGE_TYPE);
	}
	
	public MustDodge addDodgeType(Damage.Type t){
		dodgeType	|=	type2int(t);
		return this;
	}
	public MustDodge addDodgeTypeAll(){
		dodgeType	=	0x07;
		return this;
	}
	
	private int type2int(Damage.Type t){
		switch(t){
			case NORMAL:
				return 0x01;
			case MAGICAL:
				return 0x02;
			case MENTAL:
				return 0x04;
			default:
				return 0x00;
		}
	}
	
	// check type
	public boolean canDodge(Damage dmg){
		return (dodgeType& type2int(dmg.type))!=0;
	}
}
