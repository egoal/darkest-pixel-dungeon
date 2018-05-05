package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.IconTitle;
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
	private static final float COLLECT_RANGE	=	6;
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
		
		if(!isFull()){
			volume	+=	1;
			GLog.i(Messages.get(this, "collected", mob.name));
			// show effect
			CellEmitter.get(curUser.pos).burst(ShadowParticle.UP, 5);
			Sample.INSTANCE.play(Assets.SND_BURNING);
		}else{
			GLog.w(Messages.get(this, "full"));
		}
	}
	
	// check to not be negative value
	int volume(){ return volume; }
	void consume(int v){ volume-=v; }
	
	public boolean isFull(){ return volume>= MAX_VOLUME; }
	@Override
	public String status(){ return Messages.format("%d", volume); }
	
	// the casts 
	public class WndUrnOfShadow extends Window{
		private static final int WIDTH	=	80;
		private static final int BTN_HEIGHT	=	20;
		private static final float GAP	=	2;
		
		private static final String OP_SOUL_BURN	=	"soul_burn";
		private static final String OP_SOUL_MARK	=	"soul_mark";
		// private static final String OP_SPIRIT_SIPHON	=	"spirit_siphon";
		private static final String OP_DEMENTAGE	=	"dementage";
		
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
			RedButton btn0	=	new RedButton(Messages.get(this, OP_SOUL_BURN)){
				@Override
				protected void onClick(){ opSoulBurn(); }
			};
			btn0.setRect(0, titlebar.bottom()+GAP, WIDTH, BTN_HEIGHT);
			add(btn0);
			
			RedButton btn1	=	new RedButton(Messages.get(this, OP_SOUL_MARK)){
				@Override
				protected void onClick(){ opSoulMark(); }
			};
			btn1.setRect(0, btn0.bottom()+GAP, WIDTH, BTN_HEIGHT);
			add(btn1);
			
			RedButton btn2	=	new RedButton(Messages.get(this, OP_DEMENTAGE)){
				@Override
				protected void onClick(){ opDementage(); }
			};
			btn2.setRect(0, btn1.bottom()+GAP, WIDTH, BTN_HEIGHT);
			add(btn2);
			
			resize(WIDTH, (int)btn2.bottom());
		}
		
		private void opSoulBurn(){
			
			urnOfShadow.consume(3);
			hide();
		}
		
		private void opSoulMark(){
			hide();
		}
		private void opSpiritSiphon(){
			
			hide();
		}
		private void opDementage(){
			hide();
		}
		
	}
}
