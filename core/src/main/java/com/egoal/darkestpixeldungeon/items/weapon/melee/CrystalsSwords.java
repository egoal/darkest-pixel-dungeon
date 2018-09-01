package com.egoal.darkestpixeldungeon.items.weapon.melee;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 8/16/2018.
 */

public class CrystalsSwords extends MeleeWeapon {
  {
    image = ItemSpriteSheet.DPD_CRYSTALS_SWORDS;

    tier = 3;
  }

  // 16 + 3*lvl
  @Override
  public int max(int lvl) {
    return 4 * (tier + 1) +
            lvl * (tier);
  }

  @Override
  public Damage giveDamage(Hero hero, Char target) {
    Damage dmg = super.giveDamage(hero, target);

    // 20% chance to deal 2 times damage
    float c = .15f+ .35f*(1f- (float)Math.pow(.7, level()/3));
    
    if (Random.Float() < c) {
      dmg.value *= 2.f;
      dmg.addFeature(Damage.Feature.CRITCIAL);
    }

    return dmg;
  }

}
