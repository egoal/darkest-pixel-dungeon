package com.egoal.darkestpixeldungeon.actors;

/**
 * Created by 93942 on 4/30/2018.
 */

// data structure of Damage
// my work truely start from here
	
public class Damage{
	public enum Type{
		PHYSICAL, 
		MAGICAL, 
		MENTAL, 
		PURELY, 
	}
	
	public class Category{
		public static final int NORMAL		=	0x01;
		public static final int CRITICAL	=	0x02;
		public static final int FIRE		=	0x04;
		public static final int POISON		=	0x08;
	}
	
	// attributes
	public int value	=	0;
	public Type type	=	Type.PHYSICAL;
	public int category	=	Category.NORMAL;
	public Char caster	=	null;
	public Char target	=	null;
	
	public Damage(int v){
		value	=	v;
	}
	public Damage(int v, Type t, int c){
		value	=	v;
		type	=	t;
		category	=	c;
	}
	public Damage(int v, Type t, int c, Char from, Char to){
		value	=	v;
		type	=	t;
		category	=	c;
		caster	=	from;
		target	=	to;
	}
}
