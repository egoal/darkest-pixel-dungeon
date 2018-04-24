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
package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class DewVial extends Item {

	private static final int MAX_VOLUME	= 25;

	private static final String AC_DRINK	= "DRINK";

	private static final float TIME_TO_DRINK = 1f;

	private static final String TXT_STATUS	= "%d/%d";

	{
		image = ItemSpriteSheet.VIAL;

		defaultAction = AC_DRINK;

		unique = true;
	}

	private int volume = 0;

	private static final String VOLUME	= "volume";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( VOLUME, volume );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		volume	= bundle.getInt( VOLUME );
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (volume > 0) {
			actions.add( AC_DRINK );
		}
		return actions;
	}

	@Override
	public void execute( final Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_DRINK )) {

			if (volume > 0) {

				int hppv    =   (int)(hero.HT*
					(hero.heroClass==HeroClass.HUNTRESS?0.075:0.05)+1);

				int needToFill  =   (hero.HT-hero.HP)/hppv;
				int drink   =   volume<needToFill?volume:needToFill;

				int effect  =   Math.min(hero.HT-hero.HP, drink*hppv);

//				int value = 1 + (Dungeon.depth - 1) / 5;
//				if (hero.heroClass == HeroClass.HUNTRESS) {
//					value++;
//				}
//				value *= volume;
//				value = (int)Math.max(volume*volume*.01*hero.HT, value);
//				int effect = Math.min( hero.HT - hero.HP, value );
				if (effect > 0) {
					hero.HP += effect;
					hero.sprite.emitter().burst( Speck.factory( Speck.HEALING ), volume > 5 ? 2 : 1 );
					hero.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "value", effect) );
				}
//
//				volume = 0;
				volume  -=  drink;

				hero.spend( TIME_TO_DRINK );
				hero.busy();

				Sample.INSTANCE.play( Assets.SND_DRINK );
				hero.sprite.operate( hero.pos );

				updateQuickslot();


			} else {
				GLog.w( Messages.get(this, "empty") );
			}

		}
	}

	public int getVolume(){ return volume; }
	public DewVial setVolume(int v){
		v	=	v<0? 0: v;
		v	=	v>MAX_VOLUME? MAX_VOLUME: v;
		volume	=	v;
		return this;
	}
	
	public void empty() {volume = 0; updateQuickslot();}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	public boolean isFull() {
		return volume >= MAX_VOLUME;
	}

	public void collectDew( Dewdrop dew ) {

		GLog.i( Messages.get(this, "collected") );
		volume += dew.quantity;
		if (volume >= MAX_VOLUME) {
			volume = MAX_VOLUME;
			GLog.p( Messages.get(this, "full") );
		}

		updateQuickslot();
	}

	public void fill() {
		volume = MAX_VOLUME;
		updateQuickslot();
	}

	@Override
	public String status() {
		return Messages.format( TXT_STATUS, volume, MAX_VOLUME );
	}

}
