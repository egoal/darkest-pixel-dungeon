package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ImpSprite;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 8/21/2018.
 */

public class DPDImpShopkeeper extends DPDShopKeeper {
  {
    spriteClass = ImpSprite.class;
  }

  @Override
  public DPDShopKeeper initSellItems() {
    // devil would be place by painter, here, add extra items
    addItemToSell(new PotionOfHealing());


    for (int i = 0; i < 3; i++)
      addItemToSell(Generator.random(Generator.Category.POTION));

    addItemToSell(new ScrollOfIdentify());
    addItemToSell(new ScrollOfRemoveCurse());
    addItemToSell(new ScrollOfMagicMapping());
    addItemToSell(Generator.random(Generator.Category.SCROLL));

    for (int i = 0; i < 2; i++)
      addItemToSell(Random.Int(2) == 0 ?
              Generator.random(Generator.Category.POTION) :
              Generator.random(Generator.Category.SCROLL));

    return this;
  }

  @Override
  protected void flee() {
    destroy();

    sprite.emitter().burst(Speck.factory(Speck.WOOL), 15);
    sprite.killAndErase();
  }
}
