package com.egoal.darkestpixeldungeon.levels;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.CatLix;

import java.util.Arrays;

public class VillageLevel extends RegularLevel{

	@Override
	protected boolean[] water(){
		boolean[] nowater   =   new boolean[length];

		Arrays.fill(nowater, false);
		return nowater;
	}

	@Override
	protected boolean[] grass(){
		boolean[] noGrass   =   new boolean[length];
		Arrays.fill(noGrass, false);

		return noGrass;
	}

	@Override
	protected void decorate(){
		// do nothing
	}

	@Override
	protected void createMobs(){
		if(Dungeon.depth==1){
			CatLix cl=new CatLix();
			do{
				cl.pos=pointToCell(roomEntrance.random());
			}while(map[cl.pos]==Terrain.ENTRANCE||map[cl.pos]==Terrain.SIGN);
			mobs.add(cl);
		}

		super.createMobs();
	}

	@Override
	protected void createItems(){
		super.createItems();
	}
}
