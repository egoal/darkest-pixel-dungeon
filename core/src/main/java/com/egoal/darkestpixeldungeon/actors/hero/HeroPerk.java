package com.egoal.darkestpixeldungeon.actors.hero;

import com.egoal.darkestpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

/**
 * Created by 93942 on 5/31/2018.
 */

public class HeroPerk{
	
	public enum Perk{
		NONE(				"none",					0x00),
		DRUNKARD(			"drunkard", 			0x01),	// 酒徒：喝酒不会受伤，也不会醉
		CRITICAL_STRIKE(	"critical_strike", 		0x02),	// 致命一击：额外的暴击率
		KEEN(				"keen", 				0x04),	// 敏锐：更容易发现隐藏
		FEARLESS(			"fearless", 			0x08),	// 无畏：不会在血量低的时候因受伤加压
		NIGHT_VISION(		"night_vision", 		0x10),	// 夜视：额外的视野
		SHOOTER(			"shooter",				0x20),	// 射手：远程武器额外的攻击
		;
		
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
	
	public HeroPerk add(Perk p){
		perk	|=	p.mask;
		return this;
	}
	public boolean contain(Perk p){
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
