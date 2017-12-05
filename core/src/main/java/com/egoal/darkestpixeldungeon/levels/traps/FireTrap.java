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
package com.egoal.darkestpixeldungeon.levels.traps;

import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.Fire;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.TrapSprite;

public class FireTrap extends Trap {

	{
		color = TrapSprite.ORANGE;
		shape = TrapSprite.DOTS;
	}

	@Override
	public void activate() {

		GameScene.add( Blob.seed( pos, 2, Fire.class ) );
		CellEmitter.get( pos ).burst( FlameParticle.FACTORY, 5 );

	}
}
