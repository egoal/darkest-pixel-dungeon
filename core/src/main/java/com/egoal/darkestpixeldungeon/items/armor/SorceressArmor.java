package com.egoal.darkestpixeldungeon.items.armor;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility;
import com.egoal.darkestpixeldungeon.actors.buffs.Poison;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.buffs.Venom;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.actors.mobs.Scorpio;
import com.egoal.darkestpixeldungeon.effects.Flare;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTerror;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

/**
 * Created by 93942 on 4/24/2018.
 */

public class SorceressArmor extends ClassArmor{
	
	{
		image	=	ItemSpriteSheet.DPD_ARMOR_SORCERESS;
	}
	
	@Override
	public void doSpecial(){
		
		// cost
		curUser.HP	-=	curUser.HP/3;
		
		// effects
		new Flare(8, 48).color(0xFF0000, true).show(curUser.sprite, 3f);
		Sample.INSTANCE.play(Assets.SND_DEGRADE);
		Invisibility.dispel();
		
		// do
		for(Mob mob: Dungeon.level.mobs.toArray(new Mob[Dungeon.level.mobs.size()])){
			if(Level.fieldOfView[mob.pos]){
				// terrified & venom 
				Buff.affect(mob, Terror.class, 3).object	=	curUser.id();
				
				Venom v	=	new Venom();
				// fixme: null danger
				v.set(5f, (curUser.giveDamage(null).value/5+2));
				v.attachTo(mob);
			}
		}
		
		curUser.spendAndNext(Actor.TICK);
	}
	
}
