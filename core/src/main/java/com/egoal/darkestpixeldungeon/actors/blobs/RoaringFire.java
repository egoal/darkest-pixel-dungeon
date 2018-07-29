package com.egoal.darkestpixeldungeon.actors.blobs;

import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.effects.BlobEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle;

/**
 * Created by 93942 on 7/29/2018.
 */

public class RoaringFire extends Fire{
	
	@Override
	protected void burn(int pos){
		Char ch	=	Actor.findChar(pos);
		if(ch!=null)
			Buff.prolong(ch, Cripple.class, Cripple.DURATION/2);
		
		super.burn(pos);
	}
	
	@Override
	public void use(BlobEmitter emitter){
		super.use(emitter);
		emitter.start(RoaringFlameParticle.FACTORY, 0.03f, 0);
	}
	
	// use different color, more 'red'
	public static class RoaringFlameParticle extends FlameParticle{
		
		public RoaringFlameParticle(){
			super();
			
			color(0xEE3322);
		}
		
	}
}
