package com.egoal.darkestpixeldungeon.items.potions;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.HighlyToxicGas;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

/**
 * Created by 93942 on 4/25/2018.
 */

public class PotionOfHighlyToxicGas extends Potion{
	{
		initials	=	12;
	}

	@Override
	public void shatter(int cell){
		if (Dungeon.visible[cell]) {
			setKnown();

			splash( cell );
			Sample.INSTANCE.play( Assets.SND_SHATTER );
		}

		// longer, sharper
		GameScene.add( Blob.seed( cell, 1200, HighlyToxicGas.class));
	}
	
	@Override
	public void reset(){
		image	=	ItemSpriteSheet.DPD_HIGHLY_TOXIC_POTION;
		color	=	"drakgreen";
	}
	
	public boolean isKnown(){ return true; }
	public void setKnown(){}
}
