package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.DewVial;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.AlchemistSprite;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.IconTitle;
import com.egoal.darkestpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

public class Alchemist extends NPC{
	{
		name    =   Messages.get(this, "name");
		spriteClass =   AlchemistSprite.class;
	}

	@Override
	public boolean interact(){
		sprite.turnTo(pos,Dungeon.hero.pos);

		if(!Quest.hasGiven_){
			// give quest
			GameScene.show(new WndQuest(this, Messages.get(this, "hello")){
				@Override
				public void onBackPressed(){
					super.onBackPressed();

					Quest.hasGiven_ =   true;
					Quest.hasCompleted_ =   false;

					// drop dew vial
					DewVial dv  =   new DewVial();
					if(dv.doPickUp(Dungeon.hero)){
						GLog.i(Messages.get(Dungeon.hero, "you_now_have", dv.name()));
					}else
						Dungeon.level.drop(dv, Dungeon.hero.pos).sprite.drop();

					Dungeon.limitedDrops.dewVial.drop();
				}
			});

			// todo: add journal

		}else{
			if(!Quest.hasCompleted_){
				GameScene.show(new WndAlchemist(this));
			}else{
				tell(Messages.get(this, "farewell"));
			}
		}


		return false;
	}

	@Override
	public String description(){
		return Messages.get(this, "desc");
	}

	private void tell(String text){
		GameScene.show(new WndQuest(this, text));
	}

	public static class Quest{
		private static boolean hasGiven_    =   false;
		private static boolean hasCompleted_    =   false;

		public static void reset(){
			hasCompleted_   =   false;
			hasGiven_   =   false;
		}

		// serialization
		private static final String NODE    =   "alchemist";
		private static final String GIVEN   =   "given";
		private static final String COMPLETED   =   "completed";
		public static void storeInBundle(Bundle bundle){
			Bundle node =   new Bundle();
			node.put(GIVEN, hasGiven_);
			node.put(COMPLETED, hasCompleted_);

			bundle.put(NODE, node);
		}
		public static void restoreFromBundle(Bundle bundle){
			Bundle node =   bundle.getBundle(NODE);

			if(!node.isNull()){
				hasGiven_   =   node.getBoolean(GIVEN);
				hasCompleted_   =   node.getBoolean(COMPLETED);
			}else
				reset();
		}

	}

	public class WndAlchemist extends Window{
		private Alchemist alchemist_;

		private static final int WIDTH  =   120;
		private static final float GAP  =   2.f;
		private static final int BTN_HEIGHT =   20;

		public WndAlchemist(Alchemist alch){
			super();

			alchemist_  =   alch;

			IconTitle titleBar  =   new IconTitle();
			titleBar.icon(new AlchemistSprite());
			titleBar.label(alchemist_.name);
			titleBar.setRect(0, 0, WIDTH, 0);
			add(titleBar);

			RenderedTextMultiline rtmMessage    =   PixelScene.renderMultiline(
				Messages.get(this, "back"), 6);
			rtmMessage.maxWidth(WIDTH);
			rtmMessage.setPos(0f, titleBar.bottom()+GAP);
			add(rtmMessage);

			// add buttons
			RedButton btnAgree  =   new RedButton(Messages.get(this, "yes")){
				@Override
				protected void onClick(){ onAnswered(); }
			};
			btnAgree.setRect(0, rtmMessage.bottom()+GAP, WIDTH, BTN_HEIGHT);
			add(btnAgree);

			RedButton btnDisagree   =   new RedButton(Messages.get(this, "no")){
				@Override
				protected void onClick(){
					hide();
					yell(Messages.get(WndAlchemist.class, "wait"));
				}
			};
			btnDisagree.setRect(0, btnAgree.bottom()+GAP, WIDTH, BTN_HEIGHT);
			add(btnDisagree);

			resize(WIDTH, (int)btnDisagree.bottom());
		}

		private void onAnswered(){
			hide();

			DewVial dv  =   Dungeon.hero.belongings.getItem(DewVial.class);
			if(dv==null){
				GameScene.show(new WndQuest(alchemist_, Messages.get(this, "bottle_miss")));
			}else{
				int vol =   dv.getVolume();
				if(vol<3){

				}else if(dv.isFull()){

				}else{

				}
			}

		}
	}


}
