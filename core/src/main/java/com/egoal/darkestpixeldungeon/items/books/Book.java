package com.egoal.darkestpixeldungeon.items.books;

import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * Created by 93942 on 5/10/2018.
 */

/*
* articles, notes, magic, so on
* this is like to be scroll, 
* but, not random, not consumable, 
* so, make a new class
* 
*/

public abstract class Book extends Item {
  {
    stackable = true;
    defaultAction = AC_READ;
    image = ItemSpriteSheet.DPD_BOOKS;
  }
  
  private static final String AC_READ = "READ";
  
  public String bookName(){
    return Messages.get(this, "bookname");
  }
  public String name(){
    return isIdentified()? bookName(): super.name();
  }
  public String desc(){
    return Messages.get(this, isIdentified()? "bookdesc": "desc");
  }
  
  @Override
  public boolean isSimilar(Item item) {
    return this.isIdentified() && item.isIdentified() && super.isSimilar(item);
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    actions.add(AC_READ);

    return actions;
  }

  @Override
  public void execute(Hero hero, String action) {
    super.execute(hero, action);
    if (action.equals(AC_READ)) {
      if (hero.buff(Blindness.class) != null) {
        GLog.w(Messages.get(this, "blinded"));
      } else {
        doRead();
      }

    }
  }

  protected abstract void doRead();
  
  @Override
  public boolean isUpgradable() {
    return false;
  }

  @Override
  public int price() {
    return 30 * quantity;
  }
}
