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

import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Ooze;
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
	private static final String AC_SIP	=	"SIP";
	private static final String AC_WASH	=	"WASH";

	private static final float TIME_TO_DRINK = 1f;
	private static final float TIME_TO_WASH	=	1f;

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
			actions.add(AC_SIP);
			if(volume>5)
				actions.add(AC_WASH);
		}
		return actions;
	}

	@Override
	public void execute( final Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_DRINK )){

			if(volume>0){

				int hppv	=	(int)(hero.HT*
						(hero.heroClass==HeroClass.HUNTRESS?0.12:0.08));

				int needToFill=(hero.HT-hero.HP)/hppv;
				int drink=volume<needToFill?volume:needToFill;

				consume(drink, hero);
			}else{
				GLog.w(Messages.get(this,"empty"));
			}

		}
		if(action.equals(AC_SIP)){
			if(volume>0){
				int hppv	=	(int)(hero.HT*
						(hero.heroClass==HeroClass.HUNTRESS?0.075:0.05)+1);

				int needToFill	=	(hero.HT-hero.HP)/hppv;
				if(needToFill>5) needToFill	=	5;

				int drink	=	volume<needToFill?volume:needToFill;
				consume(drink, hero);
			}else{
				GLog.w(Messages.get(this,"empty"));
			}
		}
		if(action.equals(AC_WASH)){
			if(volume>=5){
				Buff.detach(curUser, Ooze.class);
				Buff.detach(curUser, Burning.class);
				
				volume	-=	5;
				curUser.sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "ac_wash"));
				curUser.spend(TIME_TO_WASH);
				curUser.busy();

				curUser.sprite.operate(curUser.pos);
				
				updateQuickslot();
			}
		}
	}

	private void consume(int drink, Hero hero){
		int hppv	=	(int)(hero.HT*(hero.heroClass==HeroClass.HUNTRESS?0.075:0.05)+1);
		int effect=Math.min(hero.HT-hero.HP,drink*hppv);

		hero.HP+=effect;
		hero.sprite.emitter().burst(Speck.factory(Speck.HEALING),volume>5?2:1);
		hero.sprite.showStatus(CharSprite.POSITIVE,Messages.get(this,"value",effect));

		volume-=drink;

		hero.spend(TIME_TO_DRINK);
		hero.busy();

		Sample.INSTANCE.play(Assets.SND_DRINK);
		hero.sprite.operate(hero.pos);

		updateQuickslot();
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
