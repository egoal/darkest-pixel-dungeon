package com.egoal.darkestpixeldungeon.items.weapon.enchantments;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 6/2/2018.
 */

public class Holy extends Weapon.Enchantment{
	
	private static ItemSprite.Glowing LIGHT_YELLOW	=	new ItemSprite.Glowing(0xFFFFA0);
	
	@Override
	public Damage proc(Weapon weapon,Damage damage){
		// to undead or demonic
		if(damage.to instanceof Mob){
			Mob m	=	(Mob)(damage.to);
			if(m.properties().contains(Char.Property.UNDEAD) ||
				m.properties().contains(Char.Property.DEMONIC)){
				// the extra damage is added by their resistance				
				m.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10);
			}
		}
		
		// critical
		if(!damage.isFeatured(Damage.Feature.CRITCIAL) && Random.Float()<.1f){
			damage.value	*=	1.25f;
			damage.addFeature(Damage.Feature.CRITCIAL);
		}
		
		return damage.addElement(Damage.Element.HOLY);
	}

	@Override
	public ItemSprite.Glowing glowing(){
		return LIGHT_YELLOW;
	}
}
