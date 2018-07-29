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

import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Poison;
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.messages.Messages;

public class PotionOfHealing extends Potion {

	{
		initials = 2;

		bones = true;
	}
	
	@Override
	public boolean canBeReinforced(){ return !reinforced; }
	
	@Override
	public void apply( Hero hero ) {
		setKnown();
		cure( Dungeon.hero );
		GLog.p( Messages.get(this, "heal") );
	}
	
	private void cure(Hero hero){
		hero.HP	=	hero.HT;
		Buff.detach( hero, Bleeding.class );

		if(reinforced){
			Buff.detach( hero, Poison.class );
			Buff.detach( hero, Cripple.class );
			Buff.detach( hero, Weakness.class );
			Buff.detach(hero, Burning.class);
		}

		hero.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 4 );
	}
	
	public static void heal( Hero hero ) {
		// called in water of healing, so kept
		hero.HP = hero.HT;
		Buff.detach( hero, Poison.class );
		Buff.detach( hero, Cripple.class );
		Buff.detach( hero, Weakness.class );
		Buff.detach( hero, Bleeding.class );
		
		hero.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 4 );
	}

	@Override
	public int price() {
		return isKnown() ? 30 * quantity : super.price();
	}
}
