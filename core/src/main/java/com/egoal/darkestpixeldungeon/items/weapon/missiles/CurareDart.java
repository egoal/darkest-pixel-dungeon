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
package com.egoal.darkestpixeldungeon.items.weapon.missiles;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class CurareDart extends MissileWeapon {

	public static final float DURATION	= 3f;
	
	{
		image = ItemSpriteSheet.CURARE_DART;
	}

	@Override
	public int min(int lvl) {
		return 1;
	}

	@Override
	public int max(int lvl) {
		return 3;
	}

	@Override
	public int STRReq(int lvl) {
		return 14;
	}

	public CurareDart() {
		this( 1 );
	}
	
	public CurareDart( int number ) {
		super();
		quantity = number;
	}
	
	@Override
	public Damage proc(Damage dmg){
		Buff.prolong( (Char)dmg.to, Paralysis.class, DURATION );
		return super.proc(dmg);
	}
	
	@Override
	public Item random() {
		quantity = Random.Int( 2, 5 );
		return this;
	}
	
	@Override
	public int price() {
		return 8 * quantity;
	}
}
