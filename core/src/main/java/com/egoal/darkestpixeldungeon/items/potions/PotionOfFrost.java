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
package com.egoal.darkestpixeldungeon.items.potions;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Frost;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.Fire;
import com.egoal.darkestpixeldungeon.actors.blobs.Freezing;
import com.egoal.darkestpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class PotionOfFrost extends Potion {
	
	private static final int DISTANCE	= 2;

	{
		initials = 1;
	}
	
	@Override
	public boolean canBeReinforced(){ return !reinforced; }
	
	@Override
	public void shatter( int cell ) {
		
		PathFinder.buildDistanceMap( cell, BArray.not( Level.losBlocking, null ), DISTANCE );
		
		if(reinforced){
			for(int offset: PathFinder.NEIGHBOURS9){
				Mob mob	=	Dungeon.level.findMob(cell+offset);
				if(mob!=null){
					Buff.prolong(mob, Frost.class, Frost.DURATION);
				}
				if(Dungeon.level.distance(curUser.pos, cell)<=1){
					Buff.prolong(curUser, Frost.class, Frost.DURATION);
				}
			}
		}
		
		Fire fire = (Fire)Dungeon.level.blobs.get( Fire.class );

		boolean visible = false;
		for (int i=0; i < Dungeon.level.length(); i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				visible = Freezing.affect( i, fire ) || visible;
			}
		}

		if (visible) {
			splash( cell );
			Sample.INSTANCE.play( Assets.SND_SHATTER );

			setKnown();
		}
	}
	
	@Override
	public int price() {
		return isKnown() ?(int)(30 * quantity*(reinforced? 1.5: 1)): super.price();
	}
}
