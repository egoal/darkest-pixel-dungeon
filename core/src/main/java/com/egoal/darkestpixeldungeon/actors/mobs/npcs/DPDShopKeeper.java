package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.sprites.ShopkeeperSprite;

/**
 * Created by 93942 on 8/18/2018.
 */

//* the new transaction system
public class DPDShopKeeper extends NPC{
	
	{
		spriteClass	=	ShopkeeperSprite.class;
		
		properties.add(Property.IMMOVABLE);
	}
	
	@Override
	protected boolean act(){
		throwItem();
		
		sprite.turnTo(pos, Dungeon.hero.pos);
		spend(TICK);
		return true;
	}

	@Override
	public void takeDamage(Damage dmg){
		flee();
	}

	@Override
	public void add( Buff buff ) {
	}

	@Override
	public boolean reset() {
		return true;
	}
	
	// interact
	@Override
	public boolean interact(){
		
		return false;
	}
	
	// actions
	private void flee(){
		destroy();
		sprite.killAndErase();
		CellEmitter.get(pos).burst(ElmoParticle.FACTORY, 6);
	}
	
}
