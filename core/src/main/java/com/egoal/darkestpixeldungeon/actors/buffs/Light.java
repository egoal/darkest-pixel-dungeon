/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Light extends FlavourBuff {

	public static final float DURATION	= 200f;
	public static final float DURATION_2	=	80f;
	public static final int DISTANCE	=	6;
	public static final int DISTANCE_2	=	4;	// nearly
	
	private float left_	=	DURATION;
	private boolean reObserved_	=	false;	// cache, no need to store in bundle

	@Override
	public boolean attachTo( Char target ) {
		if (super.attachTo( target )) {
			if (Dungeon.level != null) {
				target.viewDistance = Math.max(Dungeon.level.viewDistance, DISTANCE);
				Dungeon.observe();
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		target.viewDistance = Dungeon.level.viewDistance;
		Dungeon.observe(DISTANCE+1);
		super.detach();
	}
	
	@Override
	public boolean act(){
		if(target.isAlive()){
			spend(TICK);
			if((left_-=TICK)<=0)
				detach();
			else{
				// still on
				if(left_<DURATION_2 && !reObserved_){
					reObserved_	=	true;
					if(Dungeon.level != null){
						target.viewDistance	=	Math.max(Dungeon.level.viewDistance, DISTANCE_2);
						Dungeon.observe();
					}
				}
			}
		}else{
			detach();
		}

		return true;
	}

	@Override
	public int icon() {
		return BuffIndicator.LIGHT;
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.ILLUMINATED);
		else target.sprite.remove(CharSprite.State.ILLUMINATED);
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns());
	}

	private static final String LEFT	=	"left";
	@Override
	public void storeInBundle(Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, left_);
		
	}
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		left_	=	bundle.getFloat(LEFT);
	}
}
