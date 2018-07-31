package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * Created by 93942 on 7/29/2018.
 */

public class Astrolabe extends Artifact{
	{
		image	=	ItemSpriteSheet.DPD_ASTROLABE;
		unique	=	true;
		defaultAction	=	AC_INVOKE;
		
		exp	=	0;
		levelCap	=	10;
		cooldown	=	0;
	}
	
	private static final String AC_INVOKE	=	"INVOKE";
	
	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions	=	super.actions(hero);
		if(isEquipped(hero))
			actions.add(AC_INVOKE);
		
		return actions;
	}
	
	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if(action.equals(AC_INVOKE)){
			if (!isEquipped(hero)) GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if(cooldown>0) GLog.i(Messages.get(this, "cooldown", cooldown));
			else{
				//todo: sound effect
				hero.spend(1f);
				hero.busy();
				invokeMagic();
				
				hero.sprite.operate(hero.pos);
			}
		}
	}

	void invokeMagic(){
		GLog.i("invoking...");
		Sample.INSTANCE.play(Assets.SND_ASTROLABE);
		
		
		cooldown	=	10;
	}
	
	private static final String COOLDOWN	=	"cooldown";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put( COOLDOWN, cooldown );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		cooldown	=	bundle.getInt( COOLDOWN );
	}
	@Override
	public int price() {
		return 0;
	}
	
	@Override
	protected ArtifactBuff passiveBuff(){ return new AstrolabeRecharge(); }
	
	public class AstrolabeRecharge extends ArtifactBuff{
		public boolean act(){
			if(cooldown>0)
				cooldown--;
			
			updateQuickslot();
			spend(TICK);
			return true;
		}
		
	}
	
}
