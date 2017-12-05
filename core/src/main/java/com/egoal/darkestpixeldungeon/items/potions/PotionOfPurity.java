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

import com.egoal.darkestpixeldungeon.actors.blobs.ConfusionGas;
import com.egoal.darkestpixeldungeon.actors.blobs.ParalyticGas;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.GasesImmunity;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.StenchGas;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.actors.blobs.VenomGas;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class PotionOfPurity extends Potion {
	
	private static final int DISTANCE	= 5;

	{
		initials = 9;
	}

	@Override
	public void shatter( int cell ) {
		
		PathFinder.buildDistanceMap( cell, BArray.not( Level.losBlocking, null ), DISTANCE );
		
		boolean procd = false;
		
		Blob[] blobs = {
			Dungeon.level.blobs.get( ToxicGas.class ),
			Dungeon.level.blobs.get( ParalyticGas.class ),
			Dungeon.level.blobs.get( ConfusionGas.class ),
			Dungeon.level.blobs.get( StenchGas.class ),
			Dungeon.level.blobs.get( VenomGas.class )
		};
		
		for (int j=0; j < blobs.length; j++) {
			
			Blob blob = blobs[j];
			if (blob == null) {
				continue;
			}
			
			for (int i=0; i < Dungeon.level.length(); i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE) {
					
					int value = blob.cur[i];
					if (value > 0) {
						
						blob.cur[i] = 0;
						blob.volume -= value;
						procd = true;

						if (Dungeon.visible[i]) {
							CellEmitter.get( i ).burst( Speck.factory( Speck.DISCOVER ), 1 );
						}
					}

				}
			}
		}
		
		boolean heroAffected = PathFinder.distance[Dungeon.hero.pos] < Integer.MAX_VALUE;
		
		if (procd) {

			if (Dungeon.visible[cell]) {
				splash( cell );
				Sample.INSTANCE.play( Assets.SND_SHATTER );
			}

			setKnown();

			if (heroAffected) {
				GLog.p( Messages.get(this, "freshness") );
			}
			
		} else {
			
			super.shatter( cell );
			
			if (heroAffected) {
				GLog.i( Messages.get(this, "freshness") );
				setKnown();
			}
			
		}
	}
	
	@Override
	public void apply( Hero hero ) {
		GLog.w( Messages.get(this, "no_smell") );
		Buff.prolong( hero, GasesImmunity.class, GasesImmunity.DURATION );
		setKnown();
	}
	
	@Override
	public int price() {
		return isKnown() ? 40 * quantity : super.price();
	}
}
