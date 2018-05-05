package com.egoal.darkestpixeldungeon.sprites;

import android.text.style.AbsoluteSizeSpan;

import com.egoal.darkestpixeldungeon.Assets;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/**
 * Created by 93942 on 5/5/2018.
 */

public class JessicaSprite extends MobSprite{
	
	public JessicaSprite(){
		super();
		
		texture(Assets.DPD_JESSICA);

		// set animations
		TextureFilm frames	=	new TextureFilm(texture, 12, 15);
		idle	=	new MovieClip.Animation(1, true);
		idle.frames(frames, 0, 1);

		run	=	new MovieClip.Animation(20, true);
		run.frames(frames, 0);

		die	=	new MovieClip.Animation(20, true);
		die.frames(frames, 0);

		play(idle);
	}
}
