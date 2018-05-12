package com.egoal.darkestpixeldungeon.items.books;

import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBook;

import java.util.ArrayList;

/**
 * Created by 93942 on 5/10/2018.
 */

/*
* articles, notes, magic, so on
* this is like to be scroll, 
* but, not random, not consumable, 
* so, make a new class
* 
*/

public class Book extends Item{
	
	public static final String AC_READ	=	"READ";

	{
		stackable	=	true;
		defaultAction	=	AC_READ;
		image	=	ItemSpriteSheet.DPD_BOOKS;

	}
	
	private int pageSize_	=	-1;
	
	@Override
	public ArrayList<String > actions(Hero hero){
		ArrayList<String > actions	=	super.actions(hero);
		actions.add(AC_READ);
		
		return actions;
	}
	
	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if(action.equals(AC_READ)){
			if(hero.buff(Blindness.class)!=null){
				GLog.w(Messages.get(this, "blinded"));
			}else{
				doRead();
			}
			
		}
	}
	
	//todo: add open book afx
	protected void doRead(){
		GameScene.show(new WndBook(this));
		identify();
	}
	
	public String title(){ return Messages.get(this, "title"); }
	public String page(int i){ return Messages.get(this, "page"+i); }
	public int pageSize(){ 
		if(pageSize_<0)
			pageSize_	=	Integer.parseInt(Messages.get(this, "pagesize")); 
		return pageSize_;
	}
	
	@Override
	public boolean isUpgradable(){ return false; }
	
	@Override
	public int price(){ return 30*quantity; }
	

}
