package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.DisheartenedBuddySprite;
import com.egoal.darkestpixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 5/8/2018.
 */

public class DisheartenedBuddy extends NPC{

	{
		spriteClass	=	DisheartenedBuddySprite.class;
	}

	private int meetTimes_	=	0;
	
	private static final String MEET_TIMES	=	"meettimes";
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(MEET_TIMES, meetTimes_);
	}
	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		meetTimes_	=	bundle.getInt(MEET_TIMES);
	}
	
	@Override
	public boolean interact(){
		sprite.turnTo(pos, Dungeon.hero.pos);

		float[] chances=new float[]{1,2,2,2};
		if(meetTimes_++==0){
			chances[0]	=	10;
		}
		
		tell(Messages.get(this,"discourage"+Random.chances(chances)));
		
		return false;
	}

	// unbreakable
	@Override
	public boolean reset() {
		return true;
	}

	@Override
	protected boolean act() {
		throwItem();
		return super.act();
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}

	@Override
	public void takeDamage(Damage dmg){
	}

	@Override
	public void add( Buff buff ) {
	}

	private void tell(String text){
		GameScene.show(new WndQuest(this, text));
	}
}