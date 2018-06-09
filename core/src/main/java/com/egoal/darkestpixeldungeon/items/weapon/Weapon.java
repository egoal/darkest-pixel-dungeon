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
package com.egoal.darkestpixeldungeon.items.weapon;

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Dazzling;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Holy;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Projecting;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.KindOfWeapon;
import com.egoal.darkestpixeldungeon.items.rings.RingOfFuror;
import com.egoal.darkestpixeldungeon.items.rings.RingOfSharpshooting;
import com.egoal.darkestpixeldungeon.items.weapon.curses.Annoying;
import com.egoal.darkestpixeldungeon.items.weapon.curses.Displacing;
import com.egoal.darkestpixeldungeon.items.weapon.curses.Exhausting;
import com.egoal.darkestpixeldungeon.items.weapon.curses.Fragile;
import com.egoal.darkestpixeldungeon.items.weapon.curses.Sacrificial;
import com.egoal.darkestpixeldungeon.items.weapon.curses.Wayward;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Blazing;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Chilling;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Eldritch;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Grim;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Lucky;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Shocking;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Stunning;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Unstable;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Vampiric;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Venomous;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Vorpal;
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.egoal.darkestpixeldungeon.levels.features.Chasm;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import javax.microedition.khronos.opengles.GL;

abstract public class Weapon extends KindOfWeapon {

	private static final int HITS_TO_KNOW    = 20;

	private static final String TXT_TO_STRING		= "%s :%d";

	public float    ACC = 1f;	// Accuracy modifier
	public float	DLY	= 1f;	// Speed modifier
	public int      RCH = 1;    // Reach modifier (only applies to melee hits)

	// 灌注
	public enum Imbue {
		NONE	(1.0f, 1.00f),
		LIGHT	(0.7f, 0.67f),
		HEAVY	(1.5f, 1.67f);

		private float damageFactor;
		private float delayFactor;

		Imbue(float dmg, float dly){
			damageFactor = dmg;
			delayFactor = dly;
		}

		public int damageFactor(int dmg){
			return Math.round(dmg * damageFactor);
		}

		public float delayFactor(float dly){
			return dly * delayFactor;
		}
	}
	public Imbue imbue = Imbue.NONE;

	private int hitsToKnow = HITS_TO_KNOW;
	
	public Enchantment enchantment;
	
	@Override
	public Damage proc(Damage dmg){
		if(enchantment!=null)
			dmg	=	enchantment.proc(this, dmg);
		
		if(!levelKnown){
			if(--hitsToKnow<=0){
				levelKnown	=	true;
				GLog.i(Messages.get(Weapon.class, "identify"));
				Badges.validateItemLevelAquired(this);
			}
		}
		
		return dmg;
	}

	private static final String UNFAMILIRIARITY	= "unfamiliarity";
	private static final String ENCHANTMENT		= "enchantment";
	private static final String IMBUE			= "imbue";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( UNFAMILIRIARITY, hitsToKnow );
		bundle.put( ENCHANTMENT, enchantment );
		bundle.put( IMBUE, imbue );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if ((hitsToKnow = bundle.getInt( UNFAMILIRIARITY )) == 0) {
			hitsToKnow = HITS_TO_KNOW;
		}
		enchantment = (Enchantment)bundle.get( ENCHANTMENT );
		imbue = bundle.getEnum( IMBUE, Imbue.class );
	}
	
	@Override
	public float accuracyFactor( Hero hero ) {
		
		int encumbrance = STRReq() - hero.STR();

		if (hasEnchant(Wayward.class))
			encumbrance = Math.max(3, encumbrance+3);

		float ACC = this.ACC;
		
		if (this instanceof MissileWeapon) {
			int bonus = RingOfSharpshooting.getBonus(hero, RingOfSharpshooting.Aim.class);
			ACC *= (float)(Math.pow(1.2, bonus));
		}

		return encumbrance > 0 ? (float)(ACC / Math.pow( 1.5, encumbrance )) : ACC;
	}
	
	@Override
	public float speedFactor( Hero hero ) {

		int encumrance = STRReq() - hero.STR();
		if (this instanceof MissileWeapon && hero.heroClass == HeroClass.HUNTRESS) {
			encumrance -= 2;
		}

		float DLY = imbue.delayFactor(this.DLY);

		int bonus = RingOfFuror.getBonus(hero, RingOfFuror.Furor.class);

		DLY = (float)(0.25 + (DLY - 0.25)*Math.pow(0.8, bonus));

		return (encumrance > 0 ? (float)(DLY * Math.pow( 1.2, encumrance )) : DLY);
	}

	@Override
	public int reachFactor(Hero hero) {
		return hasEnchant(Projecting.class) ? RCH+1 : RCH;
	}

	@Override
	public Damage giveDamage(Hero hero, Char target){
		Damage dmg	=	super.giveDamage(hero, target).addFeature(Damage.Feature.RANGED);
		
		// extra damage
		int exStr	=	hero.STR()-STRReq();
		if(exStr>0){
			dmg.value	+=	exStr;

			// huntress perk
			if(this instanceof MissileWeapon && hero.heroClass==HeroClass.HUNTRESS)
				dmg.value	+=	Random.Int(exStr)+1;
		}
		
		dmg.value	=	imbue.damageFactor(dmg.value);
		return dmg;
	}

	public int STRReq(){
		return STRReq(level());
	}

	public abstract int STRReq(int lvl);
	
	public Item upgrade( boolean enchant ) {

		if (enchant && (enchantment == null || enchantment.curse())){
			enchant( Enchantment.random() );
		} else if (!enchant && Random.Float() > Math.pow(0.9, level())){
			enchant(null);
		}
		
		return super.upgrade();
	}
	
	@Override
	public String name() {
		return enchantment != null && (cursedKnown || !enchantment.curse()) ? enchantment.name( super.name() ) : super.name();
	}
	
	@Override
	public Item random() {
		float roll = Random.Float();
		if (roll < 0.3f){
			//30% chance to be level 0 and cursed
			enchant(Enchantment.randomCurse());
			cursed = true;
			return this;
		} else if (roll < 0.75f){
			//45% chance to be level 0
		} else if (roll < 0.95f){
			//15% chance to be +1
			upgrade(1);
		} else {
			//5% chance to be +2
			upgrade(2);
		}

		//if not cursed, 10% chance to be enchanted (7% overall)
		if (Random.Int(10) == 0)
			enchant();

		return this;
	}
	
	public Weapon enchant( Enchantment ench ) {
		enchantment = ench;
		return this;
	}

	public Weapon enchant() {

		Class<? extends Enchantment> oldEnchantment = enchantment != null ? enchantment.getClass() : null;
		Enchantment ench = Enchantment.random();
		while (ench.getClass() == oldEnchantment) {
			ench = Enchantment.random();
		}

		return enchant( ench );
	}

	public boolean hasEnchant(Class<?extends Enchantment> type) {
		return enchantment != null && enchantment.getClass() == type;
	}

	public boolean hasGoodEnchant(){
		return enchantment != null && !enchantment.curse();
	}

	public boolean hasCurseEnchant(){		return enchantment != null && enchantment.curse();
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return enchantment != null && (cursedKnown || !enchantment.curse()) ? enchantment.glowing() : null;
	}

	public static abstract class Enchantment implements Bundlable {

		private static final Class<?>[] enchants = new Class<?>[]{
			Blazing.class, Venomous.class, Vorpal.class, Shocking.class,
			Chilling.class, Eldritch.class, Lucky.class, Projecting.class, Unstable.class, Dazzling.class,
			Grim.class, Stunning.class, Vampiric.class, Holy.class};
		private static final float[] chances= new float[]{
			10, 10, 10, 10,
			5, 5, 5, 5, 5, 5,
			2, 2, 2, 1 };

		private static final Class<?>[] curses = new Class<?>[]{
				Annoying.class, Displacing.class, Exhausting.class, Fragile.class, Sacrificial.class, Wayward.class
		};
		
		public abstract Damage proc(Weapon weapon, Damage damage);
		// public abstract int proc( Weapon weapon, Char attacker, Char defender, int damage );

		public String name() {
			if (!curse())
				return name( Messages.get(this, "enchant"));
			else
				return name( Messages.get(Item.class, "curse"));
		}

		public String name( String weaponName ) {
			return Messages.get(this, "name", weaponName);
		}

		public String desc() {
			return Messages.get(this, "desc");
		}

		public boolean curse() {
			return false;
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
		}
		
		public abstract ItemSprite.Glowing glowing();
		
		@SuppressWarnings("unchecked")
		public static Enchantment random() {
			try {
				return ((Class<Enchantment>)enchants[ Random.chances( chances ) ]).newInstance();
			} catch (Exception e) {
				DarkestPixelDungeon.reportException(e);
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		public static Enchantment randomCurse(){
			try {
				return ((Class<Enchantment>)Random.oneOf(curses)).newInstance();
			} catch (Exception e) {
				DarkestPixelDungeon.reportException(e);
				return null;
			}
		}
		
	}
}
