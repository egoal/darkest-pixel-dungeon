package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.Fire;
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.unclassified.PotionTestPaper;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.PotionSellerSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 8/26/2018.
 */

public class PotionSeller extends DPDShopKeeper {

  {
    spriteClass = PotionSellerSprite.class;
  }

  @Override
  public DPDShopKeeper initSellItems() {
    // potions
    addItemToSell(new PotionOfHealing());
    int cntItems = Random.Int(3, 8);
    for (int i = 0; i < cntItems; ++i) {
      Generator.Category c = Random.Float() < .6f ? Generator.Category.POTION :
              Generator.Category.SEED;
      Item item = Generator.random(c);
      // may be reinforced
      if (item instanceof Potion) {
        Potion p = (Potion) item;
        if (p.canBeReinforced() && Random.Float() < .3f) {
          p.reinforce();
        }
      }
      addItemToSell(item);
    }

    addItemToSell(new PotionTestPaper().quantity(Random.Int(1, 3)));

    return this;
  }

  @Override
  protected void onPlayerStealFailed(Hero hero) {
    Sample.INSTANCE.play(Assets.SND_SHATTER);

    if(Random.Float()<.7f)
        GameScene.add(Blob.seed(hero.pos, 1000, ToxicGas.class));
    else
        GameScene.add(Blob.seed(hero.pos, 2, Fire.class));
    
    super.onPlayerStealFailed(hero);
  }

}
