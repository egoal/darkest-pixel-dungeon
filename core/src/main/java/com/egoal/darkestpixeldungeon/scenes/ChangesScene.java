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

public class ChangesScene extends PixelScene {

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
		RenderedTextMultiline text	=	renderMultiline(Messages.get(this, "info"), 6 );
		text.maxWidth((int) panel.innerWidth());
		content.add(text);
		content.setSize( panel.innerWidth(), text.height() );

		//todo: add version buttons

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
				parent.add(new ChangesWindow("this is a test message: "+version));
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


