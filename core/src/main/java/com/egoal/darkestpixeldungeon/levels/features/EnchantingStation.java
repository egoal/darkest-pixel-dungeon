package com.egoal.darkestpixeldungeon.levels.features;

import android.widget.ArrayAdapter;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.BrokenSeal;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.armor.Armor;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndEnchanting;

/**
 * Created by 93942 on 7/26/2018.
 */

public class EnchantingStation {

  public static void operate(Hero hero) {
    GameScene.show(new WndEnchanting());

  }

  public static String canTransform(Item src, Item tgt) {
    if (!src.isIdentified() || !tgt.isIdentified()) {
      return Messages.get(EnchantingStation.class, "unidentified");
    }
    if (src.cursed || tgt.cursed) {
      return Messages.get(EnchantingStation.class, "cursed");
    }
    if (src instanceof Weapon) {
      if (((Weapon) src).enchantment == null) {
        return Messages.get(EnchantingStation.class, "no_enchantment");
      }

      if (!(tgt instanceof Weapon)) {
        return Messages.get(EnchantingStation.class, "wrong_type");
      }

      return null;
    } else if (src instanceof Armor) {
      if (((Armor) src).glyph == null) {
        return Messages.get(EnchantingStation.class, "no_enchantment");
      }

      if (!(tgt instanceof Armor)) {
        return Messages.get(EnchantingStation.class, "wrong_type");
      }

      return null;
    }
    // never be here
    return "bad operation!";
  }

  public static boolean transform(Item src, Item tgt) {
    GLog.p(Messages.get(EnchantingStation.class, "transformed", src.name(), 
            tgt.name()));

    if (src instanceof Weapon) {
      Weapon wsrc = (Weapon) src;
      Weapon wtgt = (Weapon) tgt;

      wtgt.enchant(wsrc.enchantment);
      return true;
    } else if (src instanceof Armor) {
      Armor asrc = (Armor) src;
      Armor atgt = (Armor) tgt;

      atgt.inscribe(asrc.glyph);

      // check seal
      BrokenSeal bs = asrc.checkSeal();
      if (bs != null)
        atgt.affixSeal(bs);

      return true;
    }

    return false;
  }

}
