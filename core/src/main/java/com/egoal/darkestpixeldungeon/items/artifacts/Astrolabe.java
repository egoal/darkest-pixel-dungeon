package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.actors.buffs.LifeLink;
import com.egoal.darkestpixeldungeon.actors.buffs.MustDodge;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo;
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable;
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.CellSelector;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 7/29/2018.
 */

public class Astrolabe extends Artifact{
	{
		image	=	ItemSpriteSheet.DPD_ASTROLABE;
		unique	=	true;
		bones	=	false;
		defaultAction	=	AC_INVOKE;
		
		exp	=	0;
		levelCap	=	10;
		// cooldown	=	0;
		
		charge	=	3;
		partialCharge	=	0;
		chargeCap	=	4;
	}
	
	private static float TIME_TO_INVOKE	=	.5f;
	// private static int NORMAL_COOLDOWN	=	8;
	private static final float NORMAL_CHARGE_SPEED	=	1f/20f;
	
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
			// else if(cooldown>0) GLog.i(Messages.get(this, "cooldown", cooldown));
			else if (charge <= 0)  GLog.i( Messages.get(this, "no_charge") );
			else{
				invokeMagic();
				
				updateQuickslot();
			}
		}
	}
	
	private boolean blockNextNegative	=	false;
	private boolean nextNegativeIsImprison	=	false;
	private float chargeSpeed	=	NORMAL_CHARGE_SPEED;
	
	// invoke logic
	private void invokeMagic(){
		--charge;
		Sample.INSTANCE.play(Assets.SND_ASTROLABE);

		boolean invokePositive	=	Random.Float()< (cursed? .5f: .85f);
		
		if(!invokePositive && blockNextNegative){
			blockNextNegative	=	false;
			// cooldown	=	NORMAL_COOLDOWN;

			curUser.sprite.showStatus(0x420000,Messages.get(Invoker.class,"extremely_lucky_block"));
			return;
		}
		
		Invoker ivk;
		if(!invokePositive && nextNegativeIsImprison){
			ivk	=	new imprison();	
		}else{
			ivk	=	invokePositive? randomPositiveInvoke(): randomNegativeInvoke();
			
		}
		
		final int color	=	invokePositive? 0xCC5252: 0x000026;

		curUser.sprite.showStatus(color, ivk.status());
		ivk.invoke(curUser, this);
	}
	
	private static Class<?>[] positiveInvokers	=	new Class<?>[]{
		foresight.class, purgation.class, life_link.class, 
		extremely_lucky.class, pardon.class, faith.class, 
		overload.class, guide.class, prophesy.class
	};
	private static float[] positiveProbs	=	new float[]{
		10, 10, 10, 5, 10, 5, 10, 10, 10, 
	};
	private static Class<?>[] negativeInvokers	=	new Class<?>[]{
		punish.class, vain.class, feedback.class, imprison.class, 
	};	
	private static float[] negativeProbs	=	new float[]{
		10, 10, 10, 10, 
	};
	private Invoker randomPositiveInvoke(){
		try{
			return (Invoker)positiveInvokers[Random.chances(positiveProbs)].newInstance();
		}catch(Exception e){
			DarkestPixelDungeon.reportException(e);
			return null;
		}
	}
	private Invoker randomNegativeInvoke(){
		try{
			return (Invoker)negativeInvokers[Random.chances(negativeProbs)].newInstance();
		}catch(Exception e){
			DarkestPixelDungeon.reportException(e);
			return null;
		}
	}
	
	// private static final String COOLDOWN	=	"cooldown";
	private static final String BLOCK_NEXT_NEGATIVE	=	"block_next_negative";
	private static final String NEXT_IS_IMPRISON	=	"next_negative_is_imprison";
	private static final String CHARGE_SPEED	=	"charge_speed";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		// bundle.put( COOLDOWN, cooldown );
		bundle.put(BLOCK_NEXT_NEGATIVE, blockNextNegative);
		bundle.put(NEXT_IS_IMPRISON, nextNegativeIsImprison);
		bundle.put(CHARGE_SPEED, chargeSpeed);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		// cooldown	=	bundle.getInt( COOLDOWN );
		blockNextNegative	=	bundle.getBoolean(BLOCK_NEXT_NEGATIVE);
		nextNegativeIsImprison	=	bundle.getBoolean(NEXT_IS_IMPRISON);
		chargeSpeed	=	bundle.getFloat(CHARGE_SPEED);
	}
	@Override
	public int price() {
		return 0;
	}
	
	@Override
	protected ArtifactBuff passiveBuff(){ return new AstrolabeRecharge(); }
	
	public class AstrolabeRecharge extends ArtifactBuff{
		public boolean act(){
			if(charge<chargeCap){
				partialCharge	+=	chargeSpeed;

				if(partialCharge>=1){
					++charge;
					--partialCharge;
					chargeSpeed	=	NORMAL_CHARGE_SPEED;
					if(charge==chargeCap)
						partialCharge	=	0f;
				}
			}else{
				partialCharge	=	0f;
			}
			
			// --cooldown;
			
			updateQuickslot();
			spend(TICK);
			return true;
		}
		
	}
	
	//* invokers
	public static class Invoker{
		protected String name_	=	"invoker";
		protected boolean needTarget_	=	false;
		// protected int cooldown_	=	NORMAL_COOLDOWN;
		private Hero user_	=	null;
		private Astrolabe a_	=	null;
		protected float chargeSpeed_	=	NORMAL_CHARGE_SPEED;
		
		public String status(){
			return Messages.get(Invoker.class, name_);
		}
		
		public final void invoke(Hero user, Astrolabe a){
			user_	=	user;
			a_	=	a;
			a.chargeSpeed	=	chargeSpeed_;
			// a.cooldown	=	cooldown_;
			if(needTarget_){
				GameScene.selectCell(caster);
			}else{
				invoke_directly(user, a);
				
				user_.spend(TIME_TO_INVOKE);
				user_.busy();
				user_.sprite.operate(user_.pos);
			}
		}
		
		// impl
		protected void invoke_directly(Hero user, Astrolabe a){}
		protected void invoke_on_target(Hero user, Astrolabe a, Char c){}
		
		protected boolean check_is_other(Char c){
			if(c==null || c==user_){
				GLog.w(Messages.get(Astrolabe.Invoker.class, "not_select_target"));
				return false;
			}
			return true;
		}
		
		protected CellSelector.Listener caster	=	new CellSelector.Listener(){
			@Override
			public void onSelect(Integer cell){
				if(cell!=null){
					final Ballistica shot	=	new Ballistica(curUser.pos, cell, Ballistica.MAGIC_BOLT);
					Char c	=	Actor.findChar(shot.collisionPos);
					
					invoke_on_target(user_, a_, c);

					user_.spend(TIME_TO_INVOKE);
					user_.busy();
					user_.sprite.operate(user_.pos);
				}
			}

			@Override
			public String prompt(){
				return Messages.get(Astrolabe.Invoker.class, "prompt");
			}
		};
	}
	
	// positive
	public static class foresight extends Invoker{
		{
			name_	=	"foresight";
		}
		
		@Override
		protected void invoke_directly(Hero user, Astrolabe a){
			Buff.prolong(user, MustDodge.class, 3f).addDodgeTypeAll();
		}
	}
	public static class purgation extends Invoker{
		{
			name_	=	"purgation";
			needTarget_	=	true;
		}
		
		@Override
		protected void invoke_on_target(Hero user, Astrolabe a, Char c){
			if(check_is_other(c)){
				int dmg	=	(int)((c.HT-c.HP)*.6f)+1;
				//todo: add effect
				c.takeDamage(new Damage(dmg,user,c).addFeature(Damage.Feature.PURE| Damage.Feature.ACCURATE));
			}
		}
	}
	public static class life_link extends Invoker{
		{
			name_	=	"life_link";
			needTarget_	=	true;
		}

		@Override
		protected void invoke_on_target(Hero user, Astrolabe a, Char c){
			if(check_is_other(c)){
				Buff.prolong(user,LifeLink.class, 3f).linker	=	c.id();
				
			}
		}
	}
	public static class extremely_lucky extends Invoker{
		{
			name_	=	"extremely_lucky";
		}
		
		@Override
		protected void invoke_directly(Hero user, Astrolabe a){
			// recover hp
			int heal	=	(user.HT-user.HP)/10+1;
			user.HP	=	heal>(user.HT-user.HP)? user.HT: (user.HP+heal);
			user.sprite.showStatus(CharSprite.POSITIVE, Integer.toString(heal));
			
			a.blockNextNegative	=	true;
		}
	}
	public static class pardon extends Invoker{
		{
			name_	=	"pardon";
			needTarget_	=	true;
		}
		
		@Override
		protected void invoke_on_target(Hero user, Astrolabe a, Char c){
			if(check_is_other(c)){
				int dhp	=	c.HP/4+1;
				c.HP	+=	dhp;
				if(c.HP>c.HT)
					c.HT	=	c.HP;
				c.sprite.showStatus(CharSprite.POSITIVE, Integer.toString(dhp));
				Buff.prolong(c, Vulnerable.class, Vulnerable.DURATION).ratio	=	1.5f;
			}
		}
	}
	public static class faith extends Invoker{
		{
			name_	=	"faith";
			// cooldown_	=	NORMAL_COOLDOWN*3/5;
			chargeSpeed_	=	NORMAL_CHARGE_SPEED*2f;
		}
		
		@Override
		protected void invoke_directly(Hero user, Astrolabe a){
			user.recoverSanity(Random.Int(1, 4));
		}
	}
	public static class overload extends Invoker{
		{
			name_	=	"overload";
			needTarget_	=	true;
		}

		@Override
		protected void invoke_on_target(Hero user, Astrolabe a, Char c){
			if(check_is_other(c)){
				int cost	=	user.HT/10;
				if(cost>=user.HP) cost	=	user.HP-1;
				int dmg	=	cost*2;
				
				c.takeDamage(new Damage(dmg, user, c).type(Damage.Type.MAGICAL));
				user.takeDamage(new Damage(cost, a, c).addFeature(Damage.Feature.PURE| Damage.Feature.ACCURATE));
			}
		}
	}
	public static class guide extends Invoker{
		{
			name_	=	"guide";
			needTarget_	=	true;
		}
		@Override
		protected void invoke_on_target(Hero user, Astrolabe a, Char c){
			if(check_is_other(c)){
				Ballistica shot	=	new Ballistica(curUser.pos, c.pos, Ballistica.MAGIC_BOLT);
				if(shot.path.size()> shot.dist+1)
					WandOfBlastWave.throwChar(c, 
						new Ballistica(c.pos, shot.path.get(shot.dist+1), Ballistica.MAGIC_BOLT), 3);
			}
		}
	}
	public static class prophesy extends Invoker{
		{
			name_="prophesy";
		}

		@Override
		protected void invoke_directly(Hero user, Astrolabe a){
			for(int i: PathFinder.NEIGHBOURS8){
				Char ch	=	Actor.findChar(user.pos+i);
				if(ch!=null){
					Buff.prolong(ch, Paralysis.class, 3f);
					ch.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12);
				}
			}
		}
	}
	
	// negative
	public static class punish extends Invoker{
		{
			name_	=	"punish";
		}
		
		@Override
		protected void invoke_directly(Hero user, Astrolabe a){
			user.takeDamage(new Damage((int)(user.HP*.25f), this, user
				).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.ACCURATE));
		}
	}
	public static class vain extends Invoker{
		{
			name_	=	"vain";
		}

		@Override
		protected void invoke_directly(Hero user, Astrolabe a){
			Buff.prolong(user, Vertigo.class, 5f);
			Buff.prolong(user, Weakness.class, 5f);
		}
	}
	public static class feedback extends Invoker{
		{
			name_	=	"feedback";
		}

		@Override
		protected void invoke_directly(Hero user, Astrolabe a){
			user.takeDamage(new Damage(Random.Int(4, 8), this, user).type(Damage.Type.MENTAL)
				.addFeature(Damage.Feature.ACCURATE));
		}
	}
	public static class imprison extends Invoker{
		{
			name_	=	"imprison";
			// cooldown_	=	NORMAL_COOLDOWN*2;
			chargeSpeed_	=	NORMAL_CHARGE_SPEED*.6f;
		}
		
		@Override
		protected void invoke_directly(Hero user, Astrolabe a){
			// Buff.prolong(user, Cripple.class, Cripple.DURATION/2);
			Buff.prolong(user, Roots.class, 3f);
		}
	}
}
