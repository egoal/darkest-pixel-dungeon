package com.egoal.darkestpixeldungeon.scenes;


import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.Archs;
import com.egoal.darkestpixeldungeon.ui.ExitButton;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.watabou.noosa.Camera;
import com.watabou.noosa.RenderedText;

/**
 * Created by 93942 on 5/27/2018.
 */

public class GuideScene extends PixelScene{
	
	@Override
	public void create(){
		super.create();
		
		int w	=	Camera.main.width;
		int h	=	Camera.main.height;

		// title
		RenderedText title	=	renderText( Messages.get(this, "title"), 9 );
		title.hardlight(Window.TITLE_COLOR);
		title.x = (w - title.width()) / 2 ;
		title.y = 4;
		align(title);
		add(title);
		
		// exit
		ExitButton btnExit	=	new ExitButton();
		btnExit.setPos(w - btnExit.width(), 0);
		add( btnExit );
		
		// background
		Archs archs	=	new Archs();
		archs.setSize(Camera.main.width, Camera.main.height);
		addToBack(archs);
		
		fadeIn();
	}

	@Override
	protected void onBackPressed() {
		DarkestPixelDungeon.switchNoFade(TitleScene.class);
	}

}
