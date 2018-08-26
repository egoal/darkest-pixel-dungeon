package com.egoal.darkestpixeldungeon.actors.mobs;

import android.database.DatabaseUtils;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.items.food.Humanity;
import com.egoal.darkestpixeldungeon.items.food.Wine;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.SkeletonKnightSprite;
import com.watabou.utils.Bundle;
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
	
		loot	=	Wine.class;
		lootChance	=	.1f;
		
		properties.add(Property.UNDEAD);

		addResistances(Damage.Element.FIRE, .75f);
		addResistances(Damage.Element.SHADOW, 1.5f);
		addResistances(Damage.Element.HOLY, .667f);
	}
	
	private static final float COUNTER	=	.15f;
	private static final float COMBO	=	.15f;

	private static final int COMBO_COOLDOWN	=	2;
	private int cdCombo_	=	COMBO_COOLDOWN;
	
	@Override
	protected boolean act(){
		cdCombo_	-=	1;
		return super.act();
	}
	
	@Override
	public Damage giveDamage(Char target) {
		return new Damage(Random.NormalIntRange(8, 16), this, target);
	}

	@Override
	public Damage defendDamage(Damage dmg) {
		if(dmg.type==Damage.Type.NORMAL)
			dmg.value	-=	Random.NormalIntRange(0, 6);
		return dmg;
	}
	
	@Override
	public int attackSkill(Char target){ return 18; }
	
	@Override
	public Damage defenseProc(Damage damage){
		Char enemy	=	(Char)damage.from;
		if(damage.type==Damage.Type.MAGICAL || damage.isFeatured(Damage.Feature.RANGED) ||
			enemy==null || !Dungeon.level.adjacent(pos, enemy.pos))
			return super.defenseProc(damage);
		
		
		if(Random.Float()<COUNTER){
			sprite.showStatus(CharSprite.WARNING, Messages.get(this, "counter"));
			enemy.takeDamage(giveDamage(enemy));
			
			damage.value	=	0;
		}
		return super.defenseProc(damage);
	}
	
	@Override
	public boolean attack(Char enemy){
		if(cdCombo_<=0 && Random.Float()<COMBO){
			cdCombo_	=	COMBO_COOLDOWN;
			
			spend(-cooldown()*.99f);
			sprite.showStatus(CharSprite.WARNING, Messages.get(this, "combo"));
		}
		
		return super.attack(enemy);
	}
	
	private static final String COOLDOWN_COMBO	=	"cooldown_combo";
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(COOLDOWN_COMBO, cdCombo_);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		cdCombo_	=	bundle.getInt(COOLDOWN_COMBO);
	}
}
