package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;

import javax.microedition.khronos.opengles.GL;
import java.util.ArrayList;

public class PotionTestPaper extends Item{

	private static final float TIME_TO_TEST =   1;
	private static final String AC_TEST =   "TEST";

	// the target potion
	// private Class<Potion> targetPotion_;
	private Potion targetPotion_	=	null;
	
	{
		image   =   ItemSpriteSheet.DPD_TEST_PAPER;
		unique  =   false;

		defaultAction   =   AC_TEST;
		stackable   =   true;
	}

	public<T extends Potion> void setTarget(Class<T> target){
		try{
			targetPotion_	=	target.newInstance();
		}catch(Exception e){}
	}

	@Override
	public String desc(){
		return Messages.get(this, "desc", targetPotion_.trueName());
	}

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions   =   super.actions(hero);
		actions.add(AC_TEST);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);

		if(action==AC_TEST){
			curUser =   hero;
			GameScene.selectItem(itemSelector, WndBag.Mode.POTION,Messages.get(this, "prompt"));
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	private void test(Item item){
		if(item.isIdentified()){
			GLog.i(Messages.get(this, "tip"));
		}else{
			// try test and identify
			detach(curUser.belongings.backpack);
			if(targetPotion_.getClass()==item.getClass()){
				item.identify();
				GLog.i(Messages.get(this, "test_succeed", item.name()));
			}else{
				GLog.i(Messages.get(this, "test_failed"));
			}

			curUser.sprite.operate(curUser.pos);
			curUser.spend(TIME_TO_TEST);
			curUser.busy();
		}
	}

	private final WndBag.Listener itemSelector  =   new WndBag.Listener(){
		@Override
		public void onSelect(Item item){
			if(item!=null){
				test(item);
			}
		}
	};

	private static final String TARGET  =   "target";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(TARGET, targetPotion_);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		if(bundle.contains(TARGET))
			targetPotion_   =   (Potion)(bundle.get(TARGET));
	}
}
