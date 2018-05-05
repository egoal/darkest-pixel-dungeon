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
	
	public class Feature{
		public static final int NONE		=	0x0000;
		public static final int CRITICAL	=	0x0001;
		public static final int EXACTLY		=	0x0002;	// cannot miss
		
		public static final int FIRE		=	0x0010;
		public static final int POISON		=	0x0020;
		public static final int ICE			=	0x0040;
		public static final int LIGHT		=	0x0080;
	}
	
	// attributes
	public int value	=	0;
	public Type type	=	Type.PHYSICAL;
	public int feature	=	Feature.NONE;
	public Object from	=	null;
	public Object to	=	null;
	
	public Damage(int v, Object from, Object to){
		value	=	v;
		this.from	=	from;
		this.to	=	to;
	}
	
	public Damage setType(Type t){
		type	=	t;
		return this;
	}
	public Damage addFeature(int f){
		feature	=	feature|f;
		return this;
	}
	public boolean isFeatured(int f){
		return (feature&f) !=0;
	}
}
