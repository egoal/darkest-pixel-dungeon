package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import android.util.Log;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.HeroPerk;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.ShopkeeperSprite;
import com.egoal.darkestpixeldungeon.ui.ItemSlot;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.IconTitle;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.egoal.darkestpixeldungeon.windows.WndTradeItem;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by 93942 on 8/18/2018.
 */

//* the new transaction system
public class DPDShopKeeper extends NPC{
	
	{
		spriteClass	=	ShopkeeperSprite.class;
		
		properties.add(Property.IMMOVABLE);
	}
	
	private static final int MAX_ITEMS	=	20;
	
	protected ArrayList<Item> items_	=	new ArrayList<>();
	
	public boolean addItemToSell(Item item){
		if(items_.contains(item))
			return true;
		
//		if(item.stackable){
//			for(Item i: items_){
//				if(i.isSimilar(item)){
//					i.quantity(i.quantity()+item.quantity());
//					return true;
//				}
//			}
//		}
		
		if(items_.size()<MAX_ITEMS){
			items_.add(item);
			Collections.sort(items_, Item.itemComparator);
		}else{
			Log.e("dpd", "cannot add to shopper.");
			return false;
		}
		
		return false;
	}
	
	// called when sold
	public boolean removeItemFromSell(Item item){
		return items_.remove(item);
	}
	
	@Override
	protected boolean act(){
		throwItem();
		
		sprite.turnTo(pos, Dungeon.hero.pos);
		spend(TICK);
		return true;
	}

	@Override
	public int takeDamage(Damage dmg){
		flee();
		return 0;
	}

	@Override
	public void add( Buff buff ) {}

	@Override
	public boolean reset() {
		return true;
	}
	
	// interact
	@Override
	public boolean interact(){
		GameScene.show(new WndShop(this));
		
		return false;
	}
	
	public String greeting(){ return Messages.get(this, "greeting"); }
	
	// actions
	private void flee(){
		destroy();
		sprite.killAndErase();
		CellEmitter.get(pos).burst(ElmoParticle.FACTORY, 6);
	}
	
	protected void onPlayerSell(){
		sellAny();
	}
	protected void onPlayerBuy(){
		if(items_.isEmpty()){
			GLog.w(Messages.get(this, "nothing_more"));
			return;
		}
		GameScene.show(new WndSellItems(this));
	}

	protected WndBag sellAny(){
		return GameScene.selectItem(selectorSellAny, WndBag.Mode.FOR_SALE, 
			Messages.get(DPDShopKeeper.class, "select_to_sell"));
	}
	
	// player sell to seller
	private WndBag.Listener selectorSellAny	=	new WndBag.Listener(){
		@Override
		public void onSelect(Item item){
			if(item!=null){
				WndBag wndSell	=	sellAny();
				GameScene.show(new WndTradeItem(item, wndSell){
					@Override
					protected void sell(Item item){
						super.sell(item);
						
						addItemToSell(item);
					}
				});
			}
		}
	};
	
	private static final String ITEMS	=	"items";
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(ITEMS, items_);
	}
	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		for(Bundlable item: bundle.getCollection(ITEMS))
			if(item!=null)
				addItemToSell((Item)item);
	}
	
	// interaction
	public static class WndShop extends Window{
		private static final int WIDTH	=	120;
		private static final int BTN_HEIGHT	=	20;
		private static final float GAP	=	2;
		
		public WndShop(final DPDShopKeeper sk){
			super();

			IconTitle it	=	new IconTitle();
			it.icon(sk.sprite());
			it.label(sk.name);
			it.setRect(0, 0, WIDTH, 0);
			add(it);

			RenderedTextMultiline rtmMessage	=	PixelScene.renderMultiline(sk.greeting(), 6);
			rtmMessage.maxWidth(WIDTH);
			rtmMessage.setPos(0f, it.bottom()+GAP);
			add(rtmMessage);
			
			// action buttons
			RedButton btnBuy	=	new RedButton(Messages.get(DPDShopKeeper.class, "buy")){
				@Override
				protected void onClick(){
					hide();
					sk.onPlayerBuy();
				}
			};
			btnBuy.setRect(0, rtmMessage.bottom()+GAP, WIDTH, BTN_HEIGHT);
			add(btnBuy);
			
			RedButton btnSell	=	new RedButton(Messages.get(DPDShopKeeper.class, "sell")){
				@Override
				protected void onClick(){
					hide();
					sk.onPlayerSell();
				}
			};
			btnSell.setRect(0, btnBuy.bottom()+GAP, WIDTH, BTN_HEIGHT);
			add(btnSell);
			
			resize(WIDTH, (int)btnSell.bottom());
		}
	}
	
	public static class WndSellItems extends Window{
		private static final int SLOT_SIZE	=	20;
		private static final int SLOT_MARGIN	=	1;
		private static final float GAP	=	2;
		private static final int PRICE_HEIGHT	=	10;
		
		private static final int SLOT_COLS	=	5;
		
		private static final int WIDTH	=	(SLOT_SIZE+SLOT_MARGIN)*SLOT_COLS;

		private DPDShopKeeper sk_;

		public WndSellItems(DPDShopKeeper sk){
			super();

			sk_	=	sk;
			
			IconTitle it	=	new IconTitle();
			it.icon(sk.sprite());
			it.label(sk.name);
			it.setRect(0, 0, WIDTH, 0);
			add(it);
			
			// add items
			int btm	=	placeItems(sk, it.bottom()+GAP);
			
			resize(WIDTH, btm);
		}
		
		private int placeItems(DPDShopKeeper sk, float top){
			int i	=	0;
			for(Item item: sk.items_){
				int r	=	i/SLOT_COLS;
				int c	=	i%SLOT_COLS;
				
				ItemButton ib	=	new ItemButton(item){
					@Override
					protected void onClick(){
						if(onPlayerWantBuy(item))
							enable(false);
					}
				};
				ib.setPos(c*(SLOT_SIZE+SLOT_MARGIN), top+r*(SLOT_SIZE+PRICE_HEIGHT+SLOT_MARGIN));
				add(ib);
				
				BitmapText bt	=	new BitmapText(PixelScene.pixelFont);				
				bt.text(Integer.toString(buyPrice(item)));
				bt.measure();
				bt.hardlight(0xFFFF00);
				bt.x	=	ib.centerX()-bt.width()/2;
				bt.y	=	ib.bottom()+GAP;
				add(bt);
				
				++i;
			}
			
			return (int)top+((i-1)/SLOT_COLS+1)*(SLOT_SIZE+PRICE_HEIGHT+SLOT_MARGIN);
		}
		
		private boolean onPlayerWantBuy(Item item){
			int price	=	buyPrice(item);
			
			if(price>Dungeon.gold){
				GLog.w(Messages.get(sk_, "no_enough_gold"));
				return false;
			}else{
				Hero hero	=	Dungeon.hero;
				Dungeon.gold	-=	price;
				
				sk_.removeItemFromSell(item);
				
				if(!item.doPickUp(hero))
					Dungeon.level.drop(item, hero.pos).sprite.drop();
				
				return true;
			}
		}
		
		private int buyPrice(Item item){
			return (int)(item.sellPrice()*
				(Dungeon.hero.heroPerk.contain(HeroPerk.Perk.SHREWD)? .75: 1.));
		}
		
		private static class ItemButton extends ItemSlot{
			private static final int BG_COLOR	=	0x9953564D;
			
			private ColorBlock bg;
			
			public ItemButton(Item item){
				super(item);
				
				width	=	SLOT_SIZE;
				height	=	SLOT_SIZE;
			}
			
			//! create children is called in super constructor.
			@Override
			protected void createChildren(){
				bg	=	new ColorBlock(SLOT_SIZE, SLOT_SIZE, BG_COLOR);
				add(bg);

				super.createChildren();
			}
			
			// layout is called when setPos, setSize, and so on.
			@Override
			protected void layout(){
				super.layout();
				
				bg.x	=	x;
				bg.y	=	y;
			}

			@Override
			protected void onTouchDown() {
				bg.brightness( 1.5f );
				Sample.INSTANCE.play( Assets.SND_CLICK, 0.7f, 0.7f, 1.2f );
			};

			protected void onTouchUp() {
				bg.brightness( 1.0f );
			};
			
		}
	}
	
}

