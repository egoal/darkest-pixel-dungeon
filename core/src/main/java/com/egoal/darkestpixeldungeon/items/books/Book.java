package com.egoal.darkestpixeldungeon.items.books;

import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBook;
import com.watabou.utils.Bundle;

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

	// all text books
	public enum Title{
		UNKNOWN("unknown"),
		COLLIES_DIARY("callies_diary"),
		HEADLESS_KNIGHTS_SECRETS("headless_knights_secrets");
		
		
		Title(final String title){
			this.titile_	=	title;
		}
		String title(){ return titile_; }
		private final String titile_;
		
		// store
		private static final String TITLE	=	"TITLE";
		public void storeInBundle(Bundle bundle){
			bundle.put(TITLE, toString());
		}
		public static Title restoreInBundle(Bundle bundle){
			String value	=	bundle.getString(TITLE);
			return value.length()>0? valueOf(value): UNKNOWN;
		}
	}
	
	private Title title	=	Title.UNKNOWN;
	
	{
		stackable	=	true;
		defaultAction	=	AC_READ;
		image	=	ItemSpriteSheet.DPD_BOOKS;

	}
	
	public Book setTitle(Title t){
		title	=	t;
		return this;
	}
	public Title getTitle(){ return title; }
	
	@Override
	public boolean isSimilar(Item item){
		if(getClass()!=item.getClass()) return false;
		
		if(!this.isIdentified() && !item.isIdentified()) return true;
		
		if(this.isIdentified() && item.isIdentified()){
			if(((Book)item).getTitle()==getTitle()) return true;
		}
		
		return false;
	}
	
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
	
	public String name(){
		return isIdentified()? title(): super.name();
	}
	public String desc(){
		return isIdentified()? Messages.get(this, title.title()+".desc"): super.desc();
	}
	
	public String title(){ return Messages.get(this, title.title()+".title"); }
	public String page(int i){ return Messages.get(this, title.title()+".page"+i); }
	public int pageSize(){ 
		return Integer.parseInt(Messages.get(this, title.title()+".pagesize"));
	}
	
	@Override
	public boolean isUpgradable(){ return false; }
	
	@Override
	public int price(){ return 30*quantity; }
	
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		title.storeInBundle(bundle);
	}
	@Override
	public void restoreFromBundle(Bundle bundle){
		title	=	Title.restoreInBundle(bundle);
		super.restoreFromBundle(bundle);
	}
	
}
