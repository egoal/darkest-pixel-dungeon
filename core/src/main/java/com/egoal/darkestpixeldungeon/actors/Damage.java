package com.egoal.darkestpixeldungeon.actors;

/**
 * Created by 93942 on 4/30/2018.
 */

// data structure of Damage
// my work truely start from here
	
public class Damage{
	
	public enum Type{
		NORMAL, 
		MAGICAL, 
		MENTAL, 
	}
	
	public static class Element{
		public static final int NONE	=	0x0000;
		public static final int FIRE	=	0x0001;
		public static final int POISON	=	0x0002;
		public static final int ICE		=	0x0004;
		public static final int LIGHT	=	0x0008;	// this should be lightning...
		public static final int SHADOW	=	0x0010;
		public static final int ACID	=	0x0020;
		public static final int HOLY	=	0x0040;	// this should use name: light
		
		public static final int ELEMENT_COUNT	=	7;
		
		public static int all(){
			int a	=	0;
			for(int i=0; i<ELEMENT_COUNT; ++i)
				a |= 0x01<< i;
			return a;
		}
	}
	
	public static class Feature{
		public static final int NONE		=	0x0000;
		public static final int CRITCIAL	=	0x0001;
		public static final int ACCURATE	=	0x0002;
		public static final int PURE		=	0x0004;
		public static final int DEATH		=	0x0008;
		public static final int RANGED		=	0x0010;
		
		public static final int FEATURE_COUNT	=	5;

		public static int all(){
			int a	=	0;
			for(int i=0; i<FEATURE_COUNT; ++i)
				a |= 0x01<< i;
			return a;
		}
	}
	
	// attributes
	public int value	=	0;
	public Type type	=	Type.NORMAL;
	public int element	=	Element.NONE;
	public int feature	=	Feature.NONE;
	public Object from	=	null;
	public Object to	=	null;
	
	public Damage(int v, Object from, Object to){
		value	=	v;
		this.from	=	from;
		this.to	=	to;
	}
	
	public Damage type(Type t){
		type	=	t;
		return this;
	}
	public Damage addElement(int e){
		element	=	element| e;
		return this;
	}
	public Damage addFeature(int f){
		feature	=	feature|f;
		return this;
	}
	
	public boolean isFeatured(int f){
		return (feature& f)!=0;
	}
	public boolean hasElement(int e){ return (element& e)!=0; }
}
