package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Bundle;

/**
 * Created by 93942 on 5/30/2018.
 */

public class Statuary extends NPC{
	
	{
		spriteClass	=	StatuarySprite.class;
	}
	
	public enum Type{
		ANGEL("angel"), DEVIL("devil");
		
		public String title;
		Type(String t){
			title	=	t;
		}
	}
	
	private Type type	=	Type.ANGEL;
	
	
	private static final String TYPE	=	"TYPE";
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(TYPE, type.toString());
	}
	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		String value	=	bundle.getString(TYPE);
		type(value.length()>0? Type.valueOf(value): Type.ANGEL);
	}
	
	
	public Statuary type(Type t){
		type	=	t;
		name	=	Messages.get(this, "name_"+type.title);
		
		return this;
	}
	
	@Override
	public String description(){
		return Messages.get(this, "desc_"+type.title);
	}

	@Override
	public boolean interact(){
		return false;
	}
	
	// sprite class
	public static class StatuarySprite extends MobSprite{
		
		public StatuarySprite(){
			super();

			texture(Assets.DPD_JESSICA);

			TextureFilm frames=new TextureFilm(texture,12,14);

			idle=new Animation(10,true);
			
			idle.frames(frames,0);

			run=new Animation(20,true);
			run.frames(frames,0);

			die=new Animation(20,false);
			die.frames(frames,0);

			play(idle);
		}
	}
	
}
