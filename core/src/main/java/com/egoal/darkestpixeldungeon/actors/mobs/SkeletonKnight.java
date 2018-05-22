package com.egoal.darkestpixeldungeon.actors.mobs;

import android.app.admin.DeviceAdminInfo;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.items.Humanity;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.SkeletonKnightSprite;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 5/13/2018.
 */

public class SkeletonKnight extends Mob{
	
	{
		spriteClass	=	SkeletonKnightSprite.class;
		
		HP	=	HT	=	50;
		defenseSkill	=	0;	// no dodge
		
		EXP	=	10;
		maxLvl	=	15;
	
		loot	=	Humanity.class;
		lootChance	=	.2f;
		
		properties.add(Property.UNDEAD);
	}
	
	private static final float COUNTER	=	.15f;
	private static final float COMBO	=	.15f;

	@Override
	public Damage giveDamage(Char target) {
		return new Damage(Random.NormalIntRange(4, 12), this, target);
	}

	@Override
	public Damage defendDamage(Damage dmg) {
		dmg.value	-=	Random.NormalIntRange(0, 5);
		return dmg;
	}
	
	@Override
	public int attackSkill(Char target){ return 12; }
	
	@Override
	public Damage defenseProc(Damage damage){
		Char enemy	=	(Char)damage.from;
		
		if(Random.Float()<COUNTER){
			sprite.showStatus(CharSprite.WARNING, Messages.get(this, "counter"));
			enemy.takeDamage(damage);
			
			damage.value	=	0;
		}
		return super.defenseProc(damage);
	}
	
	@Override
	public Damage attackProc(Damage damage){
		if(Random.Float()<COMBO){
			// almost, no time cost
			spend(-cooldown()*0.99f);
			sprite.showStatus(CharSprite.WARNING, Messages.get(this, "combo"));
		}
		
		return damage;
	}
}
