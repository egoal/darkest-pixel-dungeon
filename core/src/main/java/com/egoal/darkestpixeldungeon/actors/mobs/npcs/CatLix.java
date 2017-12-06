package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Gold;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.armor.PlateArmor;
import com.egoal.darkestpixeldungeon.items.potions.*;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.CatLixSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndCatLix;
import com.egoal.darkestpixeldungeon.windows.WndQuest;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import javax.microedition.khronos.opengles.GL;
import java.util.ArrayList;

public class CatLix extends NPC{
	{
		name    =   Messages.get(this, "name");
		spriteClass =   CatLixSprite.class;

		properties.add(Property.IMMOVABLE);
	}

	private boolean isAnswered_ =   false;
	private boolean isPraised_  =   false;

	public Gift gift    =   new Gift();

	public void setAnswered_(boolean praise){
		isAnswered_ =   true;
		isPraised_  =   praise;

		// prepare rewards
		// 0. give some gold
		gift.identify();
		gift.addItem(new Gold(Random.Int(120, 150)));

		// 1. give something positive
		ArrayList<Item > alItems    =   new ArrayList<>();
		if(isPraised_){
			alItems.add(new ScrollOfIdentify());
			alItems.add(new ScrollOfMagicMapping());
			alItems.add(new ScrollOfRemoveCurse());
			alItems.add(new ScrollOfUpgrade());
		}else{
			alItems.add(new PotionOfHealing());
			alItems.add(new PotionOfExperience());
			alItems.add(new PotionOfStrength());
			// alItems.add(new PotionOfMight());
			alItems.add(new PotionOfMindVision());
			alItems.add(new PotionOfInvisibility());
		}
		gift.addItem(alItems.get(Random.Int(alItems.size())));
	}

	@Override
	public boolean interact() {
		sprite.turnTo(pos,Dungeon.hero.pos);

		if(!isAnswered_)
			GameScene.show(new WndCatLix(this));
		else{
			if(isPraised_){
				GameScene.show(new WndQuest(this, Messages.get(this, "happy")));
			}else{
				GameScene.show(new WndQuest(this, Messages.get(this, "normal",
					Dungeon.hero.className())));
			}
		}

		return false;
	}

	@Override
	public String description(){
		return Messages.get(this, "desc");
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	protected boolean act() {
		throwItem();
		return super.act();
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}

	@Override
	public void damage( int dmg, Object src ) {
	}

	@Override
	public void add( Buff buff ) {
	}

	/* gift */
	public static class Gift extends Item{
		{
			stackable   =   true;
			defaultAction   =   AC_OPEN;

			name    =   Messages.get(this, "name");
			image   =   ItemSpriteSheet.DPD_CAT_GIFT;
		}

		private static final String AC_OPEN =  "open";
		private static final float TIME_TO_OPEN =   1f;

		private ArrayList<Item> alItems_    =   new ArrayList<Item>();

		@Override
		public String desc(){
			return Messages.get(this, "desc");
		}

		public void addItem(Item item){
			alItems_.add(item);
		}

		@Override
		public ArrayList<String> actions(Hero hero){
			ArrayList<String > alActions    =   super.actions(hero);
			alActions.add(AC_OPEN);

			return alActions;
		}

		@Override
		public void execute(Hero hero,String action){
			if(action.equals(AC_OPEN))
				open(hero);
			else
				super.execute(hero, action);
		}

		private void open(Hero hero){
			detach(hero.belongings.backpack);
			hero.spend(TIME_TO_OPEN);
			hero.busy();

			GLog.i(Messages.get(this, "opened"));

			// give items
			for(Item item: alItems_){
				if(item.doPickUp(hero)){
					GLog.i(Messages.get(Dungeon.hero, "you_now_have", item.name()));
				}else
					Dungeon.level.drop(item, hero.pos).sprite.drop();
			}

			Sample.INSTANCE.play(Assets.SND_OPEN);
			hero.sprite.operate(hero.pos);
		}
	}
}
