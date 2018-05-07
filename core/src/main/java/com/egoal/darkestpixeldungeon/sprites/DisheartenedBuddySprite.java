package com.egoal.darkestpixeldungeon.sprites;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.DisheartenedBuddy;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.TextureFilm;

/**
 * Created by 93942 on 5/8/2018.
 */

public class DisheartenedBuddySprite extends MobSprite{
	
	public DisheartenedBuddySprite(){
		super();
		
		texture(Assets.DPD_NOVE);

		TextureFilm frames	=	new TextureFilm(texture, 12, 15);
		
		idle	=	new Animation(1, true);
		idle.frames(frames, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1);
		
		run	=	new Animation(20, true);
		run.frames(frames, 0);
		
		die	=	new Animation(20, false);
		die.frames(frames, 0);
		
		play(idle);
	}
	
}
