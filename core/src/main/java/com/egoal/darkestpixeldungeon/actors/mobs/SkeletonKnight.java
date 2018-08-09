package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.items.food.Humanity;
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
		
		HP	=	HT	=	60;
		defenseSkill	=	0;	// no dodge
		
		EXP	=	10;
		maxLvl	=	15;
	
		loot	=	Humanity.class;
		lootChance	=	.15f;
		
		properties.add(Property.UNDEAD);

		addResistances(Damage.Element.FIRE, .75f);
		addResistances(Damage.Element.SHADOW, 1.5f);
	}
	
	private static final float COUNTER	=	.3f;
	private static final float COMBO	=	.25f;

	@Override
	public Damage giveDamage(Char target) {
		return new Damage(Random.NormalIntRange(4, 12), this, target);
	}

	@Override
	public Damage defendDamage(Damage dmg) {
		if(dmg.type==Damage.Type.NORMAL)
			dmg.value	-=	Random.NormalIntRange(1, 6);
		return dmg;
	}
	
	@Override
	public int attackSkill(Char target){ return 12; }
	
	@Override
	public Damage defenseProc(Damage damage){
		if(damage.type==Damage.Type.MAGICAL || damage.isFeatured(Damage.Feature.RANGED))
			return super.defenseProc(damage);
		
		Char enemy	=	(Char)damage.from;
		
		if(Random.Float()<COUNTER){
			sprite.showStatus(CharSprite.WARNING, Messages.get(this, "counter"));
			enemy.takeDamage(giveDamage(enemy));
			
			damage.value	=	0;
		}
		return super.defenseProc(damage);
	}
	
	@Override
	public boolean attack(Char enemy){
		if(Random.Float()<COMBO){
			spend(-cooldown()*.99f);
			sprite.showStatus(CharSprite.WARNING, Messages.get(this, "combo"));
		}
		
		return super.attack(enemy);
	}
}
