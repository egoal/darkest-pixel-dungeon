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
package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.sprites.RotLasherSprite;
import com.watabou.utils.Random;

import java.util.HashSet;

public class RotLasher extends Mob {

	{
		spriteClass = RotLasherSprite.class;

		HP = HT = 40;
		defenseSkill = 0;

		EXP = 1;

		loot = Generator.Category.SEED;
		lootChance = 1f;

		state = WANDERING = new Waiting();

		properties.add(Property.IMMOVABLE);
	}

	@Override
	protected boolean act() {
		if (enemy == null || !Dungeon.level.adjacent(pos, enemy.pos)) {
			HP = Math.min(HT, HP + 3);
		}
		return super.act();
	}

	@Override
	public void takeDamage(Damage dmg){
		if(dmg.hasElement(Damage.Element.FIRE)){
			destroy();
			sprite.die();
		} else {
			super.takeDamage(dmg);
		}
	}

	@Override
	public Damage attackProc(Damage damage) {
		Buff.affect((Char)damage.to, Cripple.class, 2f );
		return super.attackProc(damage);
	}

	@Override
	protected boolean getCloser(int target) {
		return true;
	}

	@Override
	protected boolean getFurther(int target) {
		return true;
	}

	@Override
	public Damage giveDamage(Char target) {
		return new Damage(Random.NormalIntRange(8, 15 ), this, target);
	}

	@Override
	public int attackSkill( Char target ) {
		return 15;
	}

	@Override
	public Damage defendDamage(Damage dmg) {
		dmg.value	-=	Random.NormalIntRange(0, 8);
		return dmg;
	}

	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();
	static {
		IMMUNITIES.add( ToxicGas.class );
	}

	@Override
	public HashSet<Class<?>> immunizedBuffs() {
		return IMMUNITIES;
	}

	private class Waiting extends Mob.Wandering{}
}
