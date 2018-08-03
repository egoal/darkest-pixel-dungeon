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
package com.egoal.darkestpixeldungeon.actors;

import com.egoal.darkestpixeldungeon.actors.buffs.Bless;
import com.egoal.darkestpixeldungeon.actors.buffs.Chill;
import com.egoal.darkestpixeldungeon.actors.buffs.Frost;
import com.egoal.darkestpixeldungeon.actors.buffs.LifeLink;
import com.egoal.darkestpixeldungeon.actors.buffs.MustDodge;
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Charm;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.actors.buffs.EarthImbue;
import com.egoal.darkestpixeldungeon.actors.buffs.FireImbue;
import com.egoal.darkestpixeldungeon.actors.buffs.MagicalSleep;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.Slow;
import com.egoal.darkestpixeldungeon.actors.buffs.Speed;
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo;
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.features.Door;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.HashMap;
import java.util.HashSet;

public abstract class Char extends Actor {
	
	public int pos = 0;
	
	public CharSprite sprite;
	
	public String name = "mob";
	
	public int HT;	// max hp
	public int HP;
	public int SHLD;
	
	protected float baseSpeed	= 1;
	protected PathFinder.Path path;

	public int paralysed	    = 0;
	public boolean rooted		= false;
	public boolean flying		= false;
	public int invisible		= 0;
	
	public int viewDistance =   8;
	public int seeDistance  =   8;
	
	private HashSet<Buff> buffs = new HashSet<>();
	
	@Override
	protected boolean act() {
		Dungeon.level.updateFieldOfView( this, Level.fieldOfView );
		return false;
	}
	
	private static final String POS			= "pos";
	private static final String TAG_HP		= "HP";
	private static final String TAG_HT		= "HT";
	private static final String TAG_SHLD    = "SHLD";
	private static final String BUFFS		= "buffs";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );
		
		bundle.put( POS, pos );
		bundle.put( TAG_HP, HP );
		bundle.put( TAG_HT, HT );
		bundle.put( TAG_SHLD, SHLD );
		bundle.put( BUFFS, buffs );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );
		
		pos = bundle.getInt( POS );
		HP = bundle.getInt( TAG_HP );
		HT = bundle.getInt( TAG_HT );
		SHLD = bundle.getInt( TAG_SHLD );
		
		for (Bundlable b : bundle.getCollection( BUFFS )) {
			if (b != null) {
				((Buff)b).attachTo( this );
			}
		}
	}
	
	public boolean attack(Char enemy){
		if(enemy==null || !enemy.isAlive()) return false;
		
		boolean visibleFight	=	Dungeon.visible[pos] || Dungeon.visible[enemy.pos];
		
		Damage dmg	=	giveDamage(enemy);
		if(enemy.checkHit(dmg)){
			
			// enemy armor defense
			
			// sniper's perk, todo: use pure damage instead
			if(this instanceof Hero && ((Hero)this).rangedWeapon!=null && ((Hero)this).subClass==HeroSubClass.SNIPER){
				// sniper's perk: ignore defence
			}else if(!dmg.isFeatured(Damage.Feature.PURE))
				dmg	=	enemy.defendDamage(dmg);
			
			dmg	=	attackProc(dmg);
			dmg	=	enemy.defenseProc(dmg);
			
			// todo: use more sfx
			if(visibleFight)
				Sample.INSTANCE.play(Assets.SND_HIT, 1, 1, Random.Float(0.8f, 1.25f));
			
			// may died in proc
			if(!enemy.isAlive()) return true;
			
			// camera shake, todo: add more effects here
			float shake	=	0f;
			if(enemy==Dungeon.hero)
				shake	=	dmg.value/(enemy.HT/4);
			if(shake>1f)
				Camera.main.shake(GameMath.gate(1, shake, 5), .3f);
			
			// take!
			enemy.takeDamage(dmg);
			
			// buffs, dont know why this piece of code exists, 
			// maybe the mage? or the attack effect?
			if(buff(FireImbue.class)!=null)
				buff(FireImbue.class).proc(enemy);
			if(buff(EarthImbue.class)!=null)
				buff(EarthImbue.class).proc(enemy);
			
			// effects
			enemy.sprite.bloodBurstA(sprite.center(), dmg.value);
			enemy.sprite.flash();
			if(!enemy.isAlive() && visibleFight){
				if(enemy==Dungeon.hero){
					// hero die
					Dungeon.fail(getClass());
					GLog.n(Messages.capitalize(Messages.get(Char.class, "kill", name)));
				}else if(this==Dungeon.hero){
					// killed by hero
					Dungeon.hero.onKillChar(enemy);
				}
			}
			
			return true;
		}else{
			// missed
			if(visibleFight){
				String str	=	enemy.defenseVerb();
				enemy.sprite.showStatus(CharSprite.NEUTRAL, str);
				
				Sample.INSTANCE.play(Assets.SND_MISS);
			}
			
			return false;
		}
	}

	public Damage giveDamage(Char enemy){
		// default normal damage
		return new Damage(1, this, enemy);
	}
	public Damage defendDamage(Damage dmg){
		// normal defend, do nothing
		return dmg;
	}

	public boolean checkHit(Damage dmg){
		if(dmg.isFeatured(Damage.Feature.ACCURATE))
			return true;

		MustDodge md	=	buff(MustDodge.class);
		if(md!=null && md.canDodge(dmg))
			return false;
		
		Char attacker	=	(Char)dmg.from;
		Char defender	=	(Char)dmg.to;
		float acuRoll	=	Random.Float(attacker.attackSkill(defender));
		float defRoll	=	Random.Float(defender.defenseSkill(attacker));
		if(attacker.buffs(Bless.class)!=null) acuRoll	*=	1.2f;
		if(defender.buffs(Bless.class)!=null) defRoll	*=	1.2f;
		
		float bonus	=	1.f;
		if(dmg.type==Damage.Type.MAGICAL || dmg.type==Damage.Type.MENTAL)
			bonus	=	2f;
		
		return bonus*acuRoll >= defRoll;
	}
	
	public Damage attackProc(Damage dmg){
		return dmg;
	}
	
	public Damage defenseProc(Damage dmg){
		return dmg;
	}
	
	public void takeDamage(Damage dmg){
		// life link
		LifeLink ll	=	buff(LifeLink.class);
		if(ll!=null){
			Actor a	=	Actor.findById(ll.linker);
			if(a!=null && (a instanceof Char)){
				((Char)a).takeDamage(dmg);
				((Char)a).sprite.showStatus(0x000000, Messages.get(LifeLink.class, "transform"));
				
				return;
			}
		}
		
		// currently, only hero suffer from mental damage
		if(!isAlive() || dmg.value<0 || (dmg.type==Damage.Type.MENTAL && !(this instanceof Hero)))
			return;

		// vulnerable
		Vulnerable v	=	buff(Vulnerable.class);
		if(v!=null){
			dmg.value	*=	v.ratio;
		}
		
		// buffs shall remove when take damage
		if(this.buff(Frost.class)!=null)
			Buff.detach(this, Frost.class);
		if(this.buff(MagicalSleep.class)!=null)
			Buff.detach(this, MagicalSleep.class);
		
		// immunities, resistance 
		if(!dmg.isFeatured(Damage.Feature.PURE))
			dmg	=	resistDamage(dmg);
		
		// buffs when take damage
		if(buff(Paralysis.class)!=null){
			if(Random.Int(dmg.value)>= Random.Int(HP)){
				Buff.detach(this, Paralysis.class);
				if(Dungeon.visible[pos])
					GLog.i(Messages.get(Char.class, "out_of_paralysis", name));
			}
		}
		
		// deal with types
		//todo: the damage number can have different colour refer to the element they carry
		switch(dmg.type){
			case NORMAL:
				// physical
				if(SHLD>=dmg.value)
					SHLD	-=	dmg.value;
				else{
					HP	-=	(dmg.value-SHLD);
					SHLD	=	0;
				}
				break;
			case MAGICAL:
				HP	-=	dmg.value;
				break;
		}
		
		// effects, show number
		if(dmg.value>0 || dmg.from instanceof Char){
			String number	=	Integer.toString(dmg.value);
			if(dmg.isFeatured(Damage.Feature.CRITCIAL))
				number	+=	"!";
			sprite.showStatus(HP>HT/2? CharSprite.WARNING: CharSprite.NEGATIVE, number);
		}
		
		//note: this is a important setting
		if(HP<0)	HP=0;
		
		if(!isAlive())
			die(dmg.from);
		
	}

	public HashMap<Integer, Float > mapResists	=	new HashMap<>();
	protected Damage resistDamage(Damage dmg){
		for(int of=0; of<Damage.Element.ELEMENT_COUNT; ++of){
			int ele	=	1<<of;
			if(dmg.hasElement(ele) && mapResists.containsKey(ele))
				dmg.value	/=	mapResists.get(ele);
		}
		
		return dmg;
	}
	
	// attack or edoge ratio
	public int attackSkill( Char target ) {
		return 0;
	}
	
	public int defenseSkill( Char enemy ) {
		return 0;
	}
	
	public String defenseVerb() {
		return Messages.get(this, "def_verb");
	}

	public float speed() {
		return buff( Cripple.class ) == null ? baseSpeed : baseSpeed * 0.5f;
	}
	
	public void destroy() {
		HP = 0;
		remove( this );
	}
	
	public void die( Object src ) {
		destroy();
		sprite.die();
	}
	
	public boolean isAlive() {
		return HP > 0;
	}
	
	@Override
	protected void spend( float time ) {
		
		float timeScale = 1f;
		if (buff( Slow.class ) != null) {
			timeScale *= 0.5f;
			//slowed and chilled do not stack
		} else if (buff( Chill.class ) != null) {
			timeScale *= buff( Chill.class ).speedFactor();
		}
		if (buff( Speed.class ) != null) {
			timeScale *= 2.0f;
		}
		
		super.spend( time / timeScale );
	}
	
	public HashSet<Buff> buffs() {
		return buffs;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T extends Buff> HashSet<T> buffs( Class<T> c ) {
		HashSet<T> filtered = new HashSet<>();
		for (Buff b : buffs) {
			if (c.isInstance( b )) {
				filtered.add( (T)b );
			}
		}
		return filtered;
	}

	@SuppressWarnings("unchecked")
	public synchronized  <T extends Buff> T buff( Class<T> c ) {
		for (Buff b : buffs) {
			if (c.isInstance( b )) {
				return (T)b;
			}
		}
		return null;
	}

	public boolean isCharmedBy( Char ch ) {
		int chID = ch.id();
		for (Buff b : buffs) {
			if (b instanceof Charm && ((Charm)b).object == chID) {
				return true;
			}
		}
		return false;
	}

	public void add( Buff buff ) {
		
		buffs.add( buff );
		Actor.add( buff );

		if (sprite != null)
			switch(buff.type){
				case POSITIVE:
					sprite.showStatus(CharSprite.POSITIVE, buff.toString()); break;
				case NEGATIVE:
					sprite.showStatus(CharSprite.NEGATIVE, buff.toString());break;
				case NEUTRAL:
					sprite.showStatus(CharSprite.NEUTRAL, buff.toString()); break;
				case SILENT: default:
					break; //show nothing
			}

	}
	
	public void remove( Buff buff ) {
		
		buffs.remove( buff );
		Actor.remove( buff );

	}
	
	public void remove( Class<? extends Buff> buffClass ) {
		for (Buff buff : buffs( buffClass )) {
			remove( buff );
		}
	}
	
	@Override
	protected void onRemove() {
		for (Buff buff : buffs.toArray(new Buff[buffs.size()])) {
			buff.detach();
		}
	}
	
	public void updateSpriteState() {
		for (Buff buff:buffs) {
			buff.fx( true );
		}
	}
	
	public int stealth() {
		return 0;
	}
	
	public void move( int step ) {

		if (Dungeon.level.adjacent( step, pos ) && buff( Vertigo.class ) != null) {
			sprite.interruptMotion();
			int newPos = pos + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
			if (!(Level.passable[newPos] || Level.avoid[newPos]) || findChar( newPos ) != null)
				return;
			else {
				sprite.move(pos, newPos);
				step = newPos;
			}
		}

		if (Dungeon.level.map[pos] == Terrain.OPEN_DOOR) {
			Door.leave( pos, this );
		}

		pos = step;
		
		if (flying && Dungeon.level.map[pos] == Terrain.DOOR) {
			Door.enter( pos, this );
		}
		
		if (this != Dungeon.hero) {
			sprite.visible = Dungeon.visible[pos];
		}
	}
	
	public int distance( Char other ) {
		return Dungeon.level.distance( pos, other.pos );
	}
	
	public void onMotionComplete() {
		//Does nothing by default
		//The main actor thread already accounts for motion,
		// so calling next() here isn't necessary (see Actor.process)
	}
	
	//note: called when the animation is done
	public void onAttackComplete() {
		next();
	}
	public void onOperateComplete() {
		next();
	}

	private static final HashSet<Class<?>> EMPTY	=	new HashSet<>();
	public HashSet<Class<?> > immunizedBuffs(){
		return EMPTY;
	}
	
	protected HashSet<Property> properties = new HashSet<>();

	public HashSet<Property> properties() { return properties; }

	public enum Property{
		BOSS,
		MINIBOSS,
		UNDEAD,
		DEMONIC,
		IMMOVABLE
	}
}
