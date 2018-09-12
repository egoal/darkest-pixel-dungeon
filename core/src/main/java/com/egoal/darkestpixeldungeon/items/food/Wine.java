package com.egoal.darkestpixeldungeon.items.food;

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure;
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.HeroPerk;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 5/31/2018.
 */

public class Wine extends Item {

  private static final String AC_DRINK = "drink";
  private static final float TIME_TO_DRINK = 2f;

  {
    image = ItemSpriteSheet.DPD_WINE;
    defaultAction = AC_DRINK;
    stackable = true;
  }

  public Wine() {
    super();
    identify();
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    actions.add(AC_DRINK);

    return actions;
  }

  @Override
  public void execute(final Hero hero, String action) {
    super.execute(hero, action);

    if (action == AC_DRINK) {
      detach(hero.belongings.backpack);
      hero.spend(TIME_TO_DRINK);
      hero.busy();

      int value = Math.min(Random.IntRange(15, (int) (Pressure.heroPressure()
              * .5f)), 30);
      if (hero.heroPerk.contain(HeroPerk.Perk.DRUNKARD)) {
        value *= 1.2f;
        hero.recoverSanity(value);
      } else {
        hero.recoverSanity(value);
        // get drunk
        Buff.prolong(hero, Vertigo.class, Math.min(20, value));
        hero.takeDamage(new Damage(hero.HP / 4, this, hero).type(Damage.Type
                .MAGICAL).addFeature(Damage.Feature.PURE));
      }

      hero.sprite.operate(hero.pos);
      GLog.i(Messages.get(this, "drunk"));
    }
  }

  @Override
  public int price(){
    return 15*quantity();
  }
  
}
