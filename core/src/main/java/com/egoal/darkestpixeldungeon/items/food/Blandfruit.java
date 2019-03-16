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
package com.egoal.darkestpixeldungeon.items.food;

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.plants.Plant;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.EarthImbue;
import com.egoal.darkestpixeldungeon.actors.buffs.FireImbue;
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger;
import com.egoal.darkestpixeldungeon.actors.buffs.ToxicImbue;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfExperience;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfFrost;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfInvisibility;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLevitation;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMindVision;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfParalyticGas;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfPurity;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfToxicGas;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class Blandfruit extends Food {
  public Potion potionAttrib = null;
  public ItemSprite.Glowing potionGlow = null;

  {
    stackable = true;
    image = ItemSpriteSheet.BLANDFRUIT;

    //only applies when blandfruit is cooked
    energy = Hunger.STARVING / 2;
    hornValue = 6;

    bones = true;
  }

  @Override
  public boolean isSimilar(Item item) {
    if (item instanceof Blandfruit) {
      if (potionAttrib == null) {
        if (((Blandfruit) item).potionAttrib == null)
          return true;
      } else if (((Blandfruit) item).potionAttrib != null) {
        if (((Blandfruit) item).potionAttrib.getClass() == potionAttrib
                .getClass())
          return true;
      }
    }
    return false;
  }

  @Override
  public void execute(Hero hero, String action) {

    if (action.equals(AC_EAT) && potionAttrib == null) {

      GLog.w(Messages.get(this, "raw"));
      return;

    }

    super.execute(hero, action);

    if (action.equals(AC_EAT) && potionAttrib != null) {

      if (potionAttrib instanceof PotionOfFrost) {
        GLog.i(Messages.get(this, "ice_msg"));
        FrozenCarpaccio.effect(hero);
      } else if (potionAttrib instanceof PotionOfLiquidFlame) {
        GLog.i(Messages.get(this, "fire_msg"));
        Buff.affect(hero, FireImbue.class).set(FireImbue.DURATION);
      } else if (potionAttrib instanceof PotionOfToxicGas) {
        GLog.i(Messages.get(this, "toxic_msg"));
        Buff.affect(hero, ToxicImbue.class).set(ToxicImbue.DURATION);
      } else if (potionAttrib instanceof PotionOfParalyticGas) {
        GLog.i(Messages.get(this, "para_msg"));
        Buff.affect(hero, EarthImbue.class, EarthImbue.DURATION);
      } else {
        potionAttrib.apply(hero);
      }

    }
  }

  @Override
  public String desc() {
    if (potionAttrib == null) return super.desc();
    else return Messages.get(this, "desc_cooked");
  }

  @Override
  public int price() {
    return 20 * quantity;
  }

  public Item cook(Plant.Seed seed) {

    try {
      return imbuePotion((Potion) seed.alchemyClass.newInstance());
    } catch (Exception e) {
      DarkestPixelDungeon.reportException(e);
      return null;
    }

  }

  public Item imbuePotion(Potion potion) {

    potionAttrib = potion;
    potionAttrib.ownedByFruit = true;

    potionAttrib.image = ItemSpriteSheet.BLANDFRUIT;

    if (potionAttrib instanceof PotionOfHealing) {
      name = Messages.get(this, "sunfruit");
      potionGlow = new ItemSprite.Glowing(0x2EE62E);
    } else if (potionAttrib instanceof PotionOfStrength) {
      name = Messages.get(this, "rotfruit");
      potionGlow = new ItemSprite.Glowing(0xCC0022);
    } else if (potionAttrib instanceof PotionOfParalyticGas) {
      name = Messages.get(this, "earthfruit");
      potionGlow = new ItemSprite.Glowing(0x67583D);
    } else if (potionAttrib instanceof PotionOfInvisibility) {
      name = Messages.get(this, "blindfruit");
      potionGlow = new ItemSprite.Glowing(0xE5D273);
    } else if (potionAttrib instanceof PotionOfLiquidFlame) {
      name = Messages.get(this, "firefruit");
      potionGlow = new ItemSprite.Glowing(0xFF7F00);
    } else if (potionAttrib instanceof PotionOfFrost) {
      name = Messages.get(this, "icefruit");
      potionGlow = new ItemSprite.Glowing(0x66B3FF);
    } else if (potionAttrib instanceof PotionOfMindVision) {
      name = Messages.get(this, "fadefruit");
      potionGlow = new ItemSprite.Glowing(0xB8E6CF);
    } else if (potionAttrib instanceof PotionOfToxicGas) {
      name = Messages.get(this, "sorrowfruit");
      potionGlow = new ItemSprite.Glowing(0xA15CE5);
    } else if (potionAttrib instanceof PotionOfLevitation) {
      name = Messages.get(this, "stormfruit");
      potionGlow = new ItemSprite.Glowing(0x1C3A57);
    } else if (potionAttrib instanceof PotionOfPurity) {
      name = Messages.get(this, "dreamfruit");
      potionGlow = new ItemSprite.Glowing(0x8E2975);
    } else if (potionAttrib instanceof PotionOfExperience) {
      name = Messages.get(this, "starfruit");
      potionGlow = new ItemSprite.Glowing(0xA79400);
    }

    if (potionAttrib.canBeReinforced()) potionAttrib.reinforce();

    return this;
  }

  public static final String POTIONATTRIB = "potionattrib";

  @Override
  public void cast(final Hero user, int dst) {
    if (potionAttrib instanceof PotionOfLiquidFlame ||
            potionAttrib instanceof PotionOfToxicGas ||
            potionAttrib instanceof PotionOfParalyticGas ||
            potionAttrib instanceof PotionOfFrost ||
            potionAttrib instanceof PotionOfLevitation ||
            potionAttrib instanceof PotionOfPurity) {
      potionAttrib.cast(user, dst);
      detach(user.belongings.backpack);
    } else {
      super.cast(user, dst);
    }

  }

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(POTIONATTRIB, potionAttrib);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    if (bundle.contains(POTIONATTRIB)) {
      imbuePotion((Potion) bundle.get(POTIONATTRIB));
    }
  }

  @Override
  public ItemSprite.Glowing glowing() {
    return potionGlow;
  }

}
