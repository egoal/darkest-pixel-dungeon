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
package com.egoal.darkestpixeldungeon.items;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndOptions;


public abstract class KindofMisc extends EquipableItem {

	private static final float TIME_TO_EQUIP = 1f;

	@Override
	public boolean doEquip(final Hero hero) {

		if (hero.belongings.misc1 != null && hero.belongings.misc2 != null && 
			hero.belongings.misc3!=null){

			final KindofMisc m1 = hero.belongings.misc1;
			final KindofMisc m2 = hero.belongings.misc2;
			final KindofMisc m3	=	hero.belongings.misc3;

			GameScene.show(
					new WndOptions(Messages.get(KindofMisc.class, "unequip_title"),
							Messages.get(KindofMisc.class, "unequip_message"),
							Messages.titleCase(m1.toString()),
							Messages.titleCase(m2.toString()), 
							Messages.titleCase(m3.toString())) {

						@Override
						protected void onSelect(int index) {
							KindofMisc equipped	=	null;
							switch(index){
							case 0:
								equipped	=	m1;
								break;
							case 1:
								equipped	=	m2;
								break;
							case 2:
								equipped	=	m3;
								break;
							}
							detach( hero.belongings.backpack );
							if (equipped.doUnequip(hero, true, false)) {
								execute(hero, AC_EQUIP);
							} else {
								collect( hero.belongings.backpack );
							}
						}
					});

			return false;

		} else {

			if (hero.belongings.misc1 == null) {
				hero.belongings.misc1 = this;
			} else if(hero.belongings.misc2==null){
				hero.belongings.misc2 = this;
			}else{
				hero.belongings.misc3	=	this;
			}

			detach( hero.belongings.backpack );

			activate( hero );

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(this, "cursed", this) );
			}

			hero.spendAndNext( TIME_TO_EQUIP );
			return true;

		}

	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){

			if (hero.belongings.misc1 == this) {
				hero.belongings.misc1 = null;
			} else if(hero.belongings.misc2==this){
				hero.belongings.misc2 = null;
			}else{
				hero.belongings.misc3	=	null;
			}

			return true;

		} else {

			return false;

		}
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		return hero.belongings.misc1 == this || hero.belongings.misc2 == this
			|| hero.belongings.misc3==this;
	}

}
