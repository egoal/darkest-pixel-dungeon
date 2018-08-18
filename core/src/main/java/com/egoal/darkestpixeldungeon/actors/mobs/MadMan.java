package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.items.food.Humanity;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

/**
 * Created by 93942 on 8/18/2018.
 */

public class MadMan extends Mob{
	{
		spriteClass	=	Sprite.class;
		
		HP	=	HT	=	30;
		defenseSkill	=	15;
		
		EXP	=	10;
		maxLvl	=	15;
		
		loot	=	Humanity.class;
		lootChance	=	0.15f;
		
		addResistances(Damage.Element.SHADOW, 2f);
		addResistances(Damage.Element.HOLY, .5f);
	}
	
	@Override
	public Damage giveDamage(Char target){
		return super.giveDamage(target);
	}
	
	@Override
	public int attackSkill(Char target){
		return 16;
	}
	
	@Override
	public Damage defendDamage(Damage dmg){
		return dmg;
	}
	
//	@Override
//	protected boolean doAttack(Char enemy){
//		if(Dungeon.level.distance(pos, enemy.pos)<=2){
//			return super.doAttack(enemy);
//		}else {
//			boolean visible	=	Level.fieldOfView[pos] || Level.fieldOfView[enemy.pos];
//
//			// give a debuff
//			
//			return !visible;
//		}
//	}
//	
	
	// sprite
	public static class Sprite extends MobSprite{
		
		public Sprite(){
			super();
			
			texture(Assets.DPD_MADMAN);

			TextureFilm frames	=	new TextureFilm(texture, 12, 14);
			
			idle	=	new Animation(8, true);
			idle.frames(frames, 0, 0, 0, 0, 0, 0, 1, 2);
			
			run	=	new Animation(8, true);
			run.frames(frames, 3, 4, 5, 6);
			
			attack	=	new Animation(8, false);
			attack.frames(frames, 10, 11, 12);
			
			die	=	new Animation(8, false);
			die.frames(frames, 7, 8, 9);
			
			play(idle);
		}
		
	}
	
}
