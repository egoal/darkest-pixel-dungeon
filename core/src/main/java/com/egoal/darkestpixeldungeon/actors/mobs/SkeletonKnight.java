package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.actors.Char;
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
	
	private static final float COUNTER	=	.2f;
	private static final float COMBO	=	.2f;
	
	@Override
	public int damageRoll(){ return Random.NormalIntRange(4, 12); }
	
	@Override
	public int drRoll(){ return Random.NormalIntRange(0, 5); }
	
	@Override
	public int attackSkill(Char target){ return 12; }
	
	@Override
	public int defenseProc(Char enemy, int damage){
		if(Random.Float()<COUNTER){
			sprite.showStatus(CharSprite.WARNING, Messages.get(this, "counter"));
			enemy.damage(damage, this);
			
			return 0;
		}
		return super.defenseProc(enemy, damage);
	}
	
	@Override
	public int attackProc(Char enemy, int damage){
		if(Random.Float()<COMBO){
			// almost, no time cost
			spend(-cooldown()*0.99f);
			sprite.showStatus(CharSprite.WARNING, Messages.get(this, "combo"));
		}
		
		return damage;
	}
}
