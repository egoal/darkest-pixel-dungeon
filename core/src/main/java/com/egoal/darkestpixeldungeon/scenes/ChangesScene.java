/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.scenes;

import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.ui.ScrollPane;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.Chrome;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.Archs;
import com.egoal.darkestpixeldungeon.ui.ExitButton;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.windows.WndMessage;
// import com.sun.prism.Image;
import com.watabou.input.Touchscreen;
import com.watabou.noosa.Camera;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.ui.Component;
import com.watabou.noosa.TouchArea;

import java.lang.annotation.Inherited;
import java.util.ArrayList;

public class ChangesScene extends PixelScene {

	private static final int BTN_WIDTH	=	30;
	private static final int BTN_HEIGHT	=	15;
	private static final int BTN_GAP	=	2;
	
	@Override
	public void create() {
		super.create();

		int w = Camera.main.width;
		int h = Camera.main.height;

		RenderedText title = renderText( Messages.get(this, "title"), 9 );
		title.hardlight(Window.TITLE_COLOR);
		title.x = (w - title.width()) / 2 ;
		title.y = 4;
		align(title);
		add(title);

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );

		
		// add chrome base
		int pw = w - 6;
		int ph = h - 20;
		
		NinePatch panel = Chrome.get(Chrome.Type.WINDOW);
		panel.size( pw, ph );
		panel.x = (w - pw) / 2;
		panel.y = title.y + title.height() + 2;
		add( panel );
		
		// add scroll text
		ScrollPane list = new ScrollPane( new Component() );
		add( list );
		
		Component content = list.content();
		content.clear();
		
		//Messages.get(this, "warning")+"
		RenderedTextMultiline text	=	renderMultiline("_"+
			DarkestPixelDungeon.version+"_\n"+
			Messages.get(this, "info"+DarkestPixelDungeon.version), 6 );
		text.maxWidth((int) panel.innerWidth());
		content.add(text);

		// add versions' button
		final String HSPLIT	=	"---";
		String[] oldVersions	=	new String[]{
			"0.2.0","", "",
			HSPLIT,
			"0.1.3", "0.1.2", "0.1.1", 
			"0.1.0", "", ""
		};
		{
			// todo: code lint
			float sx	=	0f;
			float sy	=	text.height()+8;
			int r	=	0;
			int c	=	0;
			int gaps	=	0;
			for(String v: oldVersions){
				if(v.equals(HSPLIT)){
					++gaps;
					continue;
				}
				
				if(v.length()>0){
					RedButton rb	=	createChangeButton(v);
					rb.setRect(sx+(BTN_WIDTH+BTN_GAP)*c,sy+(BTN_GAP+BTN_HEIGHT)*r+gaps*BTN_GAP,
							BTN_WIDTH,BTN_HEIGHT);
					content.add(rb);
				}
				
				if((++c)==3){
					++r;
					c	=	0;
				}
			}
			content.setSize(panel.innerWidth(), sy+(BTN_GAP+BTN_HEIGHT)*r+BTN_HEIGHT);
		}
		
		list.setRect(
				panel.x + panel.marginLeft(),
				panel.y + panel.marginTop(),
				panel.innerWidth(),
				panel.innerHeight());
		list.scrollTo(0, 0);

		Archs archs = new Archs();
		archs.setSize( Camera.main.width, Camera.main.height );
		addToBack( archs );

		fadeIn();
	}

	@Override
	protected void onBackPressed() {
		DarkestPixelDungeon.switchNoFade(TitleScene.class);
	}

	private RedButton createChangeButton(final String version){
		RedButton btnVersion	=	new RedButton(version){
			@Override
			protected void onClick(){
				parent.add(new ChangesWindow(
						Messages.get(ChangesScene.class, "info"+version)));
			}
		};

		return btnVersion;
	}

	private static class ChangesWindow extends WndMessage{
		public ChangesWindow(String message){
			super(message);

			add(new TouchArea(chrome){
				@Override
				protected void onClick(Touchscreen.Touch touch){
					hide();	
				}
			});
		}
	}
}


