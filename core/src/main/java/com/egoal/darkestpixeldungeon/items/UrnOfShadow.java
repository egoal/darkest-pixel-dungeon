package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption;
import com.egoal.darkestpixeldungeon.actors.buffs.SoulBurning;
import com.egoal.darkestpixeldungeon.actors.buffs.SoulMark;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.CellSelector;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.IconTitle;
import com.egoal.darkestpixeldungeon.windows.WndMessage;
import com.egoal.darkestpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by 93942 on 5/5/2018.
 */

public class UrnOfShadow extends Item{
	
	{
		image	=	ItemSpriteSheet.DPD_URN_OF_SHADOW;
		unique	=	true;
		defaultAction	=	AC_CONSUME;
	}
	
	private static final int MAX_VOLUME	=	10;
	private static final float COLLECT_RANGE	=	4;
	private int volume	=	0;
	
	private static final String AC_CONSUME	=	"CONSUME";
	
	private static final String VOLUME	=	"volume";
	
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(VOLUME, volume);
	}
	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		volume	=	bundle.getInt(VOLUME);
	}
	
	@Override
	public boolean isUpgradable() { return false; }
	
	// functions
	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions	=	super.actions(hero);
		if(volume>0)
			actions.add(AC_CONSUME);
		
		return actions;
	}
	
	@Override
	public void execute(final Hero hero, String action){
		super.execute(hero, action);
		if(action.equals(AC_CONSUME)){
			GameScene.show(new WndUrnOfShadow(this));
		}
		
	}
	
	public void collectSoul(Mob mob){
		// check range
		Point mp	=	Dungeon.level.cellToPoint(mob.pos);
		Point cp	=	Dungeon.level.cellToPoint(curUser.pos);
		if(Math.abs(cp.x-mp.x)+Math.abs(cp.y-mp.y) > COLLECT_RANGE)
			// not in range
			return;
		
		// check soul
		if(!mob.hostile || mob.properties().contains(Char.Property.UNDEAD)){
			// no soul to grasp
			return;
		}
		
		if(!isFull()){
			volume	+=	1;
			GLog.i(Messages.get(this, "collected", mob.name));
			// show effect
			CellEmitter.get(curUser.pos).burst(ShadowParticle.CURSE, 5);
			Sample.INSTANCE.play(Assets.SND_BURNING);
		}else{
			GLog.w(Messages.get(this, "full"));
		}

		updateQuickslot();
	}
	
	// check to not be negative value
	int volume(){ return volume; }
	void consume(int v){ 
		volume	-=	v;
		updateQuickslot();
	}
	
	public boolean isFull(){ return volume>= MAX_VOLUME; }
	@Override
	public String status(){ return Messages.format("%d", volume); }
	@Override
	public String desc(){
		String desc	=	super.desc();
		desc	+=	"\n\n"+Messages.get(this, "desc_hint");
		return desc;
	}
	
	// the casts 
	public class WndUrnOfShadow extends Window{
		private static final int WIDTH	=	80;
		private static final int BTN_HEIGHT	=	20;
		private static final float GAP	=	2;
		
		private static final String OP_SOUL_BURN	=	"soul_burn";
		private static final int COST_SOUL_BURN	=	3;
		private static final String OP_SOUL_MARK	=	"soul_mark";
		private static final int COST_SOUL_MARK	=	5;
		// private static final String OP_SPIRIT_SIPHON	=	"spirit_siphon";
		private static final String OP_DEMENTAGE	=	"dementage";
		private static final int COST_DEMENTAGE	=	MAX_VOLUME;
		
		private String opCast_;
		
		private UrnOfShadow urnOfShadow	=	null;
		
		public WndUrnOfShadow(UrnOfShadow uos){
			super();
			urnOfShadow	=	uos;
			
			IconTitle titlebar	=	new IconTitle();
			titlebar.icon(new ItemSprite(uos.image(), null));
			titlebar.label(Messages.titleCase(uos.name()));
			titlebar.setRect(0, 0, WIDTH, 0);
			add(titlebar);
			
			// add casts
			RedButton btn0	=	addCastAndHelpButton(OP_SOUL_BURN, titlebar.bottom()+GAP, COST_SOUL_BURN);
			
			RedButton btn1	=	addCastAndHelpButton(OP_SOUL_MARK, btn0.bottom()+GAP, COST_SOUL_MARK);
			
			RedButton btn2	=	addCastAndHelpButton(OP_DEMENTAGE, btn1.bottom()+GAP, COST_DEMENTAGE);
			
			resize(WIDTH, (int)btn2.bottom());
		}
		
		private static final int WIDTH_CAST_BUTTON	=	60;
		private static final int WIDTH_HELP_BUTTON	=	15;
		private RedButton addCastAndHelpButton(final String op, float y, final int cost){
			RedButton btnCast	=	new RedButton(Messages.get(this, op)){
				@Override
				protected void onClick(){
					opCast_	=	op;
					hide();
					GameScene.selectCell(caster);
				}
			};
			btnCast.setRect(0, y, WIDTH_CAST_BUTTON, BTN_HEIGHT);
			add(btnCast);
			btnCast.enable(urnOfShadow.volume()>=cost);
			
			// todo: use sprite instead
			RedButton btnHelp	=	new RedButton("?"){
				@Override
				protected void onClick(){
					GameScene.show(new WndTitledMessage(
						new ItemSprite(urnOfShadow.image(), null), 
							Messages.get(UrnOfShadow.WndUrnOfShadow.class, op), 
							Messages.get(UrnOfShadow.WndUrnOfShadow.class, op+"_desc")+
								Messages.get(UrnOfShadow.WndUrnOfShadow.class, "cost", cost)));
				}
			};
			btnHelp.setRect(WIDTH-WIDTH_HELP_BUTTON, btnCast.top(), WIDTH_HELP_BUTTON, BTN_HEIGHT);
			add(btnHelp);
			
			return btnCast;
		}
		
		private void opSoulBurn(Char target){
			urnOfShadow.consume(COST_SOUL_BURN);
			
			target.damage(curUser.damageRoll(), curUser);
			Buff.affect(target, SoulBurning.class).reignite(target);
		}
		
		private void opSoulMark(Char target){
			urnOfShadow.consume(COST_SOUL_MARK);

			SoulMark.prolong(target, SoulMark.class, SoulMark.DURATION);
		}

		private void opDementage(Char target){
			if(target.buffs(Corruption.class)!=null){
				GLog.w(Messages.get(this, "already_dementage"));
				return;
			}
			if(target.properties().contains(Char.Property.BOSS) || 
					target.properties().contains(Char.Property.MINIBOSS)){
				GLog.w(Messages.get(this, "boss"));
				return;
			}
			
			// corruption, refill health
			urnOfShadow.consume(COST_DEMENTAGE);
			
			Buff.append(target, Corruption.class);
			target.HP	=	target.HT;
			GLog.i(Messages.get(this, "sucess_dementage", target.name));
			
		}
		
		protected CellSelector.Listener caster	=	new CellSelector.Listener(){
			@Override
			public void onSelect(Integer target){
				if(target!=null){
					final Ballistica shot	=	new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT);
					//todo: check
					Char c	=	Actor.findChar(shot.collisionPos);
					if(c!=null){
						switch(opCast_){
							case OP_SOUL_BURN:
								opSoulBurn(c);
								break;
							case OP_SOUL_MARK:
								opSoulMark(c);
								break;
							case OP_DEMENTAGE:
								opDementage(c);
								break;
						}
					}else{
						GLog.w(Messages.get(UrnOfShadow.WndUrnOfShadow.class, "not_select_target"));
					}
;				}
			}

			@Override
			public String prompt(){
				return Messages.get(UrnOfShadow.WndUrnOfShadow.class, "prompt");
			}
		};
		
	}
}
