package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.JessicaSprite;
import com.egoal.darkestpixeldungeon.windows.WndQuest;

/**
 * Created by 93942 on 5/5/2018.
 */

public class Jessica extends NPC{

	{
		spriteClass	=	JessicaSprite.class;
	}
	
	/// do something
	@Override
	public boolean interact(){
		sprite.turnTo(pos, Dungeon.hero.pos);
		tell(Messages.get(this, "please"));

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
	public void damage( int dmg, Object src ) {
	}

	@Override
	public void add( Buff buff ) {
	}

	private void tell(String text){
		GameScene.show(new WndQuest(this, text));
	}
}
