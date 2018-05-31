package com.egoal.darkestpixeldungeon.actors.hero;

import com.egoal.darkestpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

/**
 * Created by 93942 on 5/31/2018.
 */

public class HeroPerk{
	
	public enum Perk{
		NONE("none",			0x00),
		DRUNKARD("drunkard", 	0x01);
		
		public String title;
		public int mask;
		
		public String desc(){
			return Messages.get(HeroPerk.class, title+"_desc");
		}
		public String title(){
			return Messages.get(HeroPerk.class, title);
		}
		Perk(String title, int mask){
			this.title	=	title;
			this.mask	=	mask;
		}
	}
	
	private int perk	=	0x00;	// nothing
	
	public HeroPerk(int p){
		perk	=	p;
	}
	
	public HeroPerk addPerk(Perk p){
		perk	|=	p.mask;
		return this;
	}
	public boolean hasPerk(Perk p){
		return (perk & p.mask)!=0;
	}
	
	// storage
	private static final String PERK	=	"perk"; 
	public void storeInBundle(Bundle bundle){
		bundle.put(PERK, perk);
	}
	
	public static HeroPerk restoreFromBundle(Bundle bundle){
		int p	=	bundle.getInt(PERK);
		return new HeroPerk(p);
	}
	
}
