package com.egoal.darkestpixeldungeon.items.books;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.windows.WndTextBook;

import java.util.ArrayList;

/**
 * Created by 93942 on 10/14/2018.
 */

public class TextBook extends Book {

  private int pageSize = -1;

  public int pageSize() {
    if (pageSize < 0)
      pageSize = Integer.parseInt(Messages.get(this, "pagesize"));
    return pageSize;
  }

  public String page(int i) {
    return Messages.get(this, String.format("page%d", i));
  }

  @Override
  protected void doRead() {
    identify();
    GameScene.show(new WndTextBook(this));
  }

  @Override
  public int price() {
    return quantity() * pageSize() * 5;
  }
}
